package uk.ac.standrews.cs.trombone.trombone.selector;

import org.mashti.jetson.exception.RPCException;
import uk.ac.standrews.cs.trombone.trombone.key.Key;
import uk.ac.standrews.cs.trombone.trombone.Peer;
import uk.ac.standrews.cs.trombone.trombone.PeerReference;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class LookupSelector implements Selector {

    private final Key[] targets;

    public LookupSelector(Key... targets) {

        this.targets = targets;
    }

    @Override
    public PeerReference[] select(final Peer peer, final int size) throws RPCException {

        final PeerReference[] results = new PeerReference[targets.length];
        int index = 0;
        for (Key target : targets) {
            results[index] = peer.lookup(target);
            index++;
        }
        return results;
    }
}
