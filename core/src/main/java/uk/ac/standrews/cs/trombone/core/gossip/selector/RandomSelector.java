package uk.ac.standrews.cs.trombone.core.gossip.selector;

import java.util.List;
import java.util.Random;
import org.mashti.jetson.exception.RPCException;
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
    public PeerReference[] select(final Peer peer) throws RPCException {

        final List<PeerReference> references = peer.getPeerState().getReferences();
        final int references_size = references.size();

        final int result_size = Math.min(size, references_size);
        final PeerReference[] result = new PeerReference[result_size];

        for (int i = 0; i < result_size; i++) {
            result[i] = references.get(nextInt(references_size - i));
            references.remove(result[i]);
        }

        return result;
    }

    private synchronized int nextInt(final int max) {

        return RANDOM.nextInt(max);
    }
}
