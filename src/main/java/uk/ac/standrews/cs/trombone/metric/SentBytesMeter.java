package uk.ac.standrews.cs.trombone.metric;

import uk.ac.standrews.cs.trombone.metric.core.Gauge;
import uk.ac.standrews.cs.trombone.metric.core.Rate;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class SentBytesMeter extends Rate {

    private static final Rate TOTAL_SENT_BYTES_METER = new Rate();
    private static final Gauge<Double> SENT_BYTES_PER_EXPOSED_PEER_GAUGE = new Gauge<Double>() {

        @Override
        public Double get() {

            final long totoal_sent_bytes = TOTAL_SENT_BYTES_METER.getCount();
            final double exposed_peers_count = PeerExposureChangeMeter.EXPOSED_PEERS_COUNTER.get();
            return exposed_peers_count != 0 ? totoal_sent_bytes / exposed_peers_count : 0;
        }
    };

    public static Gauge<Double> getSentBytesPerExposedPeerGauge() {

        return SENT_BYTES_PER_EXPOSED_PEER_GAUGE;
    }

    public static Rate getTotalSentBytesMeter() {

        return TOTAL_SENT_BYTES_METER;
    }

    @Override
    public void mark(final long n) {

        try {
            super.mark(n);
        }
        finally {
            TOTAL_SENT_BYTES_METER.mark(n);
        }
    }
}
