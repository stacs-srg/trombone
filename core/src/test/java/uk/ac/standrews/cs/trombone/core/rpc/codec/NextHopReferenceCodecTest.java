package uk.ac.standrews.cs.trombone.core.rpc.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.net.InetSocketAddress;
import org.junit.Before;
import org.junit.Test;
import uk.ac.standrews.cs.trombone.core.NextHopReference;
import uk.ac.standrews.cs.trombone.core.PeerReference;
import uk.ac.standrews.cs.trombone.core.key.Key;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class NextHopReferenceCodecTest {

    private NextHopReferenceCodec codec;
    private ByteBuf buffer;

    @Before
    public void setUp() throws Exception {

        codec = new NextHopReferenceCodec();
        buffer = Unpooled.buffer();
    }

    @Test
    public void testIsSupported() throws Exception {

        assertTrue(codec.isSupported(NextHopReference.class));
        assertFalse(codec.isSupported(PeerReference.class));
        assertFalse(codec.isSupported(String.class));
        assertFalse(codec.isSupported(null));
    }

    @Test
    public void testCodec() throws Exception {

        final NextHopReference[] keys = {
                null,
                new NextHopReference(new PeerReference(Key.valueOf(45), new InetSocketAddress(8541)), true),
                new NextHopReference(new PeerReference(Key.valueOf(333), new InetSocketAddress(85), false), false)
        };

        for (NextHopReference key : keys) {
            codec.encode(key, buffer, PeerCodecs.getInstance(), NextHopReference.class);
            assertEquals(key, codec.decode(buffer, PeerCodecs.getInstance(), NextHopReference.class));
        }
    }
}
