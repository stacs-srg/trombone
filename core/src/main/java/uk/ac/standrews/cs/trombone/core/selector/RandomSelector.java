package uk.ac.standrews.cs.trombone.core.selector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerReference;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class RandomSelector extends Selector {

    private static final long serialVersionUID = -2686666721712477700L;

    public RandomSelector(Integer size, ReachabilityCriteria reachability_criteria) {

        super(size, reachability_criteria);
    }

    @Override
    public List<PeerReference> select(final Peer peer) {

        final Collection<PeerReference> references = peer.getPeerState()
                .getReferences();
        final int references_size = references.size();

        final int result_size = Math.min(size, references_size);
        final List<PeerReference> result = new ArrayList<>(references);
        final Random random = peer.getRandom();

        while(result.size() > result_size){
            final int selection_index = random.nextInt(result.size());
            result.remove(selection_index);
        }

        return result;
    }

    @Override
    public RandomSelector copy() {

        return new RandomSelector(size, reachability_criteria);
    }
}
