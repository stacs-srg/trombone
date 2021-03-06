package uk.ac.standrews.cs.trombone.core.selector;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerReference;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public final class Self extends Selector {

    private static final Self INSTANCE = new Self();
    private static final long serialVersionUID = 5755937814923183362L;

    public static Self getInstance() {

        return INSTANCE;
    }

    private Self() {

        super(1);
    }

    @Override
    public CompletableFuture<List<PeerReference>> select(final Peer peer) {

        final PeerReference self_reference = peer.getSelfReference();
        return CompletableFuture.completedFuture(Collections.singletonList(self_reference));
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
