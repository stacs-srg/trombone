package uk.ac.standrews.cs.trombone.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.ac.standrews.cs.trombone.core.key.Key;
import uk.ac.standrews.cs.trombone.core.key.KeyProvider;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class LookupCorrectnessTest {

    public static final KeyProvider KEY_PROVIDER = new KeyProvider(32, 984156);
    private final NavigableMap<Key, Peer> network = new TreeMap<>();
    private static final int NETWORK_SIZE = 500;

    @Before
    public void setUp() throws Exception {

        List<PeerReference> references = new ArrayList<>();
        for (int i = 0; i < NETWORK_SIZE; i++) {
            final Key key = KEY_PROVIDER.get();
            final Peer peer = PeerFactory.createPeer(key);
            network.put(key, peer);
            peer.expose();

            references.add(peer.getSelfReference());
        }

        for (Peer peer : network.values()) {
            peer.join(lookupCorrectly(peer.getKey().get().next())).get();
        }
    }

    @Test
    public void testRingStabilisation() throws Exception {

        for (Map.Entry<Key, Peer> entry : network.entrySet()) {
            Key key = entry.getKey();
            Peer peer = entry.getValue();

            Assert.assertEquals(lookupCorrectly(key.next()), peer.getPeerState().first());
        }

    }

    @Test
    public void testLookupCorrectness() throws Exception {

        List<CompletableFuture<?>> futures = new ArrayList<>();

        for (Map.Entry<Key, Peer> entry : network.entrySet()) {

            final Peer peer = entry.getValue();
            for (int i = 0; i < 10; i++) {

                final Key target = KEY_PROVIDER.get();
                final CompletableFuture<Void> future_test = peer.lookup(target).thenAccept(actual -> {

                    final PeerReference expected = lookupCorrectly(target);
                    Assert.assertEquals(expected, actual);
                });

                futures.add(future_test);
            }
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])).get();
    }

    private PeerReference lookupCorrectly(Key target) {

        final Map.Entry<Key, Peer> ceilingEntry = network.ceilingEntry(target);
        return (ceilingEntry == null ? network.firstEntry() : ceilingEntry).getValue().getSelfReference();
    }
}
