package uk.ac.standrews.cs.trombone.core.selector;

import java.util.ArrayList;
import java.util.List;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerReference;
import uk.ac.standrews.cs.trombone.core.key.Key;

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

        final List<PeerReference> next_hops = new ArrayList<>(targets.length);
        for (Key target : targets) {
            next_hops.add(peer.nextHop(target));
        }
        return next_hops;
    }

    @Override
    public Selector copy() {

        return new NextHopSelector(targets);
    }
}
