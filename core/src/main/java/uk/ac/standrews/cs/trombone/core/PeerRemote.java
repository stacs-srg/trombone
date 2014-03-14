package uk.ac.standrews.cs.trombone.core;

import java.util.List;
import org.mashti.jetson.exception.RPCException;
import uk.ac.standrews.cs.trombone.core.key.Key;
import uk.ac.standrews.cs.trombone.core.selector.Selector;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public interface PeerRemote {

    Key getKey() throws RPCException;

    void join(PeerReference member) throws RPCException;

    void push(List<PeerReference> references) throws RPCException;

    void push(PeerReference references) throws RPCException;

    List<PeerReference> pull(Selector selector) throws RPCException;

    PeerReference lookup(Key target) throws RPCException;

    PeerReference nextHop(Key target) throws RPCException;
}
