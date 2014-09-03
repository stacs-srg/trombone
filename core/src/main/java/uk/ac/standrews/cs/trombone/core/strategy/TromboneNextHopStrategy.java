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
public class TromboneNextHopStrategy implements NextHopStrategy {

    @Override
    public CompletableFuture<NextHopReference> nextHop(final Peer local, final Key target) {

        ;
        final PeerState state = local.getPeerState();
        final ScheduledExecutorService executor = local.getExecutor();

        return CompletableFuture.supplyAsync(() -> {

            final NextHopReference next_hop;
            final PeerReference self = local.getSelfReference();

            if (state.inLocalKeyRange(target)) {
                next_hop = new NextHopReference(self, true);
            }
            else {
                final PeerReference closest = state.closest(target);
                next_hop = new NextHopReference(closest, false);
            }

            return next_hop;

        }, executor);
    }
}
