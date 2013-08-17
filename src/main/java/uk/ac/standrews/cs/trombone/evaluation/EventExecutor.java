package uk.ac.standrews.cs.trombone.evaluation;

import au.com.bytecode.opencsv.CSVReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.io.FileUtils;
import org.mashti.jetson.exception.RPCException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.shabdiz.util.TimeoutExecutorService;
import uk.ac.standrews.cs.trombone.Peer;
import uk.ac.standrews.cs.trombone.PeerFactory;
import uk.ac.standrews.cs.trombone.PeerMetric;
import uk.ac.standrews.cs.trombone.PeerReference;
import uk.ac.standrews.cs.trombone.metric.SentBytesMeter;
import uk.ac.standrews.cs.trombone.metric.core.Counter;
import uk.ac.standrews.cs.trombone.metric.core.CsvReporter;
import uk.ac.standrews.cs.trombone.metric.core.MetricRegistry;
import uk.ac.standrews.cs.trombone.metric.core.Rate;
import uk.ac.standrews.cs.trombone.metric.core.Sampler;
import uk.ac.standrews.cs.trombone.metric.core.Timer;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class EventExecutor {

    public static final Integer[] NO_INDECIES = new Integer[0];
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
    private final Rate sent_bytes_rate = new Rate();
    private final Sampler event_execution_lag_sampler = new Sampler();
    private final Timer event_execution_duration_timer = new Timer();
    private final DelayQueue<RunnableExperimentEvent> runnable_events;
    private final ExecutorService task_populator;
    private final ExecutorService task_scheduler;
    private final ExecutorService task_executor;
    private final Semaphore event_queue_semaphore;
    private final Map<PeerReference, Peer> peers_map = new ConcurrentSkipListMap<PeerReference, Peer>();
    private final EventCsvReader event_reader;
    private final MetricRegistry metric_registry;
    private final CsvReporter csv_reporter;
    private final AtomicLong max_loaded_oracle_time = new AtomicLong();
    private final ConcurrentSkipListMap<Long, Integer[]> oracle = new ConcurrentSkipListMap<Long, Integer[]>();
    private final Random random = new Random(65465);
    private int lookup_retry_count;
    private Future<Object> task_scheduler_future;
    private long start_time;

    public EventExecutor(File peers_csv, final File events_csv, File lookup_targets_csv, final File oracle_csv) throws IOException {

        event_reader = new EventCsvReader(peers_csv, events_csv, lookup_targets_csv);
        runnable_events = new DelayQueue<RunnableExperimentEvent>();
        task_populator = Executors.newCachedThreadPool();
        task_scheduler = Executors.newSingleThreadExecutor();
        task_executor = Executors.newCachedThreadPool();
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
        metric_registry.register("sent_bytes_rate", SentBytesMeter.getTotalSentBytesMeter());
        metric_registry.register("event_execution_lag_sampler", event_execution_lag_sampler);
        metric_registry.register("event_execution_duration_timer", event_execution_duration_timer);

        final File observations = new File(events_csv.getParent(), "observations");
        FileUtils.deleteDirectory(observations);
        FileUtils.forceMkdir(observations);
        csv_reporter = new CsvReporter(metric_registry, observations);

        loadInitialEvents();
        LOGGER.info("loaded initial event queue population");
        startTaskQueuePopulator();

        //        task_populator.submit(new Callable<Void>() {
        //
        //            @Override
        //            public Void call() throws Exception {

        final CSVReader reader = new CSVReader(new FileReader(oracle_csv));

        reader.readNext();
        String[] line = reader.readNext();
        while (line != null) {

            final Long time = Long.valueOf(line[0]);

            final Integer[] peer_indecies;
            if (line[1].equals("")) {
                peer_indecies = NO_INDECIES;
            }
            else {

                final String[] peer_indecies_as_string = line[1].split(" ");

                final int indecies_count = peer_indecies_as_string.length;
                peer_indecies = new Integer[indecies_count];

                for (int i = 0; i < indecies_count; i++) {
                    peer_indecies[i] = Integer.parseInt(peer_indecies_as_string[i]);
                }
            }
            oracle.put(time, peer_indecies);
            line = reader.readNext();
        }
        System.out.println("loaded oracle");
        //                return null;
        //            }
        //        });

    }

    public static void main(String[] args) throws IOException {

        EventExecutor eventExecutor = new EventExecutor(new File("/Users/masih/Desktop/test/peers.csv"), new File("/Users/masih/Desktop/test/1/events.csv"), new File("/Users/masih/Desktop/test/lookup_targets.csv"), new File("/Users/masih/Desktop/test/oracle.csv"));
        eventExecutor.start();
    }

    public long getCurrentTimeInNanos() {

        return System.nanoTime() - start_time;
    }

    public synchronized void start() {
        if (!isStarted()) {

            start_time = System.nanoTime();
            csv_reporter.start(10, TimeUnit.SECONDS);
            task_scheduler_future = task_scheduler.submit(new Callable<Object>() {

                @Override
                public Object call() throws Exception {
                    try {
                        while (!Thread.currentThread().isInterrupted() && !runnable_events.isEmpty()) {
                            final RunnableExperimentEvent runnable = runnable_events.take();
                            task_executor.execute(runnable);
                            event_queue_semaphore.release();
                        }
                    }
                    catch (Exception e) {
                        LOGGER.error("failure occured while executing events", e);
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

    private void loadInitialEvents() throws IOException {
        for (int i = 0; i < MAX_BUFFERED_EVENTS; i++) {
            if (event_reader.hasNext()) { queueNextEvent(); }
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
                    LOGGER.error("failure occured while queuing events", e);
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
        final Peer peer = getPeerByReference(event_source);
        execute(peer, event);
    }

    void execute(Peer peer, final Event event) throws IOException {

        if (event instanceof ChurnEvent) {
            execute(peer, (ChurnEvent) event);
        }
        else if (event instanceof LookupEvent) {
            execute(peer, (LookupEvent) event);

        }
        else {
            throw new IllegalArgumentException("unknown event type: " + event);
        }
    }

    void execute(Peer peer, final ChurnEvent event) throws IOException {

        runnable_events.add(new RunnableChurnEvent(peer, event));
    }

    void execute(Peer peer, final LookupEvent event) {

        runnable_events.add(new RunnableWorkloadEvent(peer, event));
    }

    private void joinWithTimeout(final Peer peer, final long timeout_nanos) {
        try {
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
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private PeerReference getRandomAlivePeer() {
        //FIXME
        final Map.Entry<Long, Integer[]> floor = oracle.floorEntry(getCurrentTimeInNanos());
        if (floor == null) {
            LOGGER.error("no node to join");
            throw new IllegalStateException("peer joining when there is no node alive");
        }
        else {
            final Integer[] candidates = floor.getValue();
            final int candidate = random.nextInt(candidates.length);
            return event_reader.getPeerReferenceByIndex(candidate);
        }
    }

    synchronized Peer getPeerByReference(PeerReference reference) {
        final Peer peer;
        if (!peers_map.containsKey(reference)) {
            peer = PeerFactory.createPeer(reference);
            peers_map.put(reference, peer);
        }
        else {
            peer = peers_map.get(reference);
        }
        return peer;
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
                LOGGER.error("failure occured when executing churn event", e);
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
                final PeerMetric.LookupMeasurement measurement = peer.lookup(event.getTarget(), lookup_retry_count);
                final PeerReference expected_result = event.getExpectedResult();
                final long hop_count = measurement.getHopCout();
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
                LOGGER.error("failure occured when executing lookup", e);
            }
            finally {
                lookup_execution_rate.mark();
            }
        }
    }
}
