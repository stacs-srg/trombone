package uk.ac.standrews.cs.trombone.core.selector;

import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerReference;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class LastReachable implements Selector {

    public static final LastReachable LAST_REACHABLE_SELECTOR_INSTANCE = new LastReachable();
    private static final long serialVersionUID = -2468932076649021046L;

    private LastReachable() {

    }

    public static LastReachable getInstance() {

        return LAST_REACHABLE_SELECTOR_INSTANCE;
    }

    @Override
    public PeerReference[] select(final Peer peer) {

        return new PeerReference[]{peer.getPeerState().lastReachable()};
    }
}
