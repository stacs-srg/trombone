package uk.ac.standrews.cs.trombone.core.selector;

import java.net.InetSocketAddress;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerFactory;
import uk.ac.standrews.cs.trombone.core.PeerReference;
import uk.ac.standrews.cs.trombone.core.integration.LookupCorrectnessTest;
import uk.ac.standrews.cs.trombone.core.Key;
import uk.ac.standrews.cs.trombone.core.util.RelativeRingDistanceComparator;

public class LastTest extends SelectorTest {

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testSelect() throws Exception {

        final Key peer_key = Key.valueOf(RANDOM.nextLong());
        final Peer peer = PeerFactory.createPeer(new InetSocketAddress(0), peer_key, LookupCorrectnessTest.TROMBONE_CONFIGURATION);
        final int reference_count = 100;

        final List<PeerReference> references = generateRandomPeerReferences(reference_count);
        peer.push(references).get();

        final RelativeRingDistanceComparator comparator = new RelativeRingDistanceComparator(peer_key);
        final Comparator<PeerReference> c = (one, other) -> {

            return comparator.compare(one.getKey(), other.getKey());
        };

        references.sort(c.reversed());

        for (int i = 0; i <= reference_count; i++) {
            Assert.assertEquals(references.subList(0, i), new Last(i).select(peer).get());
        }

        final List<PeerReference> reachable_references = references.stream().collect(Collectors.toList());
        for (int i = 0; i <= reachable_references.size(); i++) {
            Assert.assertEquals(reachable_references.subList(0, i), new Last(i).select(peer).get());
        }

    }

    @Test
    public void testCopy() throws Exception {

        for (int i = 0; i < 100; i++) {

            final Last last = new Last(RANDOM.nextInt(65) + 1);
            Assert.assertEquals(last, last.copy());
        }
    }

}