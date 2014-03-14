package uk.ac.standrews.cs.trombone.core.rpc.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.reflect.FieldUtils;
import org.junit.Before;
import org.junit.Test;
import uk.ac.standrews.cs.trombone.core.PeerReference;
import uk.ac.standrews.cs.trombone.core.key.Key;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class PeerReferenceListCodecTest {

    private PeerReferenceListCodec codec;
    private ByteBuf buffer;
    List<PeerReference> reference_list;
    List<SubPeerReference> super_reference_list;
    List<String> string_list;
    Type reference_list_type;
    Type super_reference_list_type;
    Type string_list_type;

    @Before
    public void setUp() throws Exception {

        codec = new PeerReferenceListCodec();
        buffer = Unpooled.buffer();
        reference_list = new ArrayList<>();
        string_list_type = FieldUtils.getField(getClass(), "string_list", true).getGenericType();
        reference_list_type = FieldUtils.getField(getClass(), "reference_list", true).getGenericType();
        super_reference_list_type = FieldUtils.getField(getClass(), "super_reference_list", true).getGenericType();
    }

    @Test
    public void testIsSupported() throws Exception {

        assertTrue(codec.isSupported(reference_list_type));
        assertTrue(codec.isSupported(super_reference_list_type));
        assertFalse(codec.isSupported(string_list_type));
        assertFalse(codec.isSupported(List.class));
        assertFalse(codec.isSupported(PeerReference.class));
        assertFalse(codec.isSupported(null));
    }

    @Test
    public void testCodec() throws Exception {

        final PeerReference[] references = {null, new PeerReference(Key.valueOf(45), new InetSocketAddress(8541)), new PeerReference(Key.valueOf(333), new InetSocketAddress(85), false)};
        for (PeerReference reference : references) {
            reference_list.add(reference);
        }
        codec.encode(reference_list, buffer, PeerCodecs.INSTANCE, reference_list_type);
        assertEquals(reference_list, codec.decode(buffer, PeerCodecs.INSTANCE, reference_list_type));
    }

    private static class SubPeerReference extends PeerReference {

        public SubPeerReference(final Key key, final InetSocketAddress address) {

            super(key, address);
        }
    }
}
