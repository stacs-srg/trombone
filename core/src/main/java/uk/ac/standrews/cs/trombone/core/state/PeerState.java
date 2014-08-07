package uk.ac.standrews.cs.trombone.core.state;

import java.util.List;
import java.util.stream.Stream;
import uk.ac.standrews.cs.trombone.core.PeerReference;
import uk.ac.standrews.cs.trombone.core.Key;

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

    List<PeerReference> getReferences();
}
