package uk.ac.standrews.cs.trombone.evaluation;

import au.com.bytecode.opencsv.CSVReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.mashti.jetson.exception.RPCException;
import uk.ac.standrews.cs.shabdiz.util.TimeoutExecutorService;
import uk.ac.standrews.cs.trombone.Peer;
import uk.ac.standrews.cs.trombone.PeerMetric;
import uk.ac.standrews.cs.trombone.PeerReference;
import uk.ac.standrews.cs.trombone.key.IntegerKey;
import uk.ac.standrews.cs.trombone.key.Key;
import uk.ac.standrews.cs.trombone.metric.core.Counter;
import uk.ac.standrews.cs.trombone.metric.core.Rate;
import uk.ac.standrews.cs.trombone.metric.core.Sampler;
import uk.ac.standrews.cs.trombone.metric.core.Timer;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class ExperimentEventExecutor {

    private final Rate lookup_execution_rate = new Rate();
    private final Rate lookup_failure_rate = new Rate();
    private final Rate lookup_correctness_rate = new Rate();
    private final Rate lookup_incorrectness_rate = new Rate();
    private final Counter available_peer_counter = new Counter();
    private final Rate peer_arrival_rate = new Rate();
    private final Rate peer_departure_rate = new Rate();
    private final Rate sent_bytes_rate = new Rate();
    private final Sampler event_execution_lag_sampler = new Sampler();
    private final Timer event_execution_duration_sampler = new Timer();
    private final Map<Integer, PeerReference> peers;
    private final DelayQueue<RunnableExperimentEvent> runnable_events;
    private final ExecutorService delayed_rannable_scheduler;
    private final ExecutorService delayed_rannable_executor;
    private final Semaphore semaphore;
    private final File churn_csv;
    private int lookup_retry_count;
    private Future<Object> task_scheduler_future;

    public ExperimentEventExecutor(File peers_csv, File churn_csv, File workload_csv) throws IOException {
        this.churn_csv = churn_csv;

        peers = loadPeers(peers_csv);
        runnable_events = new DelayQueue<RunnableExperimentEvent>();
        delayed_rannable_scheduler = Executors.newSingleThreadExecutor();
        delayed_rannable_executor = Executors.newCachedThreadPool();
        semaphore = new Semaphore(100, true);
    }

    public long getCurrentTimeInNanos() {

        return 0;
    }

    public synchronized void start() {
        if (!isStarted()) {
            task_scheduler_future = delayed_rannable_scheduler.submit(new Callable<Object>() {

                @Override
                public Object call() throws Exception {

                    while (!Thread.currentThread().isInterrupted()) {
                        final RunnableExperimentEvent runnable = runnable_events.take();
                        delayed_rannable_executor.execute(runnable);
                        semaphore.release();
                    }
                    return null;
                }
            });

            final Callable<Void> callable = new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    CSVReader reader = new CSVReader(new FileReader(churn_csv));
                    reader.readNext();
                    while (!Thread.currentThread().isInterrupted()) {
                        semaphore.acquire();
                        final String[] line = reader.readNext();
                        if (line != null) {
                            final long time = Long.valueOf(line[0]);
                            final PeerReference reference = peers.get(Integer.valueOf(line[1]));
                            final boolean available = Boolean.valueOf(line[2]);
                            AvailabilityChangeEvent event = new AvailabilityChangeEvent(null, time, available, 0L);
                            execute(event);
                        }
                    }
                    return null;
                }
            };
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

    void execute(final AvailabilityChangeEvent event) throws IOException {

        runnable_events.add(new RunnableAvailabilityChangeEvent(event));
    }

    void execute(final LookupEvent event) {

        runnable_events.add(new RunnableLookupEvent(event));
    }

    private Map<Integer, PeerReference> loadPeers(final File peers_csv) throws IOException {

        final Map<Integer, PeerReference> peers = new HashMap<Integer, PeerReference>();
        final CSVReader reader = new CSVReader(new FileReader(peers_csv));
        String[] line;
        reader.readNext();
        do {
            line = reader.readNext();
            final Integer index = Integer.valueOf(line[0]);
            final Key key = new IntegerKey(Integer.valueOf(line[1]));
            final InetSocketAddress address = new InetSocketAddress(line[2], Integer.valueOf(line[3]));
            peers.put(index, new PeerReference(key, address));
        } while (line != null);
        return peers;
    }

    private void joinWithTimeout(final long timeout_nanos) throws InterruptedException, ExecutionException, TimeoutException {
        TimeoutExecutorService.awaitCompletion(new Callable<Object>() {

            @Override
            public Object call() throws Exception {

                return null;
            }
        }, timeout_nanos, TimeUnit.NANOSECONDS);
    }

    Peer getPeerByReference(PeerReference reference) {

        return null;
    }

    private abstract class RunnableExperimentEvent implements Runnable, Delayed {

        private final ExperimentEvent event;

        private RunnableExperimentEvent(ExperimentEvent event) {

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

        protected ExperimentEvent getEvent() {

            return event;
        }
    }

    private class RunnableAvailabilityChangeEvent extends RunnableExperimentEvent {

        private RunnableAvailabilityChangeEvent(final AvailabilityChangeEvent event) {

            super(event);
        }

        @Override
        public void run() {

            final AvailabilityChangeEvent event = (AvailabilityChangeEvent) getEvent();
            event_execution_lag_sampler.update(getCurrentTimeInNanos() - event.getTimeInNanos());
            final PeerReference reference = event.getSource().getReference();
            final Peer peer = getPeerByReference(reference);
            final long availability_duration_nanos = event.getAvailabilityDurationInNanos();
            try {
                if (event.isAvailable()) {
                    peer.expose();
                    peer_arrival_rate.mark();
                    joinWithTimeout(availability_duration_nanos);
                }
                else {
                    peer.unexpose();
                    peer_departure_rate.mark();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class RunnableLookupEvent extends RunnableExperimentEvent {

        private RunnableLookupEvent(final LookupEvent event) {

            super(event);
        }

        @Override
        public void run() {

            final LookupEvent event = (LookupEvent) getEvent();
            event_execution_lag_sampler.update(getCurrentTimeInNanos() - event.getTimeInNanos());
            final Peer peer = getPeerByReference(event.getSource().getReference());

            try {
                final PeerMetric.LookupMeasurement measurement = peer.lookup(event.getTarget(), lookup_retry_count);
                final PeerReference expected_result = event.getExpectedResult().getReference();
                if (measurement.getResult().equals(expected_result)) {

                    lookup_correctness_rate.mark();
                }
                else {
                    lookup_incorrectness_rate.mark();
                }
                //FIXE implement hopcount & retrycount for correct and succeeded lookups
            }
            catch (RPCException e) {
                lookup_failure_rate.mark();
            }
            finally {
                lookup_execution_rate.mark();
            }
        }
    }
}
