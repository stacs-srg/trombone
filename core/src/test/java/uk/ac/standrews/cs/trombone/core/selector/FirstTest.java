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
import uk.ac.standrews.cs.trombone.core.integration.SingleProcessPeerManager;
import uk.ac.standrews.cs.trombone.core.key.Key;
import uk.ac.standrews.cs.trombone.core.util.RelativeRingDistanceComparator;

public class FirstTest extends SelectorTest {

    

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testSelect() throws Exception {

        final Key peer_key = Key.valueOf(RANDOM.nextLong());
        Peer peer = PeerFactory.createPeer(new InetSocketAddress(0), peer_key, SingleProcessPeerManager.CONFIGURATION);
        final int reference_count = 100;

        final List<PeerReference> references = generateRandomPeerReferences(reference_count);
        peer.push(references);

        final RelativeRingDistanceComparator comparator = new RelativeRingDistanceComparator(peer_key);
        final Comparator<PeerReference> c = (one, other) -> {

            return comparator.compare(one.getKey(), other.getKey());
        };
        
        references.sort(c);

        for (int i = 0; i <= reference_count; i++) {
            Assert.assertEquals(references.subList(0, i), new First(i, Selector.ReachabilityCriteria.ANY).select(peer));
        }

        final List<PeerReference> reachable_references = references.stream().filter(reference -> reference.isReachable()).collect(Collectors.toList());
        for (int i = 0; i <= reachable_references.size(); i++) {
            Assert.assertEquals(reachable_references.subList(0, i), new First(i, Selector.ReachabilityCriteria.REACHABLE).select(peer));
        }
        
        final List<PeerReference> unreachable_references = references.stream().filter(reference -> !reference.isReachable()).collect(Collectors.toList());
        for (int i = 0; i <= unreachable_references.size(); i++) {
            Assert.assertEquals(unreachable_references.subList(0, i), new First(i, Selector.ReachabilityCriteria.UNREACHABLE).select(peer));
        }
    }

   

    @Test
    public void testCopy() throws Exception {

        for (int i = 0; i < 100; i++) {

            final First first = new First(RANDOM.nextInt(65) + 1, REACHABILITY_CRITERIA_VALUES[RANDOM.nextInt(REACHABILITY_CRITERIA_VALUES.length)]);
            Assert.assertEquals(first, first.copy());
        }
    }
}