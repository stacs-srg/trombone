package uk.ac.standrews.cs.trombone.core.strategy;

import java.util.concurrent.CompletableFuture;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerReference;
import uk.ac.standrews.cs.trombone.core.state.ChordPeerState;
import uk.ac.standrews.cs.trombone.core.state.PeerState;

/**
 * Implements Chord's join protocol.
 *
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class ChordJoinStrategy implements JoinStrategy {

    public static final CompletableFuture<Void> DONE = CompletableFuture.completedFuture(null);
    private final Peer local;
    private final PeerState local_state;

    public ChordJoinStrategy(Peer local) {

        this.local = local;
        local_state = local.getPeerState();
    }

    @Override
    public CompletableFuture<Void> apply(final PeerReference member) {

        final PeerReference self = local.getSelfReference();
        if (!self.equals(member)) {

            return local.getAsynchronousRemote(member)
                    .lookup(self.getKey())
                    .thenAccept(potential_successor -> {

                        if (!self.equals(potential_successor)) {

                            if (local_state instanceof ChordPeerState) {
                                ChordPeerState chord_state = (ChordPeerState) local_state;
                                chord_state.setSuccessor(potential_successor);
                            }
                            else {
                                local.push(potential_successor);
                            }
                        }
                    });
        }
        return DONE;
    }
}
