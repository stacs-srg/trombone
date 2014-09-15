package uk.ac.standrews.cs.trombone.core.selector;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerReference;
import uk.ac.standrews.cs.trombone.core.state.ChordPeerState;
import uk.ac.standrews.cs.trombone.core.state.PeerState;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class ChordSuccessorListSelector extends Selector {

    private static final long serialVersionUID = -795878334750777307L;
    private final First fallback_selector;

    public ChordSuccessorListSelector(final Integer size) {

        super(size);
        fallback_selector = new First(size);
    }

    @Override
    public CompletableFuture<List<PeerReference>> select(final Peer peer) {

        final PeerState state = peer.getPeerState();

        if (state instanceof ChordPeerState) {
            final ChordPeerState chord_state = (ChordPeerState) state;
            return CompletableFuture.completedFuture(chord_state.getSuccessorList()
                    .values());
        }
        else {
            return fallback_selector.select(peer);
        }

    }

    @Override
    public Selector copy() {

        return new ChordSuccessorListSelector(size);
    }
}
