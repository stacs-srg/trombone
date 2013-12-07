package uk.ac.standrews.cs.trombone.core.selector;

import org.mashti.jetson.exception.RPCException;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerReference;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class FirstReachable implements Selector {

    public static final FirstReachable FIRST_REACHABLE_SELECTOR_INSTANCE = new FirstReachable();
    private static final long serialVersionUID = -8233374979336693751L;

    private FirstReachable() {

    }

    public static FirstReachable getInstance() {

        return FIRST_REACHABLE_SELECTOR_INSTANCE;
    }

    @Override
    public PeerReference[] select(final Peer peer) {

        return new PeerReference[] {peer.getPeerState().firstReachable()};
    }
}
