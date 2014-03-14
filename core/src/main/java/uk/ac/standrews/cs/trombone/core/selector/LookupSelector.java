package uk.ac.standrews.cs.trombone.core.selector;

import java.util.ArrayList;
import java.util.List;
import org.mashti.jetson.exception.RPCException;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerReference;
import uk.ac.standrews.cs.trombone.core.key.Key;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class LookupSelector implements Selector {

    private static final long serialVersionUID = -478170113699920480L;
    private final Key[] targets;

    public LookupSelector(Key... targets) {

        this.targets = targets;
    }

    @Override
    public List<PeerReference> select(final Peer peer) throws RPCException {

        final List<PeerReference> results = new ArrayList<>(targets.length);
        for (Key target : targets) {
            results.add(peer.lookup(target));
        }
        return results;
    }
}
