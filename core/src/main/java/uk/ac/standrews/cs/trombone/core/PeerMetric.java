package uk.ac.standrews.cs.trombone.core;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.math3.stat.descriptive.SynchronizedDescriptiveStatistics;
import org.mashti.gauge.Metric;
import org.mashti.gauge.Rate;
import org.mashti.gauge.Timer;
import org.mashti.jetson.WrittenByteCountListener;
import uk.ac.standrews.cs.trombone.core.adaptation.EnvironmentSnapshot;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class PeerMetric implements Metric, WrittenByteCountListener {

    private static final Rate GLOBAL_SENT_BYTES_RATE = new Rate();
    private static final Rate GLOBAL_RPC_ERROR_RATE = new Rate();
    private final Rate sent_bytes_rate;
    private final Timer lookup_success_delay_timer;
    private final Rate lookup_failure_rate;
    private final Rate lookup_counter;
    private final Rate served_next_hop_counter;
    private final Rate rpc_error_rate;
    private final boolean application_feedback_enabled;

    public PeerMetric(final boolean application_feedback_enabled) {

        this.application_feedback_enabled = application_feedback_enabled;

        sent_bytes_rate = new Rate();
        lookup_success_delay_timer = new Timer();
        lookup_failure_rate = new Rate();
        lookup_counter = new Rate();
        served_next_hop_counter = new Rate();
        rpc_error_rate = new Rate();
    }

    public static Rate getGlobalSentBytesRate() {

        return GLOBAL_SENT_BYTES_RATE;
    }

    public static Rate getGlobalRPCErrorRate() {

        return GLOBAL_RPC_ERROR_RATE;
    }

    public double getLookupExecutionRatePerSecond() {

        return lookup_counter.getRate();
    }

    public double getServedNextHopRatePerSecond() {

        return served_next_hop_counter.getRate();
    }

    public LookupMeasurement newLookupMeasurement(final int retry_count) {

        return new LookupMeasurement(retry_count);
    }

    public LookupMeasurement newLookupMeasurement(final int retry_count, PeerReference expected_result) {

        return new LookupMeasurement(retry_count, expected_result);
    }

    public long getMeanLookupSuccessDelay(TimeUnit unit) {

        final SynchronizedDescriptiveStatistics stats = lookup_success_delay_timer.getAndReset();
        final long mean_delay_nanaos = (long) stats.getMean();
        return unit.convert(mean_delay_nanaos, TimeUnit.NANOSECONDS);
    }

    public double getSentBytesRatePerSecond() {

        return sent_bytes_rate.getRate();
    }

    public double getLookupFailureRate() {

        return lookup_failure_rate.getRate();
    }

    @Override
    public void notifyWrittenByteCount(final int byte_count) {

        sent_bytes_rate.mark(byte_count);
        GLOBAL_SENT_BYTES_RATE.mark(byte_count);
    }

    public EnvironmentSnapshot getSnapshot() {

        return new EnvironmentSnapshot(this);
    }

    public double getRPCErrorRatePerSecond() {

        return rpc_error_rate.getRate();
    }

    void notifyRPCError(final Throwable error) {

        rpc_error_rate.mark();
        GLOBAL_RPC_ERROR_RATE.mark();
    }

    void notifyServe(final Method method) {

        final String method_name = method.getName();
        switch (method_name) {
            case "nextHop":
                served_next_hop_counter.mark();
                break;
            default:
                break;
        }
    }

    public final class LookupMeasurement {

        private final int retry_threshold;
        private final PeerReference expected_result;
        private final Timer.Time time;
        private final AtomicBoolean done;
        private final AtomicInteger retry_count = new AtomicInteger();
        private final AtomicInteger hop_count = new AtomicInteger();
        private volatile PeerReference result;
        private volatile Throwable error;
        private long duration_in_nanos;

        private LookupMeasurement(int retry_threshold) {

            this(retry_threshold, null);
        }

        public LookupMeasurement(final int retry_threshold, final PeerReference expected_result) {

            this.retry_threshold = retry_threshold;
            this.expected_result = expected_result;
            time = lookup_success_delay_timer.time();
            done = new AtomicBoolean();

        }

        public synchronized void incrementRetryCount() {

            if (!isDone()) {
                retry_count.getAndIncrement();
            }
        }

        public synchronized void incrementHopCount() {

            if (!isDone()) {
                hop_count.getAndIncrement();
            }
        }

        public synchronized void resetHopCount() {

            if (!isDone()) {
                hop_count.set(0);
            }
        }

        public synchronized boolean hasRetryThresholdReached() {

            return retry_count.get() >= retry_threshold;
        }

        public synchronized void stop(PeerReference result) {

            if (doneIfUndone()) {
                this.result = result;
                duration_in_nanos = time.stop();

                if (application_feedback_enabled && expected_result != null && !expected_result.equals(result)) {
                    lookup_failure_rate.mark();
                }
                lookup_counter.mark();
            }
        }

        public synchronized void stop(Throwable error) {

            if (doneIfUndone()) {
                this.error = error;
                duration_in_nanos = time.stop();
                lookup_failure_rate.mark();
                lookup_counter.mark();
            }
        }

        public boolean isDone() {

            return done.get();
        }

        public boolean isDoneInError() {

            return isDone() && error != null;
        }

        public Throwable getError() {

            return error;
        }

        public PeerReference getResult() {

            return result;
        }

        public long getHopCount() {

            return hop_count.get();
        }

        public long getRetryCount() {

            return retry_count.get();
        }

        public long getDurationInNanos() {

            return duration_in_nanos;
        }

        private boolean doneIfUndone() {

            return done.compareAndSet(false, true);
        }
    }
}
