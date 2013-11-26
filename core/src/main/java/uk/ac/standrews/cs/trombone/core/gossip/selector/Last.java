package uk.ac.standrews.cs.trombone.core.gossip.selector;

import org.mashti.jetson.exception.RPCException;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerReference;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class Last implements Selector {

    public static final Last LAST_SELECTOR_INSTANCE = new Last();

    private Last() {

    }

    public static Last getInstance() {

        return LAST_SELECTOR_INSTANCE;
    }

    @Override
    public PeerReference[] select(final Peer peer, final int size) throws RPCException {

        return peer.getPeerState().bottom(size);
    }
}
