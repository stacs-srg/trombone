package uk.ac.standrews.cs.trombone.core.selector;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
        final List<PeerReference> selection;
        switch (reachability_criteria) {
            case REACHABLE:
                selection = state.firstReachable(size);
                break;
            case UNREACHABLE:
                selection = state.firstUnreachable(size);
                break;
            case REACHABLE_OR_UNREACHABLE:
                selection = state.first(size);
                break;
            default:
                selection = null;
                LOGGER.warn("unknown reachability criteria {}", reachability_criteria);
        }

        return selection;
    }

    @Override
    public Selector copy() {

        return new First(size, reachability_criteria);
    }
}
