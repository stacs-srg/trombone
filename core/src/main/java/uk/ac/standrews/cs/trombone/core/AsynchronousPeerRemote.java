package uk.ac.standrews.cs.trombone.core;

import java.util.List;
import java.util.concurrent.CompletionStage;
import uk.ac.standrews.cs.trombone.core.key.Key;
import uk.ac.standrews.cs.trombone.core.selector.Selector;

/**
 * The asynchronous remote operations.
 *
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public interface AsynchronousPeerRemote {

    CompletionStage<Key> getKey();

    CompletionStage<Void> join(PeerReference member);

    CompletionStage<Void> push(List<PeerReference> references);

    CompletionStage<Void> push(PeerReference reference);

    CompletionStage<List<PeerReference>> pull(Selector selector);

    CompletionStage<PeerReference> lookup(Key target);

    CompletionStage<PeerReference> nextHop(Key target);
}
