package uk.ac.standrews.cs.trombone.core.selector;

import java.util.List;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerReference;
import uk.ac.standrews.cs.trombone.core.PeerState;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class First implements Selector {

    private static final long serialVersionUID = 5801175705134547771L;
    private final int size;
    private final boolean reachable;

    public First(int size, boolean reachable) {

        this.size = size;
        this.reachable = reachable;
    }

    @Override
    public List<PeerReference> select(final Peer peer) {

        final PeerState state = peer.getPeerState();
        return reachable ? state.firstReachable(size) : state.first(size);
    }
}
