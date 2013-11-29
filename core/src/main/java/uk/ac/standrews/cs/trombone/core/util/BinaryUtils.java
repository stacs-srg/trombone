package uk.ac.standrews.cs.trombone.core.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public final class BinaryUtils {

    public static final int INTEGER_SIZE_IN_BYTES = Integer.SIZE / Byte.SIZE;
    public static final int LONG_SIZE_IN_BYTES = Long.SIZE / Byte.SIZE;

    private BinaryUtils() {

    }

    public static byte[] toBytes(int value) {

        return ByteBuffer.allocate(INTEGER_SIZE_IN_BYTES).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array();
    }

    public static byte[] toBytes(final long value) {

        return ByteBuffer.allocate(LONG_SIZE_IN_BYTES).order(ByteOrder.LITTLE_ENDIAN).putLong(value).array();
    }
}
