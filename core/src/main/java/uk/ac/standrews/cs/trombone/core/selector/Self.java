package uk.ac.standrews.cs.trombone.core.selector;

import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerReference;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public final class Self implements Selector {

    private static final Self SELF_SELECTOR_INSTANCE = new Self();
    private static final long serialVersionUID = 5755937814923183362L;

    private Self() {

    }

    public static Self getInstance() {

        return SELF_SELECTOR_INSTANCE;
    }

    @Override
    public PeerReference[] select(final Peer peer) {

        final PeerReference self_reference = peer.getSelfReference();
        return new PeerReference[]{self_reference};
    }
}
