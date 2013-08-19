package uk.ac.standrews.cs.trombone;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.mashti.gauge.Metric;
import org.mashti.gauge.Rate;
import org.mashti.gauge.Sampler;
import org.mashti.gauge.Timer;
import org.mashti.jetson.WrittenByteCountListenner;
import org.mashti.jetson.exception.RPCException;
import org.mashti.sina.distribution.statistic.Statistics;
import uk.ac.standrews.cs.trombone.metric.PeerExposureChangeMeter;
import uk.ac.standrews.cs.trombone.metric.PeerMembershipChangeMeter;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class PeerMetric implements Metric, WrittenByteCountListenner {

    private static final Rate GLOBAL_SENT_BYTES_RATE = new Rate();
    private final Rate sent_bytes_meter;
    private final Timer lookup_success_delay_timer;
    private final Sampler lookup_success_hop_count_histogram;
    private final Sampler lookup_success_retry_count_histogram;
    private final Rate lookup_failure_rate_meter;
    private final PeerMembershipChangeMeter membership_change_meter;
    private final PeerExposureChangeMeter exposure_change_meter;

    public PeerMetric(final Peer peer) {

        sent_bytes_meter = new Rate();
        lookup_success_delay_timer = new Timer();
        lookup_success_hop_count_histogram = new Sampler();
        lookup_success_retry_count_histogram = new Sampler();
        lookup_failure_rate_meter = new Rate();
        membership_change_meter = new PeerMembershipChangeMeter(peer);
        exposure_change_meter = new PeerExposureChangeMeter(peer);
    }

    public static Rate getGlobalSentBytesRate() {

        return GLOBAL_SENT_BYTES_RATE;
    }

    public Timer getLookupSuccessDelayTimer() {

        return lookup_success_delay_timer;
    }

    public LookupMeasurement newLookupMeasurement(final int retry_count) {

        return new LookupMeasurement(retry_count);
    }

    public long getMeanLookupSuccessDelay(TimeUnit unit) {

        final Statistics stats = lookup_success_delay_timer.getAndReset();
        final long mean_delay_nanaos = stats.getMean().longValue();
        return unit.convert(mean_delay_nanaos, TimeUnit.NANOSECONDS);
    }

    public long getSentBytes() {

        return sent_bytes_meter.getCount();
    }

    @Override
    public void notifyWrittenByteCount(final int byte_count) {

        sent_bytes_meter.mark(byte_count);
        GLOBAL_SENT_BYTES_RATE.mark(byte_count);
    }

    public final class LookupMeasurement {

        private final int retry_threshold;
        private final Timer.Time time;
        private final AtomicBoolean done;
        private volatile int retry_count;
        private volatile int hop_count;
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
                retry_count++;
            }
        }

        public synchronized void incrementHopCount() {

            if (!isDone()) {
                hop_count++;
            }
        }

        public synchronized void resetHopCount() {

            if (!isDone()) {
                hop_count = 0;
            }
        }

        public synchronized boolean hasRetryThresholdReached() {

            return retry_count > retry_threshold;
        }

        public synchronized void stop(PeerReference result) {

            if (doneIfUndone()) {
                this.result = result;
                duration_in_nanos = time.stop();
                lookup_success_hop_count_histogram.update(hop_count);
                lookup_success_retry_count_histogram.update(retry_count);
            }
        }

        public synchronized void stop(RPCException error) {

            if (doneIfUndone()) {
                this.error = error;
                duration_in_nanos = time.stop();
                lookup_failure_rate_meter.mark();
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

        public long getHopCout() {

            return hop_count;
        }

        public long getRetryCount() {

            return retry_count;
        }

        public long getDurationInNanos() {

            return duration_in_nanos;
        }

        private boolean doneIfUndone() {

            return done.compareAndSet(false, true);
        }
    }
}
