package uk.ac.standrews.cs.trombone.core;

import java.net.InetSocketAddress;
import java.util.Random;
import java.util.function.Supplier;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PeerReferenceTest {

    private final Random random = new Random(52165);
    private final Supplier<Key> key_provider = () -> Key.valueOf(random.nextLong());

    @Test
    public void testIsReachable() throws Exception {

        assertTrue(new PeerReference(key_provider.get(), new InetSocketAddress(0)).isReachable());
        assertFalse(new PeerReference(key_provider.get(), new InetSocketAddress(0), false).isReachable());
    }

    @Test
    public void testGetKey() throws Exception {

        final Key key = key_provider.get();
        final PeerReference reference = new PeerReference(key, new InetSocketAddress(0), false);
        assertEquals(key, reference.getKey());
        Assert.assertNull(new PeerReference(null, new InetSocketAddress(0)).getKey());
    }

    @Test
    public void testGetAddress() throws Exception {

        final InetSocketAddress address = new InetSocketAddress(44444);
        final PeerReference reference = new PeerReference(key_provider.get(), address);

        assertEquals(address, reference.getAddress());
        Assert.assertNull(new PeerReference(key_provider.get(), null).getAddress());
    }

    @Test
    public void testHashCode() throws Exception {

        final Key key = key_provider.get();
        final PeerReference reference = new PeerReference(key, new InetSocketAddress(0), false);
        assertEquals(key.hashCode(), reference.hashCode());
    }

    @Test
    public void testEquals() throws Exception {

        final PeerReference reference = new PeerReference(key_provider.get(), new InetSocketAddress(0), false);
        assertFalse(reference.equals(null));
        assertFalse(reference.equals(new Object()));
        assertTrue(reference.equals(reference));

        final PeerReference reference2 = new PeerReference(key_provider.get(), new InetSocketAddress(0), false);
        assertFalse(reference.equals(reference2));
    }

    @Test
    public void testToString() throws Exception {

    }

    @Test
    public void testCompareTo() throws Exception {

        final PeerReference _1 = new PeerReference(Key.valueOf(1), new InetSocketAddress(0));
        final PeerReference _2 = new PeerReference(Key.valueOf(2), new InetSocketAddress(0));
        final PeerReference _0 = new PeerReference(Key.valueOf(0), new InetSocketAddress(0));
        assertEquals(0, _1.compareTo(_1));
        assertEquals(1, _1.compareTo(_0));
        assertEquals(-1, _1.compareTo(_2));

    }

    @Test
    public void testSetReachable() throws Exception {

        final PeerReference reference = new PeerReference(key_provider.get(), new InetSocketAddress(0), false);
        assertFalse(reference.isReachable());

        reference.setReachable(true);
        assertTrue(reference.isReachable());

        reference.setReachable(false);
        assertFalse(reference.isReachable());
    }
}