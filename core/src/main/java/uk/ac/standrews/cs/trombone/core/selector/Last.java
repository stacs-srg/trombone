package uk.ac.standrews.cs.trombone.core.selector;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.trombone.core.InternalPeerReference;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerReference;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class Last extends Selector {

    private static final long serialVersionUID = 4969898731774717311L;
    private static final Logger LOGGER = LoggerFactory.getLogger(Last.class);

    public Last(int size, ReachabilityCriteria reachability_criteria) {

        super(size, reachability_criteria);
    }

    @Override
    public List<PeerReference> select(final Peer peer) {

        final List<InternalPeerReference> references = peer.getPeerState().getInternalReferences();
        Collections.reverse(references);
        final Stream<InternalPeerReference> state_reverse_stream = references.stream();
        switch (reachability_criteria) {
            case REACHABLE:
                return state_reverse_stream.filter(reference -> reference.isReachable()).limit(size).collect(Collectors.toList());
            case UNREACHABLE:
                return state_reverse_stream.filter(reference -> !reference.isReachable()).limit(size).collect(Collectors.toList());
            case REACHABLE_OR_UNREACHABLE:
                return state_reverse_stream.limit(size).collect(Collectors.toList());
            default:
                LOGGER.warn("unknown reachability criteria {}", reachability_criteria);
                return null;
        }
    }

    @Override
    public Selector copy() {

        return new Last(size, reachability_criteria);
    }
}
