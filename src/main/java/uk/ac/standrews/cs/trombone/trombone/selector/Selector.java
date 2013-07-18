package uk.ac.standrews.cs.trombone.trombone.selector;

import org.mashti.jetson.exception.RPCException;
import java.io.Serializable;
import uk.ac.standrews.cs.trombone.trombone.Peer;
import uk.ac.standrews.cs.trombone.trombone.PeerReference;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public interface Selector extends Serializable {

    PeerReference[] select(Peer peer, int size) throws RPCException;
}
