package uk.ac.standrews.cs.trombone.trombone;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.Timer;
import com.codahale.metrics.UniformReservoir;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.atomic.AtomicBoolean;
import org.mashti.jetson.WrittenByteCountListenner;
import org.mashti.jetson.exception.RPCException;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class PeerMetric implements Metric, WrittenByteCountListenner {

    private static final Meter PEER_ARRIVAL_METER = new Meter();
    private static final Meter PEER_DEPARTURE_METER = new Meter();
    private final PeerStateSizeGauge state_size_gauge;
    private final Meter sent_bytes_meter;
    private final Timer lookup_success_delay_timer;
    private final Histogram lookup_success_hop_count_histogram;
    private final Histogram lookup_success_retry_count_histogram;
    private final Meter lookup_failure_rate_meter;

    public PeerMetric(final Peer peer) {

        state_size_gauge = new PeerStateSizeGauge(peer);
        sent_bytes_meter = new Meter();
        lookup_success_delay_timer = new Timer(new UniformReservoir());
        lookup_success_hop_count_histogram = new Histogram(new UniformReservoir());
        lookup_success_retry_count_histogram = new Histogram(new UniformReservoir());
        lookup_failure_rate_meter = new Meter();

        peer.addMembershipChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(final PropertyChangeEvent event) {

                final Boolean arrived = (Boolean) event.getNewValue();
                (arrived ? PEER_ARRIVAL_METER : PEER_DEPARTURE_METER).mark();
            }
        });
    }

    public Timer getLookupSuccessDelayTimer() {

        return lookup_success_delay_timer;
    }

    public LookupMeasurement newLookupMeasurement(final int retry_count) {

        return new LookupMeasurement(retry_count);
    }

    @Override
    public void notifyWrittenByteCount(final int byte_count) {

        sent_bytes_meter.mark(byte_count);
    }

    private static final class PeerStateSizeGauge implements Gauge<Integer> {

        private final PeerState peer_state;

        private PeerStateSizeGauge(Peer peer) {

            peer_state = peer.getPeerState();
        }

        @Override
        public Integer getValue() {

            //TODO inefficient
            return peer_state.size();
        }
    }

    final class LookupMeasurement {

        private final int retry_threshold;
        private final Timer.Context time;
        private final AtomicBoolean done;
        private volatile int retry_count;
        private volatile int hop_count;
        private volatile PeerReference result;
        private volatile RPCException error;

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
                time.stop();
                lookup_success_hop_count_histogram.update(hop_count);
                lookup_success_retry_count_histogram.update(retry_count);
            }
        }

        public synchronized void stop(RPCException error) {

            if (doneIfUndone()) {
                this.error = error;
                time.stop();
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

        private boolean doneIfUndone() {

            return done.compareAndSet(false, true);
        }
    }
}
