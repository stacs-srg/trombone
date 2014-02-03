package uk.ac.standrews.cs.trombone.event;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.commons.codec.DecoderException;
import org.mashti.gauge.Counter;
import org.mashti.gauge.Gauge;
import org.mashti.gauge.MetricRegistry;
import org.mashti.gauge.Rate;
import org.mashti.gauge.Sampler;
import org.mashti.gauge.Timer;
import org.mashti.gauge.reporter.CsvReporter;
import org.mashti.jetson.exception.RPCException;
import org.mashti.jetson.util.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerConfiguration;
import uk.ac.standrews.cs.trombone.core.PeerFactory;
import uk.ac.standrews.cs.trombone.core.PeerMetric;
import uk.ac.standrews.cs.trombone.core.PeerReference;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class EventExecutor {

    private static final int LOOKUP_RETRY_COUNT = 5;
    private static final int MAX_BUFFERED_EVENTS = 20000;
    private static final Logger LOGGER = LoggerFactory.getLogger(EventExecutor.class);
    private final Rate lookup_execution_rate = new Rate();
    private final Rate lookup_failure_rate = new Rate();
    private final Rate lookup_correctness_rate = new Rate();
    private final Sampler lookup_correctness_hop_count_sampler = new Sampler();
    private final Sampler lookup_correctness_retry_count_sampler = new Sampler();
    private final Timer lookup_correctness_delay_timer = new Timer();
    private final Rate lookup_incorrectness_rate = new Rate();
    private final Sampler lookup_incorrectness_hop_count_sampler = new Sampler();
    private final Sampler lookup_incorrectness_retry_count_sampler = new Sampler();
    private final Timer lookup_incorrectness_delay_timer = new Timer();
    private final Counter available_peer_counter = new Counter();
    private final Rate peer_arrival_rate = new Rate();
    private final Rate peer_departure_rate = new Rate();
    private final Sampler event_execution_lag_sampler = new Sampler();
    private final Timer event_execution_duration_timer = new Timer();
    private final DelayQueue<RunnableExperimentEvent> runnable_events;
    private final ExecutorService task_populator;
    private final ExecutorService task_scheduler;
    private final ThreadPoolExecutor task_executor;
    private final ThreadPoolExecutor task_executor2;
    private final Semaphore load_balancer;
    private final Map<PeerReference, Peer> peers_map = new ConcurrentSkipListMap<PeerReference, Peer>();
    private final EventReader event_reader;
    private final MetricRegistry metric_registry;
    private final CsvReporter csv_reporter;
    private final Rate join_failure_rate = new Rate();
    private final Rate join_success_rate = new Rate();
    private final Gauge<Integer> queue_size = new Gauge<Integer>() {

        @Override
        public Integer get() {

            return runnable_events.size();
        }
    };
    private final Future<Void> task_populator_future;
    private Future<Void> task_scheduler_future;
    private long start_time;

    //FIXME Add timeout for lookup execution or maybe any event execution
    //FIXME get lookup retry count from scenario

    public EventExecutor(final Path events_home, int host_index, Path observations_home) throws IOException, DecoderException, ClassNotFoundException {

        event_reader = new EventReader(events_home, host_index);
        runnable_events = new DelayQueue<RunnableExperimentEvent>();
        task_populator = Executors.newFixedThreadPool(100, new NamedThreadFactory("task_populator_"));
        task_scheduler = Executors.newFixedThreadPool(10, new NamedThreadFactory("task_scheduler_"));
        task_executor = new ThreadPoolExecutor(400, 800, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new NamedThreadFactory("task_executor_"));
        task_executor.prestartAllCoreThreads();
        task_executor2 = new ThreadPoolExecutor(100, 100, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new NamedThreadFactory("task_executor2_"));
        load_balancer = new Semaphore(MAX_BUFFERED_EVENTS, true);

        metric_registry = new MetricRegistry("test");
        metric_registry.register("lookup_execution_rate", lookup_execution_rate);
        metric_registry.register("lookup_failure_rate", lookup_failure_rate);
        metric_registry.register("lookup_correctness_rate", lookup_correctness_rate);
        metric_registry.register("lookup_correctness_hop_count_sampler", lookup_correctness_hop_count_sampler);
        metric_registry.register("lookup_correctness_retry_count_sampler", lookup_correctness_retry_count_sampler);
        metric_registry.register("lookup_correctness_delay_timer", lookup_correctness_delay_timer);
        metric_registry.register("lookup_incorrectness_rate", lookup_incorrectness_rate);
        metric_registry.register("lookup_incorrectness_hop_count_sampler", lookup_incorrectness_hop_count_sampler);
        metric_registry.register("lookup_incorrectness_retry_count_sampler", lookup_incorrectness_retry_count_sampler);
        metric_registry.register("lookup_incorrectness_delay_timer", lookup_incorrectness_delay_timer);
        metric_registry.register("available_peer_counter", available_peer_counter);
        metric_registry.register("peer_arrival_rate", peer_arrival_rate);
        metric_registry.register("peer_departure_rate", peer_departure_rate);
        metric_registry.register("sent_bytes_rate", PeerMetric.getGlobalSentBytesRate());
        metric_registry.register("event_execution_lag_sampler", event_execution_lag_sampler);
        metric_registry.register("event_execution_duration_timer", event_execution_duration_timer);
        metric_registry.register("join_failure_rate", join_failure_rate);
        metric_registry.register("join_success_rate", join_success_rate);
        metric_registry.register("queue_size", queue_size);

        csv_reporter = new CsvReporter(metric_registry, observations_home);
        task_populator_future = startTaskQueuePopulator();
    }

    public long getCurrentTimeInNanos() {

        return System.nanoTime() - start_time;
    }

    public synchronized void start() throws InterruptedException {

        if (!isStarted()) {

            task_scheduler_future = task_scheduler.submit(new Callable<Void>() {

                @Override
                public Void call() throws Exception {

                    awaitInitialEventLoading();
                    LOGGER.info("starting event execution...");
                    start_time = System.nanoTime();
                    csv_reporter.start(10, TimeUnit.SECONDS);
                    try {
                        while (!Thread.currentThread().isInterrupted() && !task_populator_future.isDone() || !runnable_events.isEmpty()) {
                            final RunnableExperimentEvent runnable = runnable_events.take();
                            task_executor.execute(runnable);
                            load_balancer.release();
                        }
                    }
                    catch (Exception e) {
                        LOGGER.error("failure occurred while executing events", e);
                    }
                    finally {
                        LOGGER.info("finished executing events");
                        csv_reporter.stop();
                    }
                    return null;
                }
            });
        }
    }

    public synchronized boolean isStarted() {

        return task_scheduler_future != null && !task_scheduler_future.isDone();
    }

    public synchronized void stop() {

        if (isStarted()) {
            task_scheduler_future.cancel(true);
        }
    }

    public void awaitCompletion() throws InterruptedException, ExecutionException {

        if (isStarted()) {
            task_populator_future.get();
            task_scheduler_future.get();
            //            task_executor.shutdown();
            //            task_executor2.shutdown();
            //            task_executor.awaitTermination(10, TimeUnit.MINUTES);
            //            task_executor2.awaitTermination(10, TimeUnit.MINUTES);
        }
    }

    void queue(Peer peer, final Event event) throws IOException {

        if (event instanceof JoinEvent) {
            queue(peer, (JoinEvent) event);
        }
        else if (event instanceof LeaveEvent) {
            queue(peer, (LeaveEvent) event);
        }
        else if (event instanceof LookupEvent) {
            queue(peer, (LookupEvent) event);
        }
        else {
            throw new IllegalArgumentException("unknown event type: " + event);
        }
    }

    void queue(Peer peer, final JoinEvent event) throws IOException {

        runnable_events.add(new RunnableJoinEvent(peer, event));
    }

    void queue(Peer peer, final LeaveEvent event) throws IOException {

        runnable_events.add(new RunnableLeaveEvent(peer, event));
    }

    void queue(Peer peer, final LookupEvent event) {

        runnable_events.add(new RunnableLookupEvent(peer, event));
    }

    synchronized Peer getPeerByReference(PeerReference reference, PeerConfiguration configurator) {

        final Peer peer;
        if (!peers_map.containsKey(reference)) {
            peer = PeerFactory.createPeer(reference, configurator);
            peers_map.put(reference, peer);
        }
        else {
            peer = peers_map.get(reference);
        }
        return peer;
    }

    private void awaitInitialEventLoading() throws InterruptedException {

        LOGGER.info("awaiting initial event population...");
        while (load_balancer.availablePermits() != 0 && !task_populator_future.isDone()) {
            Thread.sleep(1000);
        }
    }

    private Future<Void> startTaskQueuePopulator() {

        return task_populator.submit(new Callable<Void>() {

            @Override
            public Void call() throws Exception {

                try {
                    while (!Thread.currentThread().isInterrupted() && event_reader.hasNext()) {

                        load_balancer.acquire();
                        queueNextEvent();
                    }
                }
                catch (Exception e) {
                    LOGGER.error("failure occurred while queuing events", e);
                }
                finally {
                    LOGGER.info("finished queueing events");
                }
                return null;
            }
        });
    }

    private void queueNextEvent() throws IOException {

        final Event event = event_reader.next();
        final PeerReference event_source = event.getSource();
        final PeerConfiguration configuration = event_reader.getConfiguration(event_source);
        final Peer peer = getPeerByReference(event_source, configuration);
        queue(peer, event);
    }

    private void joinWithTimeout(final Peer peer, final long timeout_nanos, final Set<PeerReference> known_peers) {

        if (known_peers != null && !known_peers.isEmpty()) {

            final Future<Boolean> future_join = task_executor2.submit(new Callable<Boolean>() {

                @Override
                public Boolean call() throws Exception {

                    final Iterator<PeerReference> iterator = known_peers.iterator();
                    boolean successful = false;
                    while (!Thread.currentThread().isInterrupted() && !successful && iterator.hasNext()) {
                        final PeerReference reference = iterator.next();
                        try {
                            peer.join(reference);
                            successful = true;
                        }
                        catch (RPCException e) {
                            LOGGER.trace("error while attempting to join ", e);
                            successful = false;
                        }
                    }
                    return successful;
                }
            });
            try {
                final boolean successfully_joined = future_join.get(timeout_nanos, TimeUnit.NANOSECONDS);

                if (successfully_joined) {
                    join_success_rate.mark();
                }
                else {
                    join_failure_rate.mark();
                }
            }
            catch (final Exception e) {
                join_failure_rate.mark();
                LOGGER.warn("failed to join: {}", e.getMessage());
                LOGGER.debug("failed to join", e);
            }
            finally {
                future_join.cancel(true);
            }
        }
    }

    private abstract class RunnableExperimentEvent implements Runnable, Delayed {

        protected final Peer peer;
        private final Event event;

        private RunnableExperimentEvent(Peer peer, Event event) {

            this.peer = peer;
            this.event = event;
        }

        @Override
        public long getDelay(final TimeUnit unit) {

            final long remaining_delay_nanos = event.getTimeInNanos() - getCurrentTimeInNanos();
            return unit.convert(remaining_delay_nanos, TimeUnit.NANOSECONDS);
        }

        @Override
        public int compareTo(final Delayed other) {

            if (other instanceof RunnableExperimentEvent) {
                final RunnableExperimentEvent other_experiment_event = (RunnableExperimentEvent) other;
                return event.compareTo(other_experiment_event.event);
            }
            final Long delay = getDelay(TimeUnit.NANOSECONDS);
            final Long other_delay = other.getDelay(TimeUnit.NANOSECONDS);
            return delay.compareTo(other_delay);
        }

        @Override
        public final void run() {

            event_execution_lag_sampler.update(getCurrentTimeInNanos() - event.getTimeInNanos());
            final Timer.Time time = event_execution_duration_timer.time();
            try {
                handleEvent();
            }
            finally {
                time.stop();
            }
        }

        protected abstract void handleEvent();

        protected Event getEvent() {

            return event;
        }

    }

    private class RunnableJoinEvent extends RunnableExperimentEvent {

        private RunnableJoinEvent(Peer peer, final JoinEvent event) {

            super(peer, event);
        }

        @Override
        public void handleEvent() {

            try {
                final JoinEvent join_event = (JoinEvent) getEvent();
                final boolean successfully_exposed = peer.expose();
                if (successfully_exposed) {
                    peer_arrival_rate.mark();
                    available_peer_counter.increment();
                }
                else {
                    LOGGER.warn("exposure of peer {} was unsuccessful", peer);
                }

                joinWithTimeout(peer, join_event.getDurationInNanos(), join_event.getKnownPeerReferences());
            }
            catch (final IOException e) {
                LOGGER.error("failure occurred when executing churn event", e);
            }
        }
    }

    private class RunnableLeaveEvent extends RunnableExperimentEvent {

        private RunnableLeaveEvent(Peer peer, final LeaveEvent event) {

            super(peer, event);
        }

        @Override
        public void handleEvent() {

            try {
                final boolean successfully_unexposed = peer.unexpose();
                if (successfully_unexposed) {
                    peer_departure_rate.mark();
                    available_peer_counter.decrement();
                }
                else {
                    LOGGER.debug("un-exposure of peer {} was unsuccessful typically because it was already unexposed", peer);
                }
            }
            catch (final IOException e) {
                LOGGER.error("failed to unexpose peer", e);
            }
        }
    }

    private class RunnableLookupEvent extends RunnableExperimentEvent {

        private RunnableLookupEvent(Peer peer, final LookupEvent event) {

            super(peer, event);
        }

        @Override
        public void handleEvent() {

            final LookupEvent event = (LookupEvent) getEvent();

            try {

                final PeerMetric.LookupMeasurement measurement = peer.lookup(event.getTarget(), LOOKUP_RETRY_COUNT);
                final PeerReference expected_result = event.getExpectedResult();
                final long hop_count = measurement.getHopCount();
                final long retry_count = measurement.getRetryCount();
                final long duration_in_nanos = measurement.getDurationInNanos();

                if (measurement.isDoneInError()) {
                    lookup_failure_rate.mark();
                }
                else if (measurement.getResult().equals(expected_result)) {
                    lookup_correctness_rate.mark();
                    lookup_correctness_hop_count_sampler.update(hop_count);
                    lookup_correctness_retry_count_sampler.update(retry_count);
                    lookup_correctness_delay_timer.update(duration_in_nanos, TimeUnit.NANOSECONDS);
                }
                else {
                    lookup_incorrectness_rate.mark();
                    lookup_incorrectness_hop_count_sampler.update(hop_count);
                    lookup_incorrectness_retry_count_sampler.update(retry_count);
                    lookup_incorrectness_delay_timer.update(duration_in_nanos, TimeUnit.NANOSECONDS);
                }
            }
            catch (final Throwable e) {
                LOGGER.error("failure occurred when executing lookup", e);
            }
            finally {
                lookup_execution_rate.mark();
            }
        }
    }
}
