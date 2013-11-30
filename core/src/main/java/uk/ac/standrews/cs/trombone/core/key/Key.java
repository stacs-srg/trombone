package uk.ac.standrews.cs.trombone.core.key;

import java.io.Serializable;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class Key implements Comparable<Key>, Serializable {

    public static final BigInteger TWO = BigInteger.valueOf(2);
    private static final int SHORT_SIZE_IN_BYTES = Short.SIZE / Byte.SIZE;
    private static final int INTEGER_SIZE_IN_BYTES = Integer.SIZE / Byte.SIZE;
    private static final int LONG_SIZE_IN_BYTES = Long.SIZE / Byte.SIZE;
    private static final long serialVersionUID = -9022058863275074475L;
    private final byte[] value;
    private final int length;
    private Integer hashcode;

    public Key(final byte[] value) {

        this.value = value;
        length = value.length * Byte.SIZE;
    }

    public static Number getKeySpaceSize(Key key) {

        return getKeySpaceSize(key.getLength());
    }

    public static Number getKeySpaceSize(int key_length) {

        return TWO.pow(key_length);
    }

    public static Key valueOf(final long value) {

        final byte[] key_value = ByteBuffer.allocate(LONG_SIZE_IN_BYTES).putLong(value).array();
        return new Key(key_value);
    }

    public static Key valueOf(final short value) {

        final byte[] key_value = ByteBuffer.allocate(SHORT_SIZE_IN_BYTES).putShort(value).array();
        return new Key(key_value);
    }

    public static Key valueOf(final int value) {

        final byte[] key_value = ByteBuffer.allocate(INTEGER_SIZE_IN_BYTES).putInt(value).array();
        return new Key(key_value);
    }

    public byte[] getValue() {

        return value;
    }

    public int getLength() {

        return length;
    }

    public int compareRingDistance(Key first, Key second) {

        final int first_to_second = first.compareTo(second);
        final int this_to_first = compareTo(first);
        final int this_to_second = compareTo(second);

        if (first_to_second == 0) {
            assert first.equals(second) && this_to_first == this_to_second;
            return 0;
        }

        assert !first.equals(second);

        if (this_to_first == 0) { return -1; }
        if (this_to_second == 0) { return 1; }
        if (this_to_first == this_to_second) { return first_to_second > 0 ? 1 : -1; }
        return first_to_second > 0 ? -1 : 1;
    }

    @Override
    public int compareTo(final Key other) {

        return compareTo(getValue(), other.getValue());
    }

    @Override
    public boolean equals(final Object other) {

        if (this == other) { return true; }
        if (!(other instanceof Key)) { return false; }
        final Key that = (Key) other;
        return Arrays.equals(getValue(), that.getValue());
    }

    @Override
    public int hashCode() {

        if (hashcode == null) {
            hashcode = Arrays.hashCode(getValue());
        }
        return hashcode;
    }

    private static int compareTo(final byte[] first, final byte[] second) {

        final int first_length = first.length;
        final int second_length = second.length;

        if (first_length == second_length) {
            for (int i = 0; i < first_length; i++) {
                final int ith_byte_comparison = Byte.compare(first[i], second[i]);
                if (ith_byte_comparison != 0) { return ith_byte_comparison > 0 ? 1 : -1; }
            }
            return 0;
        }
        throw new IllegalArgumentException("keys are of different lengths");
    }
}
