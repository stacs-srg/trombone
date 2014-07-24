package uk.ac.standrews.cs.trombone.core.strategy;

import java.util.concurrent.CompletableFuture;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerReference;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class MinimalJoinStrategy implements JoinStrategy {

    private final Peer local;

    public MinimalJoinStrategy(Peer local) {

        this.local = local;
    }

    @Override
    public CompletableFuture<Void> apply(final PeerReference reference) {

        return local.push(reference);
    }
}
