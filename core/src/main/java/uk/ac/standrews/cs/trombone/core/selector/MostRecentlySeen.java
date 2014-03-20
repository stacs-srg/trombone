package uk.ac.standrews.cs.trombone.core.selector;

import java.util.List;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerReference;
import uk.ac.standrews.cs.trombone.core.PeerState;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class MostRecentlySeen implements Selector {

    private static final long serialVersionUID = -4103050337428351524L;
    private final int size;

    public MostRecentlySeen(int size) {

        this.size = size;
    }

    @Override
    public List<PeerReference> select(final Peer peer) {

        final PeerState state = peer.getPeerState();
        return state.mostRecentlySeen(size);
    }
}
