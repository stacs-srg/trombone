package uk.ac.standrews.cs.trombone.core.gossip.selector;

import java.io.Serializable;
import org.mashti.jetson.exception.RPCException;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerReference;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public interface Selector extends Serializable {

    PeerReference[] select(Peer peer, int size) throws RPCException;
}
