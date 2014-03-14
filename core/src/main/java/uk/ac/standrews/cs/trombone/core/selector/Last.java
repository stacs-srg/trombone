package uk.ac.standrews.cs.trombone.core.selector;

import java.util.List;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerReference;
import uk.ac.standrews.cs.trombone.core.PeerState;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class Last implements Selector {

    private static final long serialVersionUID = 4969898731774717311L;
    private final int size;
    private final boolean reachable;

    public Last(int size, boolean reachable) {

        this.size = size;
        this.reachable = reachable;
    }

    @Override
    public List<PeerReference> select(final Peer peer) {

        final PeerState state = peer.getPeerState();
        return reachable ? state.lastReachable(size) : state.last(size);
    }
}
