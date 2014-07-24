package uk.ac.standrews.cs.trombone.core.selector;

import java.util.List;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerReference;
import uk.ac.standrews.cs.trombone.core.RoutingState;
import uk.ac.standrews.cs.trombone.core.util.InternalReferenceLastSeenComparator;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class MostRecentlySeen extends Selector {

    private static final long serialVersionUID = -4103050337428351524L;
    private static final InternalReferenceLastSeenComparator INTERNAL_REFERENCE_LAST_SEEN_COMPARATOR = InternalReferenceLastSeenComparator.getInstance();

    public MostRecentlySeen(int size, ReachabilityCriteria reachability_criteria) {

        super(size, reachability_criteria);
    }

    @Override
    public List<? extends PeerReference> select(final Peer peer) {

        final RoutingState state = peer.getPeerState();
        return null; //state.getReferences().stream().sorted(INTERNAL_REFERENCE_LAST_SEEN_COMPARATOR).limit(size).collect(Collectors.toList());
    }

    @Override
    public Selector copy() {

        return new MostRecentlySeen(size, reachability_criteria);
    }
}
