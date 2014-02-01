package uk.ac.standrews.cs.trombone.core;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.mashti.gauge.Counter;
import org.mashti.gauge.Metric;
import org.mashti.gauge.Rate;
import org.mashti.gauge.Sampler;
import org.mashti.gauge.Timer;
import org.mashti.jetson.WrittenByteCountListener;
import org.mashti.jetson.exception.RPCException;
import org.mashti.sina.distribution.statistic.Statistics;
import uk.ac.standrews.cs.trombone.core.adaptation.EnvironmentSnapshot;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class PeerMetric implements Metric, WrittenByteCountListener {

    private static final Rate GLOBAL_SENT_BYTES_RATE = new Rate();
    private final Rate sent_bytes_rate;
    private final Timer lookup_success_delay_timer;
    private final Sampler lookup_success_hop_count_sampler;
    private final Sampler lookup_success_retry_count_sampler;
    private final Rate lookup_failure_rate;
    private final Counter lookup_counter;

    public PeerMetric(final Peer peer) {

        sent_bytes_rate = new Rate();
        lookup_success_delay_timer = new Timer();
        lookup_success_hop_count_sampler = new Sampler();
        lookup_success_retry_count_sampler = new Sampler();
        lookup_failure_rate = new Rate();
        lookup_counter = new Counter();
    }

    public static Rate getGlobalSentBytesRate() {

        return GLOBAL_SENT_BYTES_RATE;
    }

    public long getNumberOfExecutedLookups() {

        return lookup_counter.getAndReset();
    }

    public LookupMeasurement newLookupMeasurement(final int retry_count) {

        return new LookupMeasurement(retry_count);
    }

    public long getMeanLookupSuccessDelay(TimeUnit unit) {

        final Statistics stats = lookup_success_delay_timer.getAndReset();
        final long mean_delay_nanaos = stats.getMean().longValue();
        return unit.convert(mean_delay_nanaos, TimeUnit.NANOSECONDS);
    }

    public double getSentBytesRatePerSecond() {

        return sent_bytes_rate.getRateAndReset();
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

        return null;
    }

    public final class LookupMeasurement {

        private final int retry_threshold;
        private final Timer.Time time;
        private final AtomicBoolean done;
        private final AtomicInteger retry_count = new AtomicInteger();
        private final AtomicInteger hop_count = new AtomicInteger();
        private volatile PeerReference result;
        private volatile RPCException error;
        private long duration_in_nanos;

        private LookupMeasurement(int retry_threshold) {

            this.retry_threshold = retry_threshold;
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
                lookup_success_hop_count_sampler.update(hop_count.get());
                lookup_success_retry_count_sampler.update(retry_count.get());
                lookup_counter.increment();
            }
        }

        public synchronized void stop(RPCException error) {

            if (doneIfUndone()) {
                this.error = error;
                duration_in_nanos = time.stop();
                lookup_failure_rate.mark();
                lookup_counter.increment();
            }
        }

        public boolean isDone() {

            return done.get();
        }

        public boolean isDoneInError() {

            return isDone() && error != null;
        }

        public RPCException getError() {

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
