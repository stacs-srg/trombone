package uk.ac.standrews.cs.trombone.core.util;

import com.google.common.collect.Ordering;
import uk.ac.standrews.cs.trombone.core.InternalPeerReference;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class InternalReferenceLastSeenComparator extends Ordering<InternalPeerReference> {

    private static final InternalReferenceLastSeenComparator RECENTLY_SEEN_INTERNAL_REFERENCE_COMPARATOR = new InternalReferenceLastSeenComparator();

    private InternalReferenceLastSeenComparator() {

    }

    public static InternalReferenceLastSeenComparator getInstance() {

        return RECENTLY_SEEN_INTERNAL_REFERENCE_COMPARATOR;
    }

    @Override
    public int compare(final InternalPeerReference one, final InternalPeerReference other) {

        return Long.compare(one.getLastSeen(), other.getLastSeen());
    }
}