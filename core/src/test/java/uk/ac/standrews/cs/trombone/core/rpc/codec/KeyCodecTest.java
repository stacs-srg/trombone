package uk.ac.standrews.cs.trombone.core.rpc.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import uk.ac.standrews.cs.trombone.core.PeerConfiguration;
import uk.ac.standrews.cs.trombone.core.key.Key;
import uk.ac.standrews.cs.trombone.core.key.KeySupplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class KeyCodecTest {

    static {
        System.setProperty(PeerConfiguration.PEER_KEY_LENGTH_SYSTEM_PROPERTY, "128");
    }

    private KeyCodec codec;
    private ByteBuf buffer;
    private Random random;

    @Before
    public void setUp() throws Exception {

        codec = new KeyCodec();
        buffer = Unpooled.buffer();
        random = new Random(8521);
    }

    @Test
    public void testIsSupported() throws Exception {

        assertTrue(codec.isSupported(Key.class));
        assertTrue(codec.isSupported(SubKey.class));
        assertFalse(codec.isSupported(String.class));
        assertFalse(codec.isSupported(null));
    }

    @Test
    public void testCodec() throws Exception {

        final List<Key> keys = new ArrayList<>();

        final KeySupplier keySupplier = new KeySupplier(444);

        for (int i = 0; i < 1000; i++) {
            keys.add(keySupplier.get());
        }

        keys.add(null);
        keys.add(Key.valueOf(Long.MAX_VALUE));
        keys.add(Key.valueOf(Long.MIN_VALUE));
        keys.add(Key.valueOf(Integer.MAX_VALUE));
        keys.add(Key.valueOf(Integer.MIN_VALUE));
        keys.add(Key.valueOf(random.nextInt()));
        keys.add(Key.valueOf(random.nextLong()));

        for (Key key : keys) {
            codec.encode(key, buffer, PeerCodecs.getInstance(), Key.class);
            assertEquals(key, codec.decode(buffer, PeerCodecs.getInstance(), Key.class));
        }
    }

    private static class SubKey extends Key {

        private static final long serialVersionUID = 712247879535533478L;

        public SubKey(final byte[] value) {

            super(value);
        }
    }
}
