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
public class Last extends Selector {

    private static final long serialVersionUID = 4969898731774717311L;
    private static final Logger LOGGER = LoggerFactory.getLogger(Last.class);

    public Last(int size, ReachabilityCriteria reachability_criteria) {

        super(size, reachability_criteria);
    }

    @Override
    public List<PeerReference> select(final Peer peer) {

        final PeerState state = peer.getPeerState();
        switch (reachability_criteria) {
            case REACHABLE:
                return lastReachable(size, state);
            case UNREACHABLE:
                return lastUnreachable(size, state);
            case REACHABLE_OR_UNREACHABLE:
                return last(size, state);
            default:
                LOGGER.warn("unknown reachability criteria {}", reachability_criteria);
                return null;
        }
    }

    @Override
    public Selector copy() {

        return new Last(size, reachability_criteria);
    }

    public List<PeerReference> last(int count, final PeerState state) {

        final List<PeerReference> results = new ArrayList<>(count);
        final Iterator<InternalPeerReference> descending_state_iterator = state.descendingIterator();
        while (descending_state_iterator.hasNext() && results.size() < count) {
            results.add(descending_state_iterator.next());
        }
        return results;
    }

    public List<PeerReference> lastReachable(final int size, final PeerState state) {

        final List<PeerReference> result = new ArrayList<>(size);
        final Iterator<InternalPeerReference> descending_state_iterator = state.descendingIterator();
        while (descending_state_iterator.hasNext() && result.size() < size) {
            final InternalPeerReference next = descending_state_iterator.next();
            if (next.isReachable()) {
                result.add(next);
            }
        }
        return result;
    }

    public List<PeerReference> lastUnreachable(final int size, final PeerState state) {

        final List<PeerReference> result = new ArrayList<>(size);
        final Iterator<InternalPeerReference> descending_state_iterator = state.descendingIterator();
        while (descending_state_iterator.hasNext() && result.size() < size) {
            final InternalPeerReference next = descending_state_iterator.next();
            if (!next.isReachable()) {
                result.add(next);
            }
        }
        return result;
    }
}
