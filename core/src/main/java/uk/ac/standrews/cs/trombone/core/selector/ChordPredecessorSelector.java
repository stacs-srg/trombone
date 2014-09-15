package uk.ac.standrews.cs.trombone.core.selector;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
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
    private static final Last FALLBACK_SELECTOR_LAST = new Last(1);

    public static ChordPredecessorSelector getInstance() {

        return CHORD_PREDECESSOR_SELECTOR;
    }

    private ChordPredecessorSelector() {

        super(1);
    }

    @Override
    public CompletableFuture<List<PeerReference>> select(final Peer peer) {

        final PeerState state = peer.getPeerState();
        if (state instanceof ChordPeerState) {
            final ChordPeerState chord_state = (ChordPeerState) state;
            final PeerReference predecessor = chord_state.getPredecessor();
            return CompletableFuture.completedFuture(Collections.singletonList(predecessor));
        }
        else {
            return FALLBACK_SELECTOR_LAST.select(peer);
        }

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
