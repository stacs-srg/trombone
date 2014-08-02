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
public final class ChordSuccessorSelector extends Selector {

    private static final long serialVersionUID = 4945435011721991055L;
    private static final ChordSuccessorSelector CHORD_SUCCESSOR_SELECTOR = new ChordSuccessorSelector();
    private final First fallback_selector;

    public static ChordSuccessorSelector getInstance() {

        return CHORD_SUCCESSOR_SELECTOR;
    }

    private ChordSuccessorSelector() {

        super(1, ReachabilityCriteria.REACHABLE);
        fallback_selector = new First(size, reachability_criteria);
    }

    @Override
    public List<PeerReference> select(final Peer peer) {

        final PeerState state = peer.getPeerState();
        final List<PeerReference> selection;

        if (state instanceof ChordPeerState) {
            final ChordPeerState chord_state = (ChordPeerState) state;
            selection = new ArrayList<>();
            selection.add(chord_state.getSuccessor());
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
