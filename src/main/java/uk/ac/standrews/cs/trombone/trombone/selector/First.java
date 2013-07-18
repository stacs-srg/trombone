package uk.ac.standrews.cs.trombone.trombone.selector;

import org.mashti.jetson.exception.RPCException;
import uk.ac.standrews.cs.trombone.trombone.Peer;
import uk.ac.standrews.cs.trombone.trombone.PeerReference;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class First implements Selector {

    public static final First FIRST_SELECTOR_INSTANCE = new First();

    private First() {

    }

    public static First getInstance() {

        return FIRST_SELECTOR_INSTANCE;
    }

    @Override
    public PeerReference[] select(final Peer peer, final int size) throws RPCException {

        return peer.getPeerState().top(size);
    }
}
