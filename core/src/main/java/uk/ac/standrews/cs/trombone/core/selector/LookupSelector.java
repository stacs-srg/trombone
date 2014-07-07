package uk.ac.standrews.cs.trombone.core.selector;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerReference;
import uk.ac.standrews.cs.trombone.core.key.Key;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class LookupSelector extends Selector {

    private static final long serialVersionUID = -478170113699920480L;
    private final Key[] targets;

    public LookupSelector(Key... targets) {

        super(targets.length, ReachabilityCriteria.REACHABLE);
        this.targets = targets;
    }

    @Override
    public List<PeerReference> select(final Peer peer) {

        return Stream.of(targets).map(target -> {
            try {
                return peer.lookup(target).get();
            }
            catch (Exception e) {
                return null;
            }
        }).collect(Collectors.toList());
    }

    @Override
    public Selector copy() {

        return new LookupSelector(targets);
    }
}
