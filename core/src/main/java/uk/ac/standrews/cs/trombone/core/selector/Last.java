package uk.ac.standrews.cs.trombone.core.selector;

import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerReference;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class Last implements Selector {

    public static final Last LAST_REACHABLE_SELECTOR_INSTANCE = new Last();
    private static final long serialVersionUID = -1251875528485423902L;

    private Last() {

    }

    public static Last getInstance() {

        return LAST_REACHABLE_SELECTOR_INSTANCE;
    }

    @Override
    public PeerReference[] select(final Peer peer){

        return new PeerReference[]{peer.getPeerState().last()};
    }
}
