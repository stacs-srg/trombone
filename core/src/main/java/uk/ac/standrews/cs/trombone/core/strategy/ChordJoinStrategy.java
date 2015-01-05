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

    @Override
    public CompletableFuture<Void> join(final Peer local, final PeerReference member) {

        final PeerState local_state = local.getPeerState();
        final PeerReference self = local.getSelfReference();

        if (!self.equals(member)) {

            return local.getAsynchronousRemote(member, false)
                    .lookup(local.key())
                    .thenAccept(potential_successor -> {

                        if (!self.equals(potential_successor)) {

                            if (local_state instanceof ChordPeerState) {
                                ChordPeerState chord_state = (ChordPeerState) local_state;
                                chord_state.setSuccessor(potential_successor);
                            }
                            local_state.add(potential_successor);
                            local_state.add(member);
                        }
                    });
        }
        return DONE;
    }
}
