package uk.ac.standrews.cs.trombone.core;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.List;
import uk.ac.standrews.cs.trombone.core.key.Key;
import uk.ac.standrews.cs.trombone.core.selector.Selector;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public interface AsynchronousPeerRemote {

    ListenableFuture<Key> getKey();

    ListenableFuture<Void> join(PeerReference member);

    ListenableFuture<Void> push(List<PeerReference> references);

    ListenableFuture<Void> push(PeerReference reference);

    ListenableFuture<List<PeerReference>> pull(Selector selector);

    ListenableFuture<PeerReference> lookup(Key target);

    ListenableFuture<PeerReference> nextHop(Key target);
}
