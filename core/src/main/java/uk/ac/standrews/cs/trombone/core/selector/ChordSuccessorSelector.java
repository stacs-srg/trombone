package uk.ac.standrews.cs.trombone.core.selector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
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
    private static final First FALLBACK_SELECTOR_FIRST = new First(1);

    public static ChordSuccessorSelector getInstance() {

        return CHORD_SUCCESSOR_SELECTOR;
    }

    private ChordSuccessorSelector() {

        super(1);
    }

    @Override
    public CompletableFuture<List<PeerReference>> select(final Peer peer) {

        final PeerState state = peer.getPeerState();

        if (state instanceof ChordPeerState) {
            final ChordPeerState chord_state = (ChordPeerState) state;
            final List<PeerReference> selection = new ArrayList<>();
            selection.add(chord_state.getSuccessor());
            return CompletableFuture.completedFuture(selection);
        }
        return FALLBACK_SELECTOR_FIRST.select(peer);
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
