package uk.ac.standrews.cs.trombone.core.rpc.codec;

import io.netty.buffer.ByteBuf;
import java.lang.reflect.Type;
import org.mashti.jetson.exception.RPCException;
import org.mashti.jetson.lean.codec.Codec;
import org.mashti.jetson.lean.codec.Codecs;
import uk.ac.standrews.cs.trombone.core.key.Key;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class KeyCodec implements Codec {

    public static final int MAX_KEY_VALUE_LENGTH = 256;

    @Override
    public boolean isSupported(final Type type) {

        return type != null && type instanceof Class<?> && Key.class.isAssignableFrom((Class<?>) type);
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
            final int length = key_value.length;
            writeKeyLength(out, length);
            out.writeBytes(key_value);
        }
    }

    @Override
    public Key decode(final ByteBuf in, final Codecs codecs, final Type type) throws RPCException {

        final Boolean is_null = in.readBoolean();
        if (is_null) { return null; }

        final int length = readKeyLength(in);
        final byte[] key_value = new byte[length];
        in.readBytes(key_value);

        return new Key(key_value);
    }

    private static int readKeyLength(final ByteBuf in) {

        return in.readUnsignedByte();
    }

    private static void writeKeyLength(final ByteBuf out, final int length) {

        if (length > MAX_KEY_VALUE_LENGTH) { throw new UnsupportedOperationException("Key codec cannot handle keys longer than " + MAX_KEY_VALUE_LENGTH + " bytes"); }
        out.writeByte((byte) length);
    }
}
