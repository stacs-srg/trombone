package uk.ac.standrews.cs.trombone.core.selector;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerReference;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class First extends Selector {

    private static final long serialVersionUID = 5801175705134547771L;
    private static final Logger LOGGER = LoggerFactory.getLogger(First.class);

    public First(Integer size, ReachabilityCriteria reachability_criteria) {

        super(size, reachability_criteria);
    }

    @Override
    public List<PeerReference> select(final Peer peer) {

        final Stream<PeerReference> state_stream = peer.getPeerState().stream();

        switch (reachability_criteria) {
            case REACHABLE:
                return state_stream.filter(reference -> reference.isReachable()).limit(size).collect(Collectors.toList());
            case UNREACHABLE:
                return state_stream.filter(reference -> !reference.isReachable()).limit(size).collect(Collectors.toList());
            case ANY:
                return state_stream.limit(size).collect(Collectors.toList());
            default:
                LOGGER.warn("unknown reachability criteria {}", reachability_criteria);
                return null;
        }
    }

    @Override
    public Selector copy() {

        return new First(size, reachability_criteria);
    }
    
    
}
