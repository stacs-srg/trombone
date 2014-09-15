package uk.ac.standrews.cs.trombone.core.rpc.codec;

import io.netty.buffer.ByteBuf;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import org.mashti.jetson.exception.RPCException;
import org.mashti.jetson.lean.codec.Codec;
import org.mashti.jetson.lean.codec.Codecs;
import uk.ac.standrews.cs.trombone.core.Key;
import uk.ac.standrews.cs.trombone.core.PeerReference;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class PeerReferenceCodec implements Codec {

    @Override
    public boolean isSupported(final Type type) {

        return type != null && type instanceof Class<?> && PeerReference.class.isAssignableFrom((Class<?>) type);
    }

    @Override
    public void encode(final Object value, final ByteBuf out, final Codecs codecs, final Type type) throws RPCException {

        if (value == null) {
            out.writeBoolean(true);
        }
        else {
            out.writeBoolean(false);
            final PeerReference reference = (PeerReference) value;
            codecs.encodeAs(reference.getKey(), out, Key.class);
            codecs.encodeAs(reference.getAddress(), out, InetSocketAddress.class);
        }
    }

    @Override
    public PeerReference decode(final ByteBuf in, final Codecs codecs, final Type type) throws RPCException {

        final Boolean is_null = in.readBoolean();
        if (is_null) { return null; }
        final Key key = codecs.decodeAs(in, Key.class);
        final InetSocketAddress address = codecs.decodeAs(in, InetSocketAddress.class);
        return new PeerReference(key, address);
    }
}
