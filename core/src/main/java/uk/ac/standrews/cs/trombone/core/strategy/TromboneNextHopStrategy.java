package uk.ac.standrews.cs.trombone.core.strategy;

import java.util.concurrent.CompletableFuture;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerReference;
import uk.ac.standrews.cs.trombone.core.RoutingState;
import uk.ac.standrews.cs.trombone.core.key.Key;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class TromboneNextHopStrategy implements NextHopStrategy {

    private final Peer local;
    private final RoutingState state;

    public TromboneNextHopStrategy(Peer local) {

        this.local = local;
        state = local.getPeerState();
    }

    @Override
    public CompletableFuture<PeerReference> apply(final Key target) {

        final PeerReference next_hop;
        final PeerReference self = local.getSelfReference();
        if (state.inLocalKeyRange(target)) {
            next_hop = self;
        }
        else {
            final PeerReference ceiling = state.closest(target);
            next_hop = ceiling != null ? ceiling : self;
        }
        return CompletableFuture.completedFuture(next_hop);
    }
}
