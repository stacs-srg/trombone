package uk.ac.standrews.cs.trombone.gossip.selector;

import uk.ac.standrews.cs.trombone.Peer;
import uk.ac.standrews.cs.trombone.PeerReference;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public final class Self implements Selector {

    private static final Self SELF_SELECTOR_INSTANCE = new Self();

    private Self() {

    }

    public static Self getInstance() {

        return SELF_SELECTOR_INSTANCE;
    }

    @Override
    public PeerReference[] select(final Peer peer, final int size) {

        final PeerReference self_reference = peer.getSelfReference();
        return new PeerReference[]{self_reference};
    }
}
