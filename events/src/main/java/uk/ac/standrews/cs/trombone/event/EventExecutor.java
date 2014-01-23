package uk.ac.standrews.cs.trombone.event;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.apache.commons.codec.DecoderException;
import org.mashti.gauge.Counter;
import org.mashti.gauge.MetricRegistry;
import org.mashti.gauge.Rate;
import org.mashti.gauge.Sampler;
import org.mashti.gauge.Timer;
import org.mashti.gauge.reporter.CsvReporter;
import org.mashti.jetson.exception.RPCException;
import org.mashti.jetson.util.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.io.CsvListReader;
import org.supercsv.prefs.CsvPreference;
import org.uncommons.maths.random.MersenneTwisterRNG;
import uk.ac.standrews.cs.shabdiz.util.TimeoutExecutorService;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerConfigurator;
import uk.ac.standrews.cs.trombone.core.PeerFactory;
import uk.ac.standrews.cs.trombone.core.PeerMetric;
import uk.ac.standrews.cs.trombone.core.PeerReference;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class EventExecutor {

    private static final int LOOKUP_RETRY_COUNT = 5;
    private static final Integer[] NO_INDICES = new Integer[0];
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
    private final ExecutorService task_executor;
    private final Semaphore event_queue_semaphore;
    private final Map<PeerReference, Peer> peers_map = new ConcurrentSkipListMap<PeerReference, Peer>();
    private final EventReader event_reader;
    private final MetricRegistry metric_registry;
    private final CsvReporter csv_reporter;
    private final ConcurrentSkipListMap<Long, Integer[]> oracle = new ConcurrentSkipListMap<Long, Integer[]>();
    private final Random random = new MersenneTwisterRNG();
    private final Path observations_home;
    private Future<Void> task_scheduler_future;
    private long start_time;

    public EventExecutor(final Path events_home, int host_index, Path observations_home) throws IOException, DecoderException {

        this.observations_home = observations_home;
        //FIXME get lookup retry count from scenario
       
        event_reader = new EventReader(events_home, host_index);
        runnable_events = new DelayQueue<RunnableExperimentEvent>();
        task_populator = Executors.newCachedThreadPool(new NamedThreadFactory("task_populator_"));
        task_scheduler = Executors.newSingleThreadExecutor(new NamedThreadFactory("task_scheduler_"));
        task_executor = Executors.newFixedThreadPool(10, new NamedThreadFactory("task_executor_"));
        event_queue_semaphore = new Semaphore(0, true);

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
        
        csv_reporter = new CsvReporter(metric_registry, observations_home);

        loadInitialEvents();
        LOGGER.info("loaded initial event queue population");
        startTaskQueuePopulator();

        task_populator.submit(new Callable<Void>() {

            @Override
            public Void call() throws Exception {

                // TODO get rid of the oracle if you can: use a list of alive nodes for when a node joins

                final CsvListReader reader = new CsvListReader(Files.newBufferedReader(events_home.resolve("oracle.csv"), StandardCharsets.UTF_8), CsvPreference.STANDARD_PREFERENCE);
                reader.getHeader(true);
                List<String> line = reader.read();
                while (line != null) {

                    final Long time = Long.valueOf(line.get(0));
                    final Integer[] peer_indices;
                    if (line.get(1).isEmpty()) {
                        peer_indices = NO_INDICES;
                    }
                    else {
                        final String[] peer_indices_as_string = line.get(1).split(" ");
                        final int indices_count = peer_indices_as_string.length;
                        peer_indices = new Integer[indices_count];

                        for (int i = 0; i < indices_count; i++) {
                            peer_indices[i] = Integer.parseInt(peer_indices_as_string[i]);
                        }
                    }
                    oracle.put(time, peer_indices);
                    line = reader.read();
                }
                return null;
            }
        });
    }

    public long getCurrentTimeInNanos() {

        return System.nanoTime() - start_time;
    }

    public synchronized void start() {

        if (!isStarted()) {

            start_time = System.nanoTime();
            csv_reporter.start(10, TimeUnit.SECONDS);
            task_scheduler_future = task_scheduler.submit(new Callable<Void>() {

                @Override
                public Void call() throws Exception {

                    try {
                        while (!Thread.currentThread().isInterrupted() && !runnable_events.isEmpty()) {
                            final RunnableExperimentEvent runnable = runnable_events.take();
                            task_executor.execute(runnable);
                            event_queue_semaphore.release();
                        }
                    }
                    catch (Exception e) {
                        LOGGER.error("failure occurred while executing events", e);
                    }
                    finally {
                        LOGGER.info("finished executing events");
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
            task_scheduler_future.get();
        }
    }

    void queue(Peer peer, final Event event) throws IOException {

        if (event instanceof ChurnEvent) {
            queue(peer, (ChurnEvent) event);
        }
        else if (event instanceof LookupEvent) {
            queue(peer, (LookupEvent) event);

        }
        else {
            throw new IllegalArgumentException("unknown event type: " + event);
        }
    }

    void queue(Peer peer, final ChurnEvent event) throws IOException {

        runnable_events.add(new RunnableChurnEvent(peer, event));
    }

    void queue(Peer peer, final LookupEvent event) {

        runnable_events.add(new RunnableWorkloadEvent(peer, event));
    }

    synchronized Peer getPeerByReference(PeerReference reference, PeerConfigurator configurator) {

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

    private void loadInitialEvents() throws IOException {

        for (int i = 0; i < MAX_BUFFERED_EVENTS; i++) {
            if (event_reader.hasNext()) {
                queueNextEvent();
            }
            else {
                break;
            }
        }
    }

    private Future<Void> startTaskQueuePopulator() {

        return task_populator.submit(new Callable<Void>() {

            @Override
            public Void call() throws Exception {

                try {
                    while (!Thread.currentThread().isInterrupted() && event_reader.hasNext()) {

                        event_queue_semaphore.acquire();
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
        //FIXME NULL CONFIGURATOR
        final Peer peer = getPeerByReference(event_source, null);
        queue(peer, event);
    }

    private void joinWithTimeout(final Peer peer, final long timeout_nanos) {

        try {
            //FIXME optimize; threadpool for joins, use the same thread to retry
            TimeoutExecutorService.awaitCompletion(new Callable<Boolean>() {

                @Override
                public Boolean call() throws Exception {

                    boolean successful;
                    do {
                        try {
                            PeerReference reference = getRandomAlivePeer();
                            peer.join(reference);
                            successful = true;
                        }
                        catch (RPCException e) {
                            successful = false;
                        }
                    } while (!Thread.currentThread().isInterrupted() && !successful);

                    return successful;
                }
            }, timeout_nanos, TimeUnit.NANOSECONDS);
        }
        catch (final Exception e) {
            LOGGER.error("failed to join", e);
        }
    }

    private PeerReference getRandomAlivePeer() {

        final long time_through_experiment = getCurrentTimeInNanos();
        final Map.Entry<Long, Integer[]> floor = oracle.floorEntry(time_through_experiment);
        if (floor != null) {
            final Integer[] candidate_indices = floor.getValue();
            final int candidate_index = random.nextInt(candidate_indices.length);
            return event_reader.getPeerReferenceByIndex(candidate_indices[candidate_index]);
        }
        LOGGER.error("no peer is alive to join to");
        throw new IllegalStateException("peer joining when there is no node alive");
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

    private class RunnableChurnEvent extends RunnableExperimentEvent {

        private RunnableChurnEvent(Peer peer, final ChurnEvent event) {

            super(peer, event);
        }

        @Override
        public void handleEvent() {

            final ChurnEvent event = (ChurnEvent) getEvent();
            try {
                if (event.isAvailable()) {

                    final boolean exposed = peer.expose();
                    if (exposed) {
                        peer_arrival_rate.mark();
                        available_peer_counter.increment();
                    }
                    else {
                        LOGGER.warn("exposure of peer {} was unsuccessful", peer);
                    }
                    joinWithTimeout(peer, event.getDurationInNanos());
                }
                else {
                    final boolean unexposed = peer.unexpose();
                    if (unexposed) {
                        peer_departure_rate.mark();
                        available_peer_counter.decrement();
                    }
                    else {
                        LOGGER.warn("un-exposure of peer {} was unsuccessful", peer);
                    }
                }
            }
            catch (final IOException e) {
                LOGGER.error("failure occurred when executing churn event", e);
            }
        }
    }

    private class RunnableWorkloadEvent extends RunnableExperimentEvent {

        private RunnableWorkloadEvent(Peer peer, final LookupEvent event) {

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
            catch (final Exception e) {
                LOGGER.error("failure occurred when executing lookup", e);
            }
            finally {
                lookup_execution_rate.mark();
            }
        }
    }
}
