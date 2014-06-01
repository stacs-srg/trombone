package uk.ac.standrews.cs.trombone.core.key;

import java.math.BigInteger;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class Key extends Number implements Comparable<Key> {

    private static final long serialVersionUID = -9022058863275074475L;
    private final BigInteger value;
    private Integer hashcode;

    public Key(final byte[] value) {

        this(new BigInteger(value));
    }

    public Key(final BigInteger value) {

        this.value = value;
    }

    public static Key valueOf(long value) {

        return valueOf(BigInteger.valueOf(value));
    }

    public static Key valueOf(final byte[] value) {

        return new Key(value);
    }

    public static Key valueOf(final BigInteger value) {

        return new Key(value);
    }

    public BigInteger getValue() {

        return value;
    }

    public byte[] toByteArray() {

        return value.toByteArray();
    }

    public int compareRingDistance(Key first, Key second) {

        final int first_to_second = first.compareTo(second);
        if (first_to_second == 0) { return 0; }

        final int this_to_first = compareTo(first);
        if (this_to_first == 0) { return -1; }

        final int this_to_second = compareTo(second);
        if (this_to_second == 0) { return 1; }

        if (this_to_first * this_to_second > 0) { return first_to_second > 0 ? 1 : -1; }
        return first_to_second > 0 ? -1 : 1;
    }

    @Override
    public int compareTo(final Key other) {

        return value.compareTo(other.value);
    }

    @Override
    public boolean equals(final Object other) {

        if (this == other) { return true; }
        if (!(other instanceof Key)) { return false; }
        final Key that = (Key) other;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {

        if (hashcode == null) {
            hashcode = value.hashCode();
        }
        return hashcode;
    }

    @Override
    public byte byteValue() {

        return value.byteValue();
    }

    @Override
    public short shortValue() {

        return value.shortValue();
    }

    @Override
    public int intValue() {

        return value.intValue();
    }

    @Override
    public long longValue() {

        return value.longValue();
    }

    @Override
    public float floatValue() {

        return value.floatValue();
    }

    @Override
    public double doubleValue() {

        return value.doubleValue();
    }
}
