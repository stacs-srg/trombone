package uk.ac.standrews.cs.trombone.trombone;

import org.mashti.jetson.exception.RPCException;
import uk.ac.standrews.cs.trombone.trombone.key.Key;
import uk.ac.standrews.cs.trombone.trombone.selector.Selector;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public interface PeerRemote {

    Key getKey() throws RPCException;

    void join(PeerReference member) throws RPCException;

    void push(PeerReference reference) throws RPCException;

    void push(PeerReference[] reference) throws RPCException;

    PeerReference[] pull(Selector selector, int size) throws RPCException;

    PeerReference lookup(Key target) throws RPCException;

    PeerReference nextHop(Key target) throws RPCException;
}
