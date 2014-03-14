package uk.ac.standrews.cs.trombone.core.selector;

import java.io.Serializable;
import java.util.List;
import org.mashti.jetson.exception.RPCException;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerReference;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public interface Selector extends Serializable {

    List<PeerReference> select(Peer peer) throws RPCException;
}
