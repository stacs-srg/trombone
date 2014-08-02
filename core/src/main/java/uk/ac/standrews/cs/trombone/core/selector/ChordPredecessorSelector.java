package uk.ac.standrews.cs.trombone.core.selector;

import java.util.ArrayList;
import java.util.List;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerReference;
import uk.ac.standrews.cs.trombone.core.state.ChordPeerState;
import uk.ac.standrews.cs.trombone.core.state.PeerState;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class ChordPredecessorSelector extends Selector {

    private static final long serialVersionUID = -4258517625696259558L;
    private static final ChordPredecessorSelector CHORD_PREDECESSOR_SELECTOR = new ChordPredecessorSelector();
    private final Last fallback_selector;

    public static ChordPredecessorSelector getInstance() {

        return CHORD_PREDECESSOR_SELECTOR;
    }

    private ChordPredecessorSelector() {

        super(1, ReachabilityCriteria.REACHABLE);
        fallback_selector = new Last(size, reachability_criteria);
    }

    @Override
    public List<PeerReference> select(final Peer peer) {

        final PeerState state = peer.getPeerState();
        final List<PeerReference> selection;

        if (state instanceof ChordPeerState) {
            final ChordPeerState chord_state = (ChordPeerState) state;
            selection = new ArrayList<>();
            selection.add(chord_state.getPredecessor());
        }
        else {
            selection = fallback_selector.select(peer);
        }

        return selection;
    }

    @Override
    public Selector copy() {

        return getInstance();
    }

    @Override
    public boolean isSingleton() {

        return true;
    }
}
