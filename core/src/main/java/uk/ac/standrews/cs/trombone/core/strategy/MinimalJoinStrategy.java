package uk.ac.standrews.cs.trombone.core.strategy;

import java.util.concurrent.CompletableFuture;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerReference;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class MinimalJoinStrategy implements JoinStrategy {

    @Override
    public CompletableFuture<Void> join(Peer local, final PeerReference reference) {

        return local.push(reference);
    }
}
