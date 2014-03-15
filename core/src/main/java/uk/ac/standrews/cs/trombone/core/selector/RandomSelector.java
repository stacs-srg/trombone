package uk.ac.standrews.cs.trombone.core.selector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerReference;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class RandomSelector implements Selector {

    private static final long serialVersionUID = -2686666721712477700L;
    private static final int SEED = 6546545;
    private static final Random RANDOM = new Random(SEED);
    private final int size;

    public RandomSelector(int size) {

        this.size = size;
    }

    @Override
    public List<PeerReference> select(final Peer peer) {

        final List<PeerReference> references = peer.getPeerState().getReferences();
        final int references_size = references.size();

        final int result_size = Math.min(size, references_size);
        final List<PeerReference> result = new ArrayList<>(result_size);

        for (int i = 0; i < result_size; i++) {
            result.add(references.get(nextInt(references_size - i)));
            references.remove(result.get(i));
        }

        return result;
    }

    private synchronized int nextInt(final int max) {

        return RANDOM.nextInt(max);
    }
}
