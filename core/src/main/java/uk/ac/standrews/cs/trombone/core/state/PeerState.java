package uk.ac.standrews.cs.trombone.core.state;

import java.util.Collection;
import java.util.stream.Stream;
import uk.ac.standrews.cs.trombone.core.Key;
import uk.ac.standrews.cs.trombone.core.PeerReference;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public interface PeerState {

    boolean add(PeerReference reference);

    PeerReference remove(PeerReference reference);

    PeerReference closest(Key target);

    PeerReference first();

    PeerReference last();

    Stream<PeerReference> stream();

    boolean inLocalKeyRange(Key target);

    int size();

    Collection<PeerReference> getReferences();
}
