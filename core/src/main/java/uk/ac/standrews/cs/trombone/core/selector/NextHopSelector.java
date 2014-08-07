package uk.ac.standrews.cs.trombone.core.selector;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerReference;
import uk.ac.standrews.cs.trombone.core.Key;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class NextHopSelector extends Selector {

    private static final long serialVersionUID = -5105443487433643022L;
    private final Key[] targets;

    public NextHopSelector(Key... targets) {

        super(targets.length, ReachabilityCriteria.REACHABLE);
        this.targets = targets;
    }

    @Override
    public List<PeerReference> select(final Peer peer) {

        return Stream.of(targets).map(target -> {
            try {
                return peer.nextHop(target).get();
            }
            catch (Exception e) {
                return null;
            }
        }).collect(Collectors.toList());
    }

    @Override
    public Selector copy() {

        return new NextHopSelector(targets);
    }
}
