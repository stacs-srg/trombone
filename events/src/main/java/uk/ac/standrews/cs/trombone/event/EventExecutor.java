package uk.ac.standrews.cs.trombone.event;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
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
import java.util.concurrent.TimeoutException;
import org.apache.commons.codec.DecoderException;
import org.json.JSONObject;
import org.mashti.gauge.Counter;
import org.mashti.gauge.Gauge;
import org.mashti.gauge.MetricRegistry;
import org.mashti.gauge.Rate;
import org.mashti.gauge.Sampler;
import org.mashti.gauge.Timer;
import org.mashti.gauge.jvm.MemoryUsageGauge;
import org.mashti.gauge.jvm.SystemLoadAverageGauge;
import org.mashti.gauge.jvm.ThreadCountGauge;
import org.mashti.gauge.jvm.ThreadCpuUsageGauge;
import org.mashti.gauge.reporter.CsvReporter;
import org.mashti.jetson.util.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.shabdiz.util.Duration;
import uk.ac.standrews.cs.trombone.core.InternalPeerReference;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerClientFactory;
import uk.ac.standrews.cs.trombone.core.PeerConfiguration;
import uk.ac.standrews.cs.trombone.core.PeerFactory;
import uk.ac.standrews.cs.trombone.core.PeerMetric;
import uk.ac.standrews.cs.trombone.core.PeerReference;
import uk.ac.standrews.cs.trombone.core.PeerServerFactory;
import uk.ac.standrews.cs.trombone.core.PeerState;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class EventExecutor {

    private static final int MAX_BUFFERED_EVENTS = 3_000;
    private static final Logger LOGGER = LoggerFactory.getLogger(EventExecutor.class);

    private final Rate lookup_execution_rate = new Rate();
    private final Rate lookup_failure_rate = new Rate();
    private final Sampler lookup_failure_hop_count_sampler = new Sampler();
    private final Sampler lookup_failure_retry_count_sampler = new Sampler();
    private final Timer lookup_failure_delay_timer = new Timer();
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
    private final Rate event_scheduling_rate = new Rate();
    private final Rate event_completion_rate = new Rate();
    private final DelayQueue<RunnableExperimentEvent> runnable_events;
    private final ExecutorService task_populator;
    private final ExecutorService task_scheduler;
    private final ThreadPoolExecutor task_executor;
    private final Semaphore load_balancer;
    private final Map<PeerReference, Peer> peers_map = new ConcurrentSkipListMap<PeerReference, Peer>();
    private final EventReader event_reader;
    private final MetricRegistry metric_registry;
    private final CsvReporter csv_reporter;
    private final Rate join_failure_rate = new Rate();
    private final Rate join_success_rate = new Rate();
    private final Gauge<Double> sent_bytes_per_alive_peer_per_second_gauge = new Gauge<Double>() {

        @Override
        public Double get() {

            return PeerMetric.getGlobalSentBytesRate().getRate() / available_peer_counter.get();
        }
    };
    private final ThreadCountGauge thread_count_gauge = new ThreadCountGauge();
    private final SystemLoadAverageGauge system_load_average_gauge = new SystemLoadAverageGauge();
    private final ThreadCpuUsageGauge thread_cpu_usage_gauge = new ThreadCpuUsageGauge();
    private final MemoryUsageGauge memory_usage_gauge = new MemoryUsageGauge();
    private final Gauge event_executor_queue_size = new Gauge() {

        @Override
        public Object get() {

            return task_executor.getQueue().size();
        }
    };
    private final Gauge reachable_state_size_per_alive_peer_gauge = new Gauge() {

        @Override
        public Object get() {

            double number_of_reachable_state = 0;
            for (Peer peer : peers_map.values()) {
                if (peer.isExposed()) {
                    final PeerState state = peer.getPeerState();
                    for (InternalPeerReference reference : state) {
                        if (reference.isReachable()) {
                            number_of_reachable_state++;
                        }
                    }
                }
            }
            return number_of_reachable_state / available_peer_counter.get();
        }
    };
    private final Gauge unreachable_state_size_per_alive_peer_gauge = new Gauge() {

        @Override
        public Object get() {

            double number_of_unreachable_state = 0;
            for (Peer peer : peers_map.values()) {
                if (peer.isExposed()) {
                    final PeerState state = peer.getPeerState();
                    for (InternalPeerReference reference : state) {
                        if (!reference.isReachable()) {
                            number_of_unreachable_state++;
                        }
                    }
                }
            }
            return number_of_unreachable_state / available_peer_counter.get();
        }
    };

    private final Gauge<Integer> queue_size_gauge = new Gauge<Integer>() {

        @Override
        public Integer get() {

            return runnable_events.size();
        }
    };
    private final Future<Void> task_populator_future;
    private final JSONObject scenario_properties;
    private final int lookup_retry_count;
    private Future<Void> task_scheduler_future;
    private long start_time;

    public EventExecutor(final Path events_home, int host_index, Path observations_home) throws IOException, DecoderException, ClassNotFoundException {

        this(events_home, host_index, observations_home, null);
    }

    public EventExecutor(final Path events_home, int host_index, Path observations_home, final HashMap<Integer, String> host_indices) throws IOException, DecoderException, ClassNotFoundException {

        this(new CsvEventReader(events_home, host_index, host_indices), observations_home);
    }

    public EventExecutor(final EventReader reader, Path observations_home) {

        event_reader = reader;
        runnable_events = new DelayQueue<RunnableExperimentEvent>();
        task_populator = Executors.newSingleThreadExecutor(new NamedThreadFactory("task_populator_"));
        task_scheduler = Executors.newSingleThreadExecutor(new NamedThreadFactory("task_scheduler_"));
        task_executor = new ThreadPoolExecutor(500, 500, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new NamedThreadFactory("task_executor_"));
        task_executor.prestartAllCoreThreads();

        load_balancer = new Semaphore(MAX_BUFFERED_EVENTS, true);
        scenario_properties = reader.getScenario();
        lookup_retry_count = getLookupRetryCount();
        metric_registry = new MetricRegistry("test");
        metric_registry.register("lookup_execution_rate", lookup_execution_rate);
        metric_registry.register("lookup_failure_rate", lookup_failure_rate);
        metric_registry.register("lookup_failure_hop_count_sampler", lookup_failure_hop_count_sampler);
        metric_registry.register("lookup_failure_retry_count_sampler", lookup_failure_retry_count_sampler);
        metric_registry.register("lookup_failure_delay_timer", lookup_failure_delay_timer);
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
        metric_registry.register("sent_bytes_per_alive_peer_per_second_gauge", sent_bytes_per_alive_peer_per_second_gauge);
        metric_registry.register("sent_bytes_rate", PeerMetric.getGlobalSentBytesRate());
        metric_registry.register("event_executor_queue_size", event_executor_queue_size);
        metric_registry.register("event_execution_lag_sampler", event_execution_lag_sampler);
        metric_registry.register("event_execution_duration_timer", event_execution_duration_timer);
        metric_registry.register("event_scheduling_rate", event_scheduling_rate);
        metric_registry.register("event_completion_rate", event_completion_rate);
        metric_registry.register("join_failure_rate", join_failure_rate);
        metric_registry.register("join_success_rate", join_success_rate);
        metric_registry.register("queue_size_gauge", queue_size_gauge);
        metric_registry.register("reachable_state_size_per_alive_peer_gauge", reachable_state_size_per_alive_peer_gauge);
        metric_registry.register("unreachable_state_size_per_alive_peer_gauge", unreachable_state_size_per_alive_peer_gauge);
        metric_registry.register("thread_count_gauge", thread_count_gauge);
        metric_registry.register("system_load_average_gauge", system_load_average_gauge);
        metric_registry.register("thread_cpu_usage_gauge", thread_cpu_usage_gauge);
        metric_registry.register("memory_usage_gauge", memory_usage_gauge);

        csv_reporter = new CsvReporter(metric_registry, observations_home);
        task_populator_future = startTaskQueuePopulator();
    }

    private Duration getObservationInterval() {

        final JSONObject observation_interval_json = scenario_properties.getJSONObject("observationInterval");
        return new Duration(observation_interval_json.getLong("length"), TimeUnit.valueOf(observation_interval_json.getString("timeUnit")));

    }

    public Duration getExperimentDuration() {

        final JSONObject observation_interval_json = scenario_properties.getJSONObject("experimentDuration");
        return new Duration(observation_interval_json.getLong("length"), TimeUnit.valueOf(observation_interval_json.getString("timeUnit")));

    }

    private int getLookupRetryCount() {

        return scenario_properties.getInt("lookupRetryCount");
    }

    public long getCurrentTimeInNanos() {

        return System.nanoTime() - start_time;
    }

    public synchronized void start() {

        if (!isStarted()) {

            task_scheduler_future = task_scheduler.submit(new Callable<Void>() {

                @Override
                public Void call() throws Exception {

                    awaitInitialEventLoading();
                    LOGGER.info("starting event execution...");
                    start_time = System.nanoTime();

                    final Duration observation_interval = getObservationInterval();
                    csv_reporter.start(observation_interval.getLength(), observation_interval.getTimeUnit());

                    try {
                        while (!Thread.currentThread().isInterrupted() && !task_populator_future.isDone() || !runnable_events.isEmpty()) {
                            final RunnableExperimentEvent runnable = runnable_events.take();
                            task_executor.execute(runnable);
                            event_scheduling_rate.mark();
                            load_balancer.release();
                        }
                    }
                    catch (Throwable e) {
                        LOGGER.error("failure occurred while executing events", e);
                    }
                    finally {
                        LOGGER.info("finished executing events ");
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

    public synchronized void shutdown() {

        task_executor.shutdownNow();
        task_populator.shutdownNow();
        task_scheduler.shutdownNow();

        for (Peer peer : peers_map.values()) {
            try {
                peer.unexpose();
            }
            catch (Exception e) {
                LOGGER.warn("failed to unexpose peer {} due to {}", peer, e);
            }
        }
        PeerClientFactory.shutdownPeerClientFactory();
        PeerServerFactory.shutdownPeerServerFactory();
    }

    public synchronized void stop() {

        if (isStarted()) {
            task_scheduler_future.cancel(true);
        }
    }

    public void awaitCompletion(long timeout, TimeUnit timeout_unit) throws InterruptedException, TimeoutException {

        if (isStarted()) {
            try {
                task_populator_future.get(timeout, timeout_unit);
                task_scheduler_future.get(timeout, timeout_unit);
            }
            catch (ExecutionException e) {
                LOGGER.warn("event executor encountered problems ", e);
            }
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

        //        runnable_events.add(new RunnableLookupEvent(peer, event));
        runnable_events.add(new RunnableLookupAsynchEvent(peer, event));
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
                catch (Throwable e) {
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
        final PeerReference event_source;

        final PeerConfiguration configuration;

        final Participant participant = event.getParticipant();
        if (participant != null) {
            configuration = participant.getPeerConfiguration();
            event_source = participant.getReference();
        }
        else {
            event_source = event.getSource();
            configuration = event_reader.getConfiguration(event_source);
        }

        final Peer peer = getPeerByReference(event_source, configuration);
        queue(peer, event);
    }

    private void joinWithTimeout(final Peer peer, final long timeout_nanos, final Set<PeerReference> known_peers) {

        if (known_peers != null && !known_peers.isEmpty()) {

            final Iterator<PeerReference> iterator = known_peers.iterator();
            boolean successful = false;
            try {
                while (!Thread.currentThread().isInterrupted() && !successful && iterator.hasNext()) {
                    final PeerReference reference = iterator.next();
                    peer.join(reference);
                    successful = true;
                }
            }
            finally {

                if (successful) {
                    join_success_rate.mark();
                }
                else {
                    join_failure_rate.mark();
                }
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
        public boolean equals(final Object other) {

            if (this == other) { return true; }
            if (!(other instanceof RunnableExperimentEvent)) { return false; }

            final RunnableExperimentEvent that = (RunnableExperimentEvent) other;
            return event.equals(that.event) && peer.equals(that.peer);
        }

        @Override
        public int hashCode() {

            return 31 * peer.hashCode() + event.hashCode();
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
                event_completion_rate.mark();
            }
        }

        protected abstract void handleEvent();

        protected Event getEvent() {

            return event;
        }

    }

    private class RunnableJoinEvent extends RunnableExperimentEvent {

        private final Logger logger = LoggerFactory.getLogger(RunnableJoinEvent.class);

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
                    logger.warn("exposure of peer {} was unsuccessful", peer);
                }

                joinWithTimeout(peer, join_event.getDurationInNanos(), join_event.getKnownPeerReferences());
            }
            catch (final IOException e) {
                logger.warn("failed to expose peer {} on address {}", peer, peer.getAddress());
                logger.error("failure occurred when executing join event", e);
            }
        }
    }

    private class RunnableLeaveEvent extends RunnableExperimentEvent {

        private final Logger logger = LoggerFactory.getLogger(RunnableLeaveEvent.class);

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
                    logger.trace("un-exposure of peer {} was unsuccessful typically because it was already unexposed", peer);
                }
            }
            catch (final IOException e) {
                logger.error("failed to unexpose peer", e);
            }
        }
    }

    private class RunnableLookupEvent extends RunnableExperimentEvent {

        private final Logger logger = LoggerFactory.getLogger(RunnableLookupEvent.class);

        private RunnableLookupEvent(Peer peer, final LookupEvent event) {

            super(peer, event);
        }

        @Override
        public void handleEvent() {

            final LookupEvent event = (LookupEvent) getEvent();

            try {

                final PeerMetric.LookupMeasurement measurement = peer.lookup(event.getTarget(), lookup_retry_count);
                final PeerReference expected_result = event.getExpectedResult();
                final long hop_count = measurement.getHopCount();
                final long retry_count = measurement.getRetryCount();
                final long duration_in_nanos = measurement.getDurationInNanos();

                if (measurement.isDoneInError()) {
                    lookup_failure_rate.mark();
                    lookup_failure_hop_count_sampler.update(hop_count);
                    lookup_failure_retry_count_sampler.update(retry_count);
                    lookup_failure_delay_timer.update(duration_in_nanos, TimeUnit.NANOSECONDS);
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
                logger.error("failure occurred when executing lookup", e);
            }
            finally {
                lookup_execution_rate.mark();
            }
        }
    }

    private class RunnableLookupAsynchEvent extends RunnableExperimentEvent {

        private final Logger logger = LoggerFactory.getLogger(RunnableLookupEvent.class);

        private RunnableLookupAsynchEvent(Peer peer, final LookupEvent event) {

            super(peer, event);
        }

        @Override
        public void handleEvent() {

            final LookupEvent event = (LookupEvent) getEvent();

            Futures.addCallback(peer.lookupAsynch(event.getTarget(), lookup_retry_count), new FutureCallback<PeerMetric.LookupMeasurement>() {

                @Override
                public void onSuccess(final PeerMetric.LookupMeasurement measurement) {

                    lookup_execution_rate.mark();
                    final PeerReference expected_result = event.getExpectedResult();
                    final long hop_count = measurement.getHopCount();
                    final long retry_count = measurement.getRetryCount();
                    final long duration_in_nanos = measurement.getDurationInNanos();

                    if (measurement.isDoneInError()) {
                        lookup_failure_rate.mark();
                        lookup_failure_hop_count_sampler.update(hop_count);
                        lookup_failure_retry_count_sampler.update(retry_count);
                        lookup_failure_delay_timer.update(duration_in_nanos, TimeUnit.NANOSECONDS);
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

                @Override
                public void onFailure(final Throwable t) {

                    lookup_execution_rate.mark();
                    logger.error("failure occurred when executing lookup", t);
                }
            }, task_executor);
        }
    }
}
