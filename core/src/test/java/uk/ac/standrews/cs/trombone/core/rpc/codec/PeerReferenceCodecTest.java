package uk.ac.standrews.cs.trombone.core.rpc.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.net.InetSocketAddress;
import org.junit.Before;
import org.junit.Test;
import uk.ac.standrews.cs.trombone.core.PeerReference;
import uk.ac.standrews.cs.trombone.core.key.Key;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class PeerReferenceCodecTest {

    private PeerReferenceCodec codec;
    private ByteBuf buffer;

    @Before
    public void setUp() throws Exception {

        codec = new PeerReferenceCodec();
        buffer = Unpooled.buffer();
    }

    @Test
    public void testIsSupported() throws Exception {

        assertTrue(codec.isSupported(PeerReference.class));
        assertTrue(codec.isSupported(SubPeerReference.class));
        assertFalse(codec.isSupported(String.class));
        assertFalse(codec.isSupported(null));
    }

    @Test
    public void testCodec() throws Exception {

        final PeerReference[] keys = {null, new PeerReference(Key.valueOf(45), new InetSocketAddress(8541)), new PeerReference(Key.valueOf(333), new InetSocketAddress(85), false)};

        for (PeerReference key : keys) {
            codec.encode(key, buffer, PeerCodecs.INSTANCE, PeerReference.class);
            assertEquals(key, codec.decode(buffer, PeerCodecs.INSTANCE, PeerReference.class));
        }
    }

    private static class SubPeerReference extends PeerReference {

        public SubPeerReference(final Key key, final InetSocketAddress address) {

            super(key, address);
        }
    }
}
