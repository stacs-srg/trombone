package uk.ac.standrews.cs.trombone.core.selector;

import java.util.List;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerReference;
import uk.ac.standrews.cs.trombone.core.PeerState;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class MostRecentlySeen extends Selector {

    private static final long serialVersionUID = -4103050337428351524L;

    public MostRecentlySeen(int size, ReachabilityCriteria reachability_criteria) {

        super(size, reachability_criteria);
    }

    @Override
    public List<PeerReference> select(final Peer peer) {

        final PeerState state = peer.getPeerState();
        return state.mostRecentlySeen(size);
    }

    @Override
    public Selector copy() {

        return new MostRecentlySeen(size, reachability_criteria);
    }
}
