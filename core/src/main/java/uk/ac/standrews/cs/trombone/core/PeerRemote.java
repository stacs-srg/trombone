package uk.ac.standrews.cs.trombone.core;

import java.util.List;
import org.mashti.jetson.exception.RPCException;
import uk.ac.standrews.cs.trombone.core.key.Key;
import uk.ac.standrews.cs.trombone.core.selector.Selector;

/**
 * Presents the remote operations of a peer.
 *
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public interface PeerRemote {

    /**
     * Gets the identifier of this peer.
     *
     * @return the identifier of this peer
     * @throws RPCException if an error occurs while performing the remote operation
     */
    Key getKey() throws RPCException;

    /**
     * Joins this peer to a network via the given {@code member}.
     *
     * @param member a member of a network.
     * @throws RPCException if an error occurs while performing the remote operation
     */
    void join(PeerReference member) throws RPCException;

    /**
     * Pushes a given list of {@code references} to this peer.
     *
     * @param references the references to be pushed to this peer
     * @throws RPCException if an error occurs while performing the remote operation
     */
    void push(List<PeerReference> references) throws RPCException;

    /**
     * Pushes the given {@code reference} to this peer.
     *
     * @param reference the reference to be pushed to this peer
     * @throws RPCException if an error occurs while performing the remote operation
     */
    void push(PeerReference reference) throws RPCException;

    /**
     * Pulls a list of references from the state of this peer that are selected using the given {@code selector}
     *
     * @param selector the selector of state to be pulled
     * @return a list of references from the state of this peer that are selected using the given {@code selector}
     * @throws RPCException if an error occurs while performing the remote operation
     */
    List<PeerReference> pull(Selector selector) throws RPCException;

    /**
     * Finds the successor of the given {@code target} key using local and remote information.
     *
     * @param target the key of which to lookup the successor
     * @return the successor of the given {@code target} key
     * @throws RPCException if an error occurs while performing the remote operation
     */
    PeerReference lookup(Key target) throws RPCException;

    /**
     * Finds the successor of the given {@code target} key using only the local state of this peer.
     *
     * @param target the key of which to get the successor
     * @return the successor of the given {@code target} key in the local state of this peer
     * @throws RPCException if an error occurs while performing the remote operation
     */
    PeerReference nextHop(Key target) throws RPCException;
}
