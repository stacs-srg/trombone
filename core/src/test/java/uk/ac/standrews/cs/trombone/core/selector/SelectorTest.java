package uk.ac.standrews.cs.trombone.core.selector;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import org.junit.Test;
import uk.ac.standrews.cs.trombone.core.Key;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class SelectorTest {

    static final Random RANDOM = new Random(1237418);

    @Test
    public void testGetters() throws Exception {

        for (int i = 0; i < 100; i++) {

            final int size = RANDOM.nextInt(1000);

            Selector selector = new Selector(size) {

                private static final long serialVersionUID = -6385048910225591282L;

                @Override
                public CompletableFuture<List<PeerReference>> select(final Peer peer) {

                    return null;
                }

                @Override
                public Selector copy() {

                    return null;
                }
            };

            assertFalse(selector.isSingleton());
            assertEquals(size, selector.getSelectionSize());
        }
    }

    static List<PeerReference> generateRandomPeerReferences(final int count) {

        final List<PeerReference> references = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            references.add(generateRandomPeerReference());
        }
        return references;

    }


    static PeerReference generateRandomPeerReference() {

        return new PeerReference(Key.valueOf(RANDOM.nextLong()), new InetSocketAddress(RANDOM.nextInt(0xffff)));
    }
}