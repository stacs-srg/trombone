package uk.ac.standrews.cs.trombone.core.selector;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerReference;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public final class EmptySelector extends Selector {

    private static final long serialVersionUID = -7162399826632352226L;
    private static final EmptySelector INSTANCE = new EmptySelector();
    private static final CompletableFuture<List<PeerReference>> EMPTY_SELECTION = CompletableFuture.completedFuture(Collections.emptyList());

    public static EmptySelector getInstance() {

        return INSTANCE;
    }

    private EmptySelector() {

        super(0);
    }

    @Override
    public CompletableFuture<List<PeerReference>> select(final Peer peer) {

        return EMPTY_SELECTION;
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
