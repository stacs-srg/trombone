package uk.ac.standrews.cs.trombone.core.strategy;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import uk.ac.standrews.cs.trombone.core.NextHopReference;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerReference;
import uk.ac.standrews.cs.trombone.core.key.Key;
import uk.ac.standrews.cs.trombone.core.key.RingArithmetic;
import uk.ac.standrews.cs.trombone.core.state.PeerState;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class ChordNextHopStrategy implements NextHopStrategy {

    private final Peer local;
    private final PeerState local_state;
    private final Key local_key;
    private final ScheduledExecutorService executor;

    public ChordNextHopStrategy(Peer local) {

        this.local = local;
        local_key = local.key();
        local_state = local.getPeerState();
        executor = local.getExecutor();
    }

    @Override
    public CompletableFuture<NextHopReference> apply(final Key target) {

        return CompletableFuture.supplyAsync(() -> {
            final NextHopReference next_hop;
            final PeerReference successor = local_state.first();

            if (local_state.inLocalKeyRange(target)) {
                next_hop = new NextHopReference(local.getSelfReference(), true);
            }
            else if (inSuccessorKeyRange(successor, target)) {
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

    private boolean inSuccessorKeyRange(final PeerReference successor, final Key target) {

        final Key successor_key = successor.getKey();
        return RingArithmetic.inSegment(local_key, target, successor_key);
    }
}
