package uk.ac.standrews.cs.trombone.core.rpc.codec;

import io.netty.buffer.ByteBuf;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import org.mashti.jetson.exception.InvalidResponseException;
import org.mashti.jetson.exception.RPCException;
import org.mashti.jetson.lean.codec.Codec;
import org.mashti.jetson.lean.codec.Codecs;
import uk.ac.standrews.cs.trombone.core.selector.Selector;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class SelectorCodec implements Codec {

    @Override
    public boolean isSupported(final Type type) {

        return type != null && type instanceof Class<?> && Selector.class.isAssignableFrom((Class<?>) type);
    }

    @Override
    public void encode(final Object value, final ByteBuf out, final Codecs codecs, final Type type) throws RPCException {

        if (value == null) {
            out.writeBoolean(true);
        }
        else {
            out.writeBoolean(false);
            final Selector selector = (Selector) value;
            final boolean singleton = selector.isSingleton();
            out.writeBoolean(singleton);
            codecs.encodeAs(selector.getClass()
                    .getName(), out, String.class);
            if (!singleton) {
                codecs.encodeAs(selector.getSelectionSize(), out, Integer.class);
            }
        }
    }

    @Override
    public Selector decode(final ByteBuf in, final Codecs codecs, final Type type) throws RPCException {

        final Boolean is_null = in.readBoolean();
        if (is_null) { return null; }
        final Boolean singleton = in.readBoolean();
        final String class_name = codecs.decodeAs(in, String.class);
        final Class<?> selector_class;

        try {
            selector_class = Class.forName(class_name);
        }
        catch (ClassNotFoundException e) {
            throw new InvalidResponseException(e);
        }

        if (singleton) {
            try {
                return (Selector) selector_class.getMethod("getInstance")
                        .invoke(null);
            }
            catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new InvalidResponseException(e);
            }
        }
        else {
            final int selection_size = codecs.decodeAs(in, Integer.class);
            Constructor<?> constructor;
            try {
                try {
                    constructor = selector_class.getConstructor(Integer.class);
                    return (Selector) constructor.newInstance(selection_size);
                }
                catch (NoSuchMethodException e) {

                    constructor = selector_class.getConstructor();
                    return (Selector) constructor.newInstance();
                }
            }
            catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
                throw new InvalidResponseException(e);
            }
        }
    }
}
