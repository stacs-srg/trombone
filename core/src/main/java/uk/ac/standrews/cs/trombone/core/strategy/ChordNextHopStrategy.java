package uk.ac.standrews.cs.trombone.core.strategy;

import java.util.concurrent.CompletableFuture;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerReference;
import uk.ac.standrews.cs.trombone.core.RoutingState;
import uk.ac.standrews.cs.trombone.core.key.Key;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class ChordNextHopStrategy implements NextHopStrategy {

    private final Peer local;
    private final RoutingState local_state;
    private final Key local_key;

    public ChordNextHopStrategy(Peer local) {

        this.local = local;
        local_key = local.getKeySync();
        local_state = local.getPeerState();
    }

    @Override
    public CompletableFuture<PeerReference> apply(final Key target) {

        return CompletableFuture.supplyAsync(() -> {

            final PeerReference next_hop;
            final PeerReference successor = local_state.first();

            if (local_state.inLocalKeyRange(target)) {
                next_hop = local.getSelfReference();
            }
            else if (inSuccessorKeyRange(successor, target)) {
                next_hop = successor;
            }
            else {
                next_hop = local_state.closest(target);
            }

            //TODO discuss whether to fail or return successor.
            return next_hop != null ? next_hop : successor;
        }, local.getExecutor());
    }

    private boolean inSuccessorKeyRange(final PeerReference successor, final Key target) {

        final Key successor_key = successor.getKey();
        return local_key.equals(successor_key) || !local_key.equals(target) && local_key.compareRingDistance(target, successor_key) > 0;
    }
}
