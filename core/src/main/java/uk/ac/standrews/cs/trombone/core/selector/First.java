package uk.ac.standrews.cs.trombone.core.selector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.trombone.core.InternalPeerReference;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerReference;
import uk.ac.standrews.cs.trombone.core.PeerState;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class First extends Selector {

    private static final long serialVersionUID = 5801175705134547771L;
    private static final Logger LOGGER = LoggerFactory.getLogger(First.class);

    public First(int size, ReachabilityCriteria reachability_criteria) {

        super(size, reachability_criteria);
    }

    @Override
    public List<PeerReference> select(final Peer peer) {

        final PeerState state = peer.getPeerState();
        final Iterator<InternalPeerReference> state_iterator = state.iterator();
        
        switch (reachability_criteria) {
            case REACHABLE:
                return firstReachable(size, state_iterator);
            case UNREACHABLE:
                return firstUnreachable(size, state_iterator);
            case REACHABLE_OR_UNREACHABLE:
                return first(size, state_iterator);
            default:
                LOGGER.warn("unknown reachability criteria {}", reachability_criteria);
                return null;
        }
    }

    @Override
    public Selector copy() {

        return new First(size, reachability_criteria);
    }

    public List<PeerReference> first(int count, final Iterator<InternalPeerReference> references) {

        final List<PeerReference> result = new ArrayList<>(count);
        while (references.hasNext() && result.size() < count) {
            result.add(references.next());
        }
        return result;
    }

    public List<PeerReference> firstReachable(final int size, final Iterator<InternalPeerReference> references) {

        final List<PeerReference> result = new ArrayList<>(size);
        while (references.hasNext() && result.size() < size) {
            final InternalPeerReference next = references.next();
            if (next.isReachable()) {
                result.add(next);
            }
        }
        return result;
    }

    public List<PeerReference> firstUnreachable(final int size, final Iterator<InternalPeerReference> references) {

        final List<PeerReference> result = new ArrayList<>(size);
        while (references.hasNext() && result.size() < size) {
            final InternalPeerReference next = references.next();
            if (!next.isReachable()) {
                result.add(next);
            }
        }
        return result;
    }
}
