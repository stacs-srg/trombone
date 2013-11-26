package uk.ac.standrews.cs.trombone.core.rpc.codec;

import io.netty.buffer.ByteBuf;
import java.lang.reflect.Type;
import org.mashti.jetson.exception.RPCException;
import org.mashti.jetson.lean.codec.Codec;
import org.mashti.jetson.lean.codec.Codecs;
import uk.ac.standrews.cs.trombone.core.key.IntegerKey;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class IntegerKeyCodec implements Codec {

    @Override
    public boolean isSupported(final Type type) {

        return type != null && type instanceof Class<?> && IntegerKey.class.isAssignableFrom((Class<?>) type);
    }

    @Override
    public void encode(final Object value, final ByteBuf out, final Codecs codecs, final Type type) throws RPCException {

        final IntegerKey key = (IntegerKey) value;
        codecs.encodeAs(key.getValue(), out, Integer.class);
    }

    @Override
    public IntegerKey decode(final ByteBuf in, final Codecs codecs, final Type type) throws RPCException {

        final Integer key_value = codecs.decodeAs(in, Integer.class);
        return new IntegerKey(key_value);
    }
}
