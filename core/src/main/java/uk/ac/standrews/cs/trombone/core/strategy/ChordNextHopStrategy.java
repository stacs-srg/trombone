package uk.ac.standrews.cs.trombone.core.strategy;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import uk.ac.standrews.cs.trombone.core.Key;
import uk.ac.standrews.cs.trombone.core.NextHopReference;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerReference;
import uk.ac.standrews.cs.trombone.core.state.PeerState;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class ChordNextHopStrategy implements NextHopStrategy {

    @Override
    public CompletableFuture<NextHopReference> nextHop(final Peer local, final Key target) {

        final PeerState local_state = local.getPeerState();
        final Key local_key = local.key();
        final ScheduledExecutorService executor = local.getExecutor();

        return CompletableFuture.supplyAsync(() -> {
            final NextHopReference next_hop;
            final PeerReference successor = local_state.first();

            if (local_state.inLocalKeyRange(target)) {
                next_hop = new NextHopReference(local.getSelfReference(), true);
            }
            else if (inSuccessorKeyRange(local_key, target, successor.getKey())) {
                next_hop = new NextHopReference(successor, true);
            }
            else {
                final PeerReference closest = local_state.closest(target);
                final boolean exact_hit = target.equals(closest.getKey());
                next_hop = new NextHopReference(closest, exact_hit);
            }

            return next_hop;

        }, executor);
    }

    private static boolean inSuccessorKeyRange(Key local_key, final Key target, Key successor_key) {

        return Key.inSegment(local_key, target, successor_key);
    }
}
