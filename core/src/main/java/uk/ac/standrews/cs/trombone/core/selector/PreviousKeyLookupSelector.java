package uk.ac.standrews.cs.trombone.core.selector;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerReference;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class PreviousKeyLookupSelector extends Selector {

    private static final long serialVersionUID = -6477106603395278649L;
    private static final PreviousKeyLookupSelector NEXT_PREVIOUS_LOOKUP_SELECTOR = new PreviousKeyLookupSelector();

    public static PreviousKeyLookupSelector getInstance() {

        return NEXT_PREVIOUS_LOOKUP_SELECTOR;
    }

    private PreviousKeyLookupSelector() {

        super(1);
    }

    @Override
    public CompletableFuture<List<PeerReference>> select(final Peer peer) {

        return peer.lookup(peer.key()
                .previous())
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
