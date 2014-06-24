package uk.ac.standrews.cs.trombone.core.selector;

import java.util.ArrayList;
import java.util.List;
import org.uncommons.maths.random.MersenneTwisterRNG;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerReference;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class RandomSelector extends Selector {

    private static final long serialVersionUID = -2686666721712477700L;

    public RandomSelector(int size, ReachabilityCriteria reachability_criteria) {

        super(size, reachability_criteria);
    }

    @Override
    public List<PeerReference> select(final Peer peer) {

        final List<PeerReference> references = peer.getPeerState().getReferences();
        final int references_size = references.size();

        final int result_size = Math.min(size, references_size);
        final List<PeerReference> result = new ArrayList<>(result_size);
        final MersenneTwisterRNG random = peer.getRandom();

        for (int i = 0; i < result_size; i++) {
            final int selection_index = random.nextInt(references_size - i);
            result.add(references.get(selection_index));
            references.remove(result.get(i));
        }

        return result;
    }

    @Override
    public RandomSelector copy() {

        return new RandomSelector(size, reachability_criteria);
    }
}
