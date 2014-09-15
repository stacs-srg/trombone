package uk.ac.standrews.cs.trombone.core.selector;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerReference;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class First extends Selector {

    private static final long serialVersionUID = 5801175705134547771L;

    public First(Integer size) {

        super(size);
    }

    @Override
    public CompletableFuture<List<PeerReference>> select(final Peer peer) {

        final Stream<PeerReference> state_stream = peer.getPeerState()
                .stream();
        final List<PeerReference> result = state_stream.limit(size)
                .collect(Collectors.toList());
        return CompletableFuture.completedFuture(result);
    }

    @Override
    public Selector copy() {

        return new First(size);
    }

}
