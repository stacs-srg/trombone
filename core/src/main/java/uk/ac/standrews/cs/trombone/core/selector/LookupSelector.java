package uk.ac.standrews.cs.trombone.core.selector;

import java.util.ArrayList;
import java.util.List;
import org.mashti.jetson.exception.RPCException;
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

        final List<PeerReference> results = new ArrayList<>(targets.length);
        for (Key target : targets) {

            PeerReference lookup_result;
            try {
                lookup_result = peer.lookup(target);
            }
            catch (RPCException e) {
                lookup_result = null;
            }
            results.add(lookup_result);
        }
        return results;
    }

    @Override
    public Selector copy() {

        return new LookupSelector(targets);
    }
}
