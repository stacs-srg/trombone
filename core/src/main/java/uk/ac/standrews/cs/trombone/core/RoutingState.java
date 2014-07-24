package uk.ac.standrews.cs.trombone.core;

import java.util.List;
import java.util.stream.Stream;
import uk.ac.standrews.cs.trombone.core.key.Key;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public interface RoutingState {

    boolean add(PeerReference reference);

    PeerReference remove(PeerReference reference);

    PeerReference closest(Key target);

    PeerReference first();

    PeerReference last();

    Stream<InternalPeerReference> stream();

    boolean inLocalKeyRange(Key target);

    int size();

    List<PeerReference> getReferences();
}
