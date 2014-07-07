package uk.ac.standrews.cs.trombone.core;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import uk.ac.standrews.cs.trombone.core.key.Key;
import uk.ac.standrews.cs.trombone.core.selector.Selector;

/**
 * The asynchronous remote operations.
 *
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public interface AsynchronousPeerRemote {

    CompletableFuture<Key> getKey();

    CompletableFuture<Void> join(PeerReference member);

    CompletableFuture<Void> push(List<PeerReference> references);

    CompletableFuture<Void> push(PeerReference reference);

    CompletableFuture<List<PeerReference>> pull(Selector selector);

    CompletableFuture<PeerReference> lookup(Key target);

    CompletableFuture<PeerReference> nextHop(Key target);
}
