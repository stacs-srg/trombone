package uk.ac.standrews.cs.trombone.trombone.selector;

import org.mashti.jetson.exception.RPCException;
import uk.ac.standrews.cs.trombone.trombone.Peer;
import uk.ac.standrews.cs.trombone.trombone.PeerReference;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public final class Self implements Selector {

    private static final Self SELF_SELECTOR_INSTANCE = new Self();

    private Self() {

    }

    public static Self getInstance() {

        return SELF_SELECTOR_INSTANCE;
    }

    @Override
    public PeerReference[] select(final Peer peer, final int size) throws RPCException {

        return new PeerReference[]{peer.getSelfReference()};
    }
}
