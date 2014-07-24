
package uk.ac.standrews.cs.trombone.core.strategy;

import java.util.concurrent.CompletableFuture;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerReference;
import uk.ac.standrews.cs.trombone.core.RoutingState;
import uk.ac.standrews.cs.trombone.core.chord.ChordPeerState;

/**
 * Implements Chord's join protocol.
 *
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class ChordJoinStrategy implements JoinStrategy {

    private final Peer local;

    public ChordJoinStrategy(Peer local) {

        this.local = local;
    }

    @Override
    public CompletableFuture<Void> apply(final PeerReference member) {

        final PeerReference self = local.getSelfReference();

        if (!self.equals(member)) {

            return local.getAsynchronousRemote(member).lookup(self.getKey()).thenAccept(potential_successor -> {

                if (!self.equals(potential_successor)) {

                    final RoutingState peerState = local.getPeerState();
                    if (peerState instanceof ChordPeerState) {
                        ChordPeerState chord_state = (ChordPeerState) peerState;
                        chord_state.setSuccessor(potential_successor);
                    }
                    else {
                        local.push(potential_successor);
                    }
                }
            });
        }
        return CompletableFuture.completedFuture(null);
    }
}
