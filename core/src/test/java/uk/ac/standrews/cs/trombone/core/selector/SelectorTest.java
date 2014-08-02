package uk.ac.standrews.cs.trombone.core.selector;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.Test;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerReference;
import uk.ac.standrews.cs.trombone.core.key.Key;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class SelectorTest {

    static final Random RANDOM = new Random(1237418);
    static final Selector.ReachabilityCriteria[] REACHABILITY_CRITERIA_VALUES = Selector.ReachabilityCriteria.values();

    @Test
    public void testGetters() throws Exception {

        for (int i = 0; i < 100; i++) {

            final int size = RANDOM.nextInt(1000);
            final Selector.ReachabilityCriteria criteria = randomReachabilityCriteria();

            Selector selector = new Selector(size, criteria) {

                private static final long serialVersionUID = -6385048910225591282L;

                @Override
                public List<PeerReference> select(final Peer peer) {

                    return null;
                }

                @Override
                public Selector copy() {

                    return null;
                }
            };

            assertFalse(selector.isSingleton());
            assertEquals(size, selector.getSelectionSize());
            assertEquals(criteria, selector.getReachabilityCriteria());
        }
    }

    static List<PeerReference> generateRandomPeerReferences(final int count) {

        final List<PeerReference> references = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            references.add(generateRandomPeerReference());
        }
        return references;

    }

    static Selector.ReachabilityCriteria randomReachabilityCriteria() {

        return REACHABILITY_CRITERIA_VALUES[RANDOM.nextInt(REACHABILITY_CRITERIA_VALUES.length)];
    }

    static PeerReference generateRandomPeerReference() {

        return new PeerReference(Key.valueOf(RANDOM.nextLong()), new InetSocketAddress(RANDOM.nextInt(0xffff)), RANDOM.nextBoolean());
    }
}