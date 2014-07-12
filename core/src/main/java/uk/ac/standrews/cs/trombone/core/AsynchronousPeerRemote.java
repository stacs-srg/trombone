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

    /**
     * Gets the {@link Key key} of this peer.
     *
     * @return the future that will return the {@link Key key} of this peer if successful,
     */
    CompletableFuture<Key> getKey();

    /**
     * Joins this peer the the network of the given {@code member}.
     *
     * @param member a member of the network to which to join
     * @return the future that will complete normally if the join was successful, exceptionally otherwise.
     */
    CompletableFuture<Void> join(PeerReference member);

    /**
     * Pushes the knowledge of the given {@code references} to this peer.
     *
     * @param references the references to push to this peer
     * @return the future that will complete normally if the push was successful, exceptionally otherwise.
     */
    CompletableFuture<Void> push(List<PeerReference> references);

    /**
     * Pushes the knowledge of the given {@code reference} to this peer.
     *
     * @param reference the reference to push to this peer
     * @return the future that will complete normally if the push was successful, exceptionally otherwise.
     */
    CompletableFuture<Void> push(PeerReference reference);

    /**
     * Pulls a list of references from the state of this peer that are selected using the given {@code selector}.
     *
     * @param selector the selector that selects the state to be pulled
     * @return the future that will return the selected {@link PeerReference references} from the state of this peer if successful.
     */
    CompletableFuture<List<PeerReference>> pull(Selector selector);

    /**
     * Finds the successor of the given {@code target} key using local and remote information.
     *
     * @param target the key of which to lookup the successor
     * @return the future that will return the successor of the given {@code target} key
     */
    CompletableFuture<PeerReference> lookup(Key target);

    /**
     * Finds the successor of the given {@code target} key using only the local state of this peer.
     *
     * @param target the key of which to get the successor
     * @return the future that will return the successor of the given {@code target} key in the local state of this peer
     */
    CompletableFuture<PeerReference> nextHop(Key target);
}
