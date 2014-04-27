package uk.ac.standrews.cs.trombone.core.selector;

import java.util.ArrayList;
import java.util.List;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerReference;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public final class Self extends Selector {

    public static final Self INSTANCE = new Self();
    private static final long serialVersionUID = 5755937814923183362L;

    private Self() {

        super(1, ReachabilityCriteria.REACHABLE);
    }

    @Override
    public List<PeerReference> select(final Peer peer) {

        final PeerReference self_reference = peer.getSelfReference();
        final List<PeerReference> result = new ArrayList<>(1);
        result.add(self_reference);
        return result;
    }

    @Override
    public Selector copy() {

        return this;
    }

    @Override
    public boolean isSingleton() {

        return true;
    }
}
