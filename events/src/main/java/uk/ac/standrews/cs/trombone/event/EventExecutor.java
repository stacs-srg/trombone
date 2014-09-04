package uk.ac.standrews.cs.trombone.event;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
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
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.mashti.gauge.MetricRegistry;
import org.mashti.gauge.Timer;
import org.mashti.gauge.reporter.CsvReporter;
import org.mashti.jetson.util.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.shabdiz.util.Duration;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerMetric;
import uk.ac.standrews.cs.trombone.core.PeerReference;
import uk.ac.standrews.cs.trombone.core.maintenance.EvaluatedDisseminationStrategy;
import uk.ac.standrews.cs.trombone.core.maintenance.EvolutionaryMaintenance;
import uk.ac.standrews.cs.trombone.core.maintenance.Maintenance;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class EventExecutor {

    private static final int MAX_BUFFERED_EVENTS = 3_000;
    private static final Logger LOGGER = LoggerFactory.getLogger(EventExecutor.class);

    final DelayQueue<RunnableExperimentEvent> runnable_events;
    private final ExecutorService task_populator;
    private final ExecutorService task_scheduler;
    final ThreadPoolExecutor task_executor;
    private final Semaphore load_balancer;
    final EventQueue event_reader;
    private final Path observations_home;
    private final MetricRegistry metric_registry;
    private final CsvReporter csv_reporter;
    private final Future<Void> task_populator_future;
    private final Scenario scenario;
    private final int lookup_retry_count;
    private final TromboneMetricSet metric_set;
    private Future<Void> task_scheduler_future;
    private long start_time;

    public EventExecutor(final EventQueue reader, Path observations_home) {

        event_reader = reader;
        this.observations_home = observations_home;
        runnable_events = new DelayQueue<RunnableExperimentEvent>();
        task_populator = Executors.newSingleThreadExecutor(new NamedThreadFactory("task_populator_"));
        task_scheduler = Executors.newSingleThreadExecutor(new NamedThreadFactory("task_scheduler_"));
        task_executor = new ThreadPoolExecutor(100, 100, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new NamedThreadFactory("task_executor_"));
        task_executor.prestartAllCoreThreads();
        load_balancer = new Semaphore(MAX_BUFFERED_EVENTS, true);
        scenario = reader.getScenario();
        lookup_retry_count = getLookupRetryCount();
        metric_registry = new MetricRegistry("test");
        metric_set = new TromboneMetricSet(this);
        metric_registry.registerAll(metric_set);

        csv_reporter = new CsvReporter(metric_registry, observations_home);
        csv_reporter.setUseCountAsTimestamp(true);
        task_populator_future = startTaskQueuePopulator();
    }

    private Duration getObservationInterval() {

        return scenario.getObservationInterval();
    }

    public Duration getExperimentDuration() {

        return scenario.getExperimentDuration();
    }

    private int getLookupRetryCount() {

        return scenario.getLookupRetryCount();
    }

    public long getCurrentTimeInNanos() {

        return System.nanoTime() - start_time;
    }

    public synchronized void start() {

        if (!isStarted()) {

            task_scheduler_future = task_scheduler.submit(new Callable<Void>() {

                @Override
                public Void call() throws Exception {

                    LOGGER.info("starting event execution...");
                    start_time = System.nanoTime();

                    final Duration observation_interval = getObservationInterval();
                    csv_reporter.start(observation_interval.getLength(), observation_interval.getTimeUnit());
                    try {
                        while (!Thread.currentThread()
                                .isInterrupted() && !task_populator_future.isDone() || !runnable_events.isEmpty()) {
                            final RunnableExperimentEvent runnable = runnable_events.take();

                            task_executor.execute(runnable);

                            metric_set.event_scheduling_rate.mark();
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

        Map<String, List<EvaluatedDisseminationStrategy>> node_strategies = new HashMap<>();

        for (Participant participant : event_reader.getParticipants()) {
            final Peer peer = participant.getPeer();
            final Maintenance maintenance = peer.getMaintenance();
            if (maintenance instanceof EvolutionaryMaintenance) {
                EvolutionaryMaintenance evolutionary_maintainer = (EvolutionaryMaintenance) maintenance;
                node_strategies.put(peer.getKey()
                        .toString(), evolutionary_maintainer.getEvaluatedStrategies());
            }

            try {
                peer.unexpose();
            }
            catch (Exception e) {
                LOGGER.debug("failed to unexpose peer {} due to {}", peer, e);
            }
        }

        final JSONObject strategies_json = new JSONObject(node_strategies);
        try {
            FileUtils.write(observations_home.resolve("evaluated_strategies_per_peer.json")
                    .toFile(), strategies_json.toString(4), StandardCharsets.UTF_8, false);
        }
        catch (Exception e) {
            LOGGER.error("failed to save evaluated strategies per peer", e);
            LOGGER.error("Evaluated strategies per peer {}", strategies_json);
        }

        LOGGER.info("shutting down maintenance scheduler...");
        //TODO fix
        //MaintenanceFactory.SCHEDULER.shutdownNow();
        //        LOGGER.info("shutting down peer client factory...");
        //        PeerClientFactory.shutdownPeerClientFactory();
        //        LOGGER.info("shutting down peer server factory...");
        //        PeerServerFactory.shutdownPeerServerFactory();
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
        runnable_events.add(new RunnableLookupAsyncEvent(peer, event));
    }

    private Future<Void> startTaskQueuePopulator() {

        return task_populator.submit(new Callable<Void>() {

            @Override
            public Void call() throws Exception {

                try {
                    while (!Thread.currentThread()
                            .isInterrupted() && event_reader.hasNext()) {

                        load_balancer.acquire();
                        queueNextEvent();
                    }
                }
                catch (Throwable e) {
                    LOGGER.error("failure occurred while queuing events ", e);
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
        final Participant participant = event.getSource();
        final Peer peer = participant.getPeer();
        queue(peer, event);
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

            metric_set.event_execution_lag_sampler.update(getCurrentTimeInNanos() - event.getTimeInNanos());
            final Timer.Time time = metric_set.event_execution_duration_timer.time();
            try {
                handleEvent();
            }
            finally {
                time.stop();
                metric_set.event_completion_rate.mark();
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
                if (!peer.isExposed()) {
                    final boolean successfully_exposed = peer.expose();
                    if (successfully_exposed) {
                        metric_set.peer_arrival_rate.mark();
                        metric_set.available_peer_counter.increment();
                    }
                    else {
                        logger.warn("exposure of peer {} was unsuccessful", peer);
                    }

                    try {
                        join(peer, join_event.getKnownPeerReferences()).get();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            catch (final Exception e) {
                logger.warn("failed to expose peer {} on address {}", peer, peer.getAddress());
                logger.error("failure occurred when executing join event", e);
            }
        }

        private CompletableFuture<Void> join(final Peer peer, final Set<PeerReference> known_members) {

            assert known_members != null;

            final CompletableFuture<Void> future_join = new CompletableFuture<>();
            if (known_members.isEmpty()) {
                future_join.complete(null);
            }
            else {
                join(future_join, peer, known_members.iterator());
            }

            return future_join;
        }

        private void join(CompletableFuture<Void> future_join, final Peer peer, final Iterator<PeerReference> known_members) {

            assert known_members.hasNext();

            final PeerReference known_peer = known_members.next();
            final CompletableFuture<Void> join_trial = peer.join(known_peer);

            join_trial.whenComplete((success, error) -> {
                if (join_trial.isCompletedExceptionally()) {
                    if (known_members.hasNext()) {
                        join(future_join, peer, known_members);
                    }
                    else {
                        future_join.completeExceptionally(error);
                        error.printStackTrace();
                        metric_set.join_failure_rate.mark();
                    }
                }
                else {
                    future_join.complete(null); // void future.
                    metric_set.join_success_rate.mark();
                }
            });
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
                    metric_set.peer_departure_rate.mark();
                    metric_set.available_peer_counter.decrement();
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

                final PeerReference expected_result = event.getExpectedResult();
                final PeerMetric.LookupMeasurement measurement = peer.lookupWithRetry(event.getTarget(), lookup_retry_count, expected_result)
                        .get();
                final long hop_count = measurement.getHopCount();
                final long retry_count = measurement.getRetryCount();
                final long duration_in_nanos = measurement.getDurationInNanos();

                if (measurement.isDoneInError()) {
                    metric_set.lookup_failure_rate.mark();
                    metric_set.lookup_failure_hop_count_sampler.update(hop_count);
                    metric_set.lookup_failure_retry_count_sampler.update(retry_count);
                    metric_set.lookup_failure_delay_timer.update(duration_in_nanos, TimeUnit.NANOSECONDS);
                }
                else if (measurement.getResult()
                        .equals(expected_result)) {
                    metric_set.lookup_correctness_rate.mark();
                    metric_set.lookup_correctness_hop_count_sampler.update(hop_count);
                    metric_set.lookup_correctness_retry_count_sampler.update(retry_count);
                    metric_set.lookup_correctness_delay_timer.update(duration_in_nanos, TimeUnit.NANOSECONDS);
                }
                else {
                    metric_set.lookup_incorrectness_rate.mark();
                    metric_set.lookup_incorrectness_hop_count_sampler.update(hop_count);
                    metric_set.lookup_incorrectness_retry_count_sampler.update(retry_count);
                    metric_set.lookup_incorrectness_delay_timer.update(duration_in_nanos, TimeUnit.NANOSECONDS);
                }
            }
            catch (final Throwable e) {
                logger.error("failure occurred when executing lookup", e);
            }
            finally {
                metric_set.lookup_execution_rate.mark();
            }
        }
    }

    private class RunnableLookupAsyncEvent extends RunnableExperimentEvent {

        private final Logger logger = LoggerFactory.getLogger(RunnableLookupEvent.class);

        private RunnableLookupAsyncEvent(Peer peer, final LookupEvent event) {

            super(peer, event);
        }

        @Override
        public void handleEvent() {

            final LookupEvent event = (LookupEvent) getEvent();
            final PeerReference expected_result = event.getExpectedResult();
            final CompletionStage<PeerMetric.LookupMeasurement> async_lookup = peer.lookupWithRetry(event.getTarget(), lookup_retry_count, expected_result);

            async_lookup.whenCompleteAsync((measurement, error) -> {

                final boolean looked_up = error == null;
                if (looked_up) {
                    metric_set.lookup_execution_rate.mark();
                    final long hop_count = measurement.getHopCount();
                    final long retry_count = measurement.getRetryCount();
                    final long duration_in_nanos = measurement.getDurationInNanos();

                    if (measurement.isDoneInError()) {
                        metric_set.lookup_failure_rate.mark();
                        metric_set.lookup_failure_hop_count_sampler.update(hop_count);
                        metric_set.lookup_failure_retry_count_sampler.update(retry_count);
                        metric_set.lookup_failure_delay_timer.update(duration_in_nanos, TimeUnit.NANOSECONDS);
                    }
                    else if (measurement.getResult()
                            .getKey()
                            .equals(expected_result.getKey())) {
                        metric_set.lookup_correctness_rate.mark();
                        metric_set.lookup_correctness_hop_count_sampler.update(hop_count);
                        metric_set.lookup_correctness_retry_count_sampler.update(retry_count);
                        metric_set.lookup_correctness_delay_timer.update(duration_in_nanos, TimeUnit.NANOSECONDS);
                    }
                    else {
                        metric_set.lookup_incorrectness_rate.mark();
                        metric_set.lookup_incorrectness_hop_count_sampler.update(hop_count);
                        metric_set.lookup_incorrectness_retry_count_sampler.update(retry_count);
                        metric_set.lookup_incorrectness_delay_timer.update(duration_in_nanos, TimeUnit.NANOSECONDS);
                    }
                }
                else {
                    logger.error("failure occurred when executing lookup", error);
                }
            }, task_executor);
        }
    }
}
