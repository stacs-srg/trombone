package uk.ac.standrews.cs.trombone.core.selector;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerReference;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class NextKeyLookupSelector extends Selector {

    private static final long serialVersionUID = -6477106603395278649L;
    private static final NextKeyLookupSelector NEXT_PREVIOUS_LOOKUP_SELECTOR = new NextKeyLookupSelector();

    public static NextKeyLookupSelector getInstance() {

        return NEXT_PREVIOUS_LOOKUP_SELECTOR;
    }

    private NextKeyLookupSelector() {

        super(1);
    }

    @Override
    public CompletableFuture<List<PeerReference>> select(final Peer peer) {

        return peer.lookup(peer.key()
                .next())
                .thenApply(Collections:: singletonList);
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
