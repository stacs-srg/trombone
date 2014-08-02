package uk.ac.standrews.cs.trombone.core.strategy;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import uk.ac.standrews.cs.trombone.core.NextHopReference;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerReference;
import uk.ac.standrews.cs.trombone.core.key.Key;
import uk.ac.standrews.cs.trombone.core.state.PeerState;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class TromboneNextHopStrategy implements NextHopStrategy {

    private final Peer local;
    private final PeerState state;
    private final ScheduledExecutorService executor;

    public TromboneNextHopStrategy(Peer local) {

        this.local = local;
        state = local.getPeerState();
        executor = local.getExecutor();
    }

    @Override
    public CompletableFuture<NextHopReference> apply(final Key target) {

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
