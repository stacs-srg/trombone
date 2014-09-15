package uk.ac.standrews.cs.trombone.core.selector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerReference;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class Last extends Selector {

    private static final long serialVersionUID = 4969898731774717311L;

    public Last(Integer size) {

        super(size);
    }

    @Override
    public CompletableFuture<List<PeerReference>> select(final Peer peer) {

        final Collection<PeerReference> references = peer.getPeerState()
                .getReferences();

        final ArrayList<PeerReference> list = new ArrayList<PeerReference>(references);
        Collections.reverse(list);
        final Stream<PeerReference> state_reverse_stream = list.stream();

        final List<PeerReference> result = state_reverse_stream.limit(size)
                .collect(Collectors.toList());
        return CompletableFuture.completedFuture(result);
    }

    @Override
    public Selector copy() {

        return new Last(size);
    }
}
