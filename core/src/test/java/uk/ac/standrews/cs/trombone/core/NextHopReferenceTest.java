package uk.ac.standrews.cs.trombone.core;

import java.net.InetSocketAddress;
import org.junit.Assert;
import org.junit.Test;
import uk.ac.standrews.cs.trombone.core.key.Key;

public class NextHopReferenceTest {

    private static final Key key = Key.valueOf(234);
    private static final InetSocketAddress address = new InetSocketAddress(4564);
    private static final PeerReference reference = new PeerReference(key, address);
    private static final NextHopReference next_hop_reference = new NextHopReference(reference, true);

    @Test
    public void testIsFinalHop() throws Exception {

        Assert.assertTrue(next_hop_reference.isFinalHop());
        Assert.assertFalse(new NextHopReference(new PeerReference(key, address), false).isFinalHop());
        Assert.assertEquals(key, next_hop_reference.getKey());
        Assert.assertEquals(address, next_hop_reference.getAddress());
    }

    @Test
    public void testEquals() throws Exception {

        Assert.assertEquals(next_hop_reference, new NextHopReference(reference, true));
        Assert.assertNotEquals(next_hop_reference, new NextHopReference(reference, false));
        Assert.assertNotEquals(next_hop_reference, reference);
        Assert.assertNotEquals(new Object(), new NextHopReference(reference, false));
        Assert.assertNotEquals(next_hop_reference, new NextHopReference(new PeerReference(Key.valueOf(44), address), false));
    }

    @Test
    public void testHashCode() throws Exception {

        Assert.assertNotEquals(reference.hashCode(), next_hop_reference.hashCode());
    }
}