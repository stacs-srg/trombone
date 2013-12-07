package uk.ac.standrews.cs.trombone.core.selector;

import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerReference;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class First implements Selector {

    public static final First FIRST_REACHABLE_SELECTOR_INSTANCE = new First();
    private static final long serialVersionUID = 5801175705134547771L;

    private First() {

    }

    public static First getInstance() {

        return FIRST_REACHABLE_SELECTOR_INSTANCE;
    }

    @Override
    public PeerReference[] select(final Peer peer) {

        return new PeerReference[] {peer.getPeerState().first()};
    }
}
