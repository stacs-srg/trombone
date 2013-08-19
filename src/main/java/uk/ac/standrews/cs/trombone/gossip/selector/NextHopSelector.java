package uk.ac.standrews.cs.trombone.gossip.selector;

import uk.ac.standrews.cs.trombone.Peer;
import uk.ac.standrews.cs.trombone.PeerReference;
import uk.ac.standrews.cs.trombone.key.Key;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class NextHopSelector implements Selector {

    private final Key[] targets;

    public NextHopSelector(Key... targets) {

        this.targets = targets;
    }

    @Override
    public PeerReference[] select(final Peer peer, final int size) {

        final PeerReference[] next_hops = new PeerReference[targets.length];
        int index = 0;
        for (Key target : targets) {
            next_hops[index] = peer.nextHop(target);
            index++;
        }
        return next_hops;
    }
}
