package uk.ac.standrews.cs.trombone.metric;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import uk.ac.standrews.cs.shabdiz.util.HashCodeUtil;
import uk.ac.standrews.cs.trombone.Peer;
import uk.ac.standrews.cs.trombone.PeerState;
import uk.ac.standrews.cs.trombone.math.Statistics;
import uk.ac.standrews.cs.trombone.metric.core.Gauge;
import uk.ac.standrews.cs.trombone.metric.core.Sampler;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class PeerStateSizeGauge implements Gauge<Integer>, Comparable<PeerStateSizeGauge> {

    private static final Sampler TOTAL_PEER_STATE_SIZE_HISTOGRAM = new TotalPeerStateSizeHistogram();
    private static final Set<PeerStateSizeGauge> PEER_STATE_SIZE_GAUGES = new ConcurrentSkipListSet<PeerStateSizeGauge>();
    private final PeerState peer_state;
    private final Peer peer;

    public PeerStateSizeGauge(Peer peer) {

        this.peer = peer;
        peer_state = peer.getPeerState();
        PEER_STATE_SIZE_GAUGES.add(this);
    }

    public static Sampler getTotalPeerStateSizeHistogram() {

        return TOTAL_PEER_STATE_SIZE_HISTOGRAM;
    }

    public Peer getPeer() {

        return peer;
    }

    @Override
    public Integer get() {

        return peer_state.size();
    }

    @Override
    public int hashCode() {

        return HashCodeUtil.generate(peer.hashCode());
    }

    @Override
    public boolean equals(final Object other) {

        if (this == other) { return true; }
        if (!(other instanceof PeerStateSizeGauge)) { return false; }
        final PeerStateSizeGauge that = (PeerStateSizeGauge) other;
        return peer.equals(that.peer);
    }

    @Override
    public int compareTo(final PeerStateSizeGauge other) {

        return peer.getKey().compareTo(other.peer.getKey());
    }

    private static class TotalPeerStateSizeHistogram extends Sampler {

        @Override
        public Statistics getAndReset() {

            for (PeerStateSizeGauge guage : PEER_STATE_SIZE_GAUGES) {
                if (guage.getPeer().isExposed()) {
                    update(guage.get());
                }
            }

            return super.getAndReset();
        }
    }
}
