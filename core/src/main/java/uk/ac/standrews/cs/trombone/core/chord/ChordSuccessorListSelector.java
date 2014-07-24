package uk.ac.standrews.cs.trombone.core.chord;

import java.util.List;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerReference;
import uk.ac.standrews.cs.trombone.core.RoutingState;
import uk.ac.standrews.cs.trombone.core.selector.First;
import uk.ac.standrews.cs.trombone.core.selector.Selector;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class ChordSuccessorListSelector extends Selector {

    private static final long serialVersionUID = -795878334750777307L;
    private final First fallback_selector;

    public ChordSuccessorListSelector(final int size) {

        super(size, ReachabilityCriteria.REACHABLE);
        fallback_selector = new First(size, reachability_criteria);
    }

    @Override
    public List<PeerReference> select(final Peer peer) {

        final RoutingState state = peer.getPeerState();
        final List<PeerReference> selection;

        if (state instanceof ChordPeerState) {
            final ChordPeerState chord_state = (ChordPeerState) state;
            selection = chord_state.getSuccessorList().values();
        }
        else {
            selection = fallback_selector.select(peer);
        }

        return selection;
    }

    @Override
    public Selector copy() {

        return new ChordSuccessorListSelector(size);
    }
}
