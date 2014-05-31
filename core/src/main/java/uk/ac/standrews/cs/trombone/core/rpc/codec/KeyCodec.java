package uk.ac.standrews.cs.trombone.core.rpc.codec;

import io.netty.buffer.ByteBuf;
import java.lang.reflect.Type;
import org.mashti.jetson.exception.RPCException;
import org.mashti.jetson.lean.codec.Codec;
import org.mashti.jetson.lean.codec.Codecs;
import uk.ac.standrews.cs.trombone.core.key.Key;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class KeyCodec implements Codec {

    @Override
    public boolean isSupported(final Type type) {

        return Class.class.isInstance(type) && Key.class.isAssignableFrom(Class.class.cast(type));
    }

    @Override
    public void encode(final Object value, final ByteBuf out, final Codecs codecs, final Type type) throws RPCException {

        if (value == null) {
            out.writeBoolean(true);
        }
        else {
            out.writeBoolean(false);
            final Key key = (Key) value;
            final byte[] key_value = key.getValue();
            codecs.encodeAs(key_value, out, byte[].class);
        }
    }

    @Override
    public Key decode(final ByteBuf in, final Codecs codecs, final Type type) throws RPCException {

        final Boolean is_null = in.readBoolean();
        if (is_null) { return null; }

        final byte[] key_value = codecs.decodeAs(in, byte[].class);
        return new Key(key_value);
    }
}
