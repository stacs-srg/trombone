package uk.ac.standrews.cs.trombone.trombone.codec;

import org.mashti.jetson.exception.RPCException;
import org.mashti.jetson.exception.UnknownTypeException;
import org.mashti.jetson.lean.codec.Codec;
import org.mashti.jetson.lean.codec.Codecs;
import io.netty.buffer.ByteBuf;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import uk.ac.standrews.cs.trombone.trombone.key.IntegerKey;
import uk.ac.standrews.cs.trombone.trombone.key.Key;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class KeyCodec implements Codec {

    private static final Map<Byte, Class<? extends Key>> KEY_TYPE_MAP = new HashMap<Byte, Class<? extends Key>>();
    private static final Byte INTEGER_KEY_TYPE_ID = 0x0;
    static {
        KEY_TYPE_MAP.put(INTEGER_KEY_TYPE_ID, IntegerKey.class);
    }

    @Override
    public boolean isSupported(final Type type) {

        return type != null && type instanceof Class<?> && Key.class.isAssignableFrom((Class<?>) type);
    }

    @Override
    public void encode(final Object value, final ByteBuf out, final Codecs codecs, final Type type) throws RPCException {

        for (Map.Entry<Byte, Class<? extends Key>> key_type_entry : KEY_TYPE_MAP.entrySet()) {
            final Class<? extends Key> key_type = key_type_entry.getValue();

            if (key_type.isInstance(value)) {

                out.writeByte(key_type_entry.getKey());
                codecs.encodeAs(value, out, key_type);
                return;
            }
        }

        throw new UnknownTypeException("no codec is registered for type: " + value.getClass());
    }

    @Override
    public <Value> Value decode(final ByteBuf in, final Codecs codecs, final Type type) throws RPCException {

        final Byte key_type_code = in.readByte();
        if (KEY_TYPE_MAP.containsKey(key_type_code)) { return codecs.decodeAs(in, KEY_TYPE_MAP.get(key_type_code)); }
        throw new UnknownTypeException("unknown key type code " + key_type_code);
    }
}
