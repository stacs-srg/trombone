package uk.ac.standrews.cs.trombone.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
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

        Set<PeerReference> references = new HashSet<>();
        for (int i = 0; i < NETWORK_SIZE; i++) {
            final Key key = KEY_PROVIDER.get();
            final Peer peer = PeerFactory.createPeer(key);
            network.put(key, peer);
            peer.expose();

            references.add(peer.getSelfReference());
        }

        for (Peer peer : network.values()) {
            peer.push(new ArrayList<PeerReference>(references)).get();
        }
    }

    @Test
    public void testState() throws Exception {

        for (Peer peer : network.values()) {

            Assert.assertEquals(network.size() - 1, peer.getPeerState().size());
        }

    }

    @Test
    public void testLookupCorrectness() throws Exception {

        for (int i = 0; i < 500; i++) {
            final Map.Entry<Key, Peer> entry = network.firstEntry();
            final Peer peer = entry.getValue();
            final Key target = KEY_PROVIDER.get();

            final Map.Entry<Key, Peer> ceilingEntry = network.ceilingEntry(target);
            Assert.assertEquals((ceilingEntry == null ? network.firstEntry() : ceilingEntry).getKey(), peer.lookup(target).get().getKey());

        }
    }
}
