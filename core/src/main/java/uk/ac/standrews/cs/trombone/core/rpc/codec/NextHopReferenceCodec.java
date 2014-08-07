package uk.ac.standrews.cs.trombone.core.rpc.codec;

import io.netty.buffer.ByteBuf;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import org.mashti.jetson.exception.RPCException;
import org.mashti.jetson.lean.codec.Codec;
import org.mashti.jetson.lean.codec.Codecs;
import uk.ac.standrews.cs.trombone.core.NextHopReference;
import uk.ac.standrews.cs.trombone.core.PeerReference;
import uk.ac.standrews.cs.trombone.core.Key;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class NextHopReferenceCodec implements Codec {

    @Override
    public boolean isSupported(final Type type) {

        return type != null && type instanceof Class<?> && NextHopReference.class.isAssignableFrom((Class<?>) type);
    }

    @Override
    public void encode(final Object value, final ByteBuf out, final Codecs codecs, final Type type) throws RPCException {

        if (value == null) {
            out.writeBoolean(true);
        }
        else {
            out.writeBoolean(false);
            final NextHopReference next_hop_reference = (NextHopReference) value;
            codecs.encodeAs(next_hop_reference.getKey(), out, Key.class);
            codecs.encodeAs(next_hop_reference.getAddress(), out, InetSocketAddress.class);
            codecs.encodeAs(next_hop_reference.isReachable(), out, Boolean.TYPE);
            codecs.encodeAs(next_hop_reference.isFinalHop(), out, Boolean.TYPE);
        }
    }

    @Override
    public NextHopReference decode(final ByteBuf in, final Codecs codecs, final Type type) throws RPCException {

        final Boolean is_null = in.readBoolean();
        if (is_null) { return null; }
        final Key key = codecs.decodeAs(in, Key.class);
        final InetSocketAddress address = codecs.decodeAs(in, InetSocketAddress.class);
        final Boolean reachable = codecs.decodeAs(in, Boolean.TYPE);
        final Boolean final_hop = codecs.decodeAs(in, Boolean.TYPE);
        final PeerReference reference = new PeerReference(key, address, reachable);
        return new NextHopReference(reference, final_hop);
    }
}
