package uk.ac.standrews.cs.trombone.core.key;

import java.math.BigInteger;
import uk.ac.standrews.cs.trombone.core.PeerConfiguration;

import static java.math.BigInteger.ONE;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class Key extends Number implements Comparable<Key> {

    private static final long serialVersionUID = -9022058863275074475L;

    public static final BigInteger TWO = ONE.add(ONE);

    public static final int KEY_LENGTH = PeerConfiguration.KEY_LENGTH;
    public static final BigInteger KEYSPACE_SIZE = TWO.pow(KEY_LENGTH);
    public static final BigInteger MAX_KEY_VALUE = KEYSPACE_SIZE.subtract(ONE);
    public static final BigInteger MIN_KEY_VALUE = BigInteger.ZERO;
    public static final Key MAX_VALUE = valueOf(MAX_KEY_VALUE);
    public static final Key MIN_VALUE = valueOf(MIN_KEY_VALUE);

    private final BigInteger value;
    private Integer hashcode;

    public Key(final byte[] value) {

        this(new BigInteger(value));
    }

    public Key(final BigInteger value) {

        if (value.compareTo(MAX_KEY_VALUE) > 0) {
            this.value = value.remainder(KEYSPACE_SIZE);
        }
        else if (value.compareTo(MIN_KEY_VALUE) < 0) {

            this.value = value.remainder(KEYSPACE_SIZE).add(KEYSPACE_SIZE);
        }
        else {
            this.value = value;
        }
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

    public Key next() {

        return equals(MAX_VALUE) ? MIN_VALUE : valueOf(value.add(ONE));
    }

    public Key previous() {

        return equals(MIN_VALUE) ? MAX_VALUE : valueOf(value.subtract(ONE));
    }

    /**
     * Compares the ring distance of this key to first key, with the distance of this key to second key.
     *
     * @param first the first key
     * @param second the second key
     * @return {@code -1}, {@code 0} or {@code 1} as the distance of this to first key is less than, equal or greater than the distance of this to second key.
     */
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

    /**
     * Calculates the ring distance from this to other, where ringDistance(k, k) = 0, and in general ringDistance(k1, other) != ringDistance(other, k1).
     *
     * @param other the second key
     * @return the ring distance from k1 to other
     */
    public BigInteger ringDistance(final Key other) {

        final BigInteger distance = other.getValue().subtract(value);
        return distance.compareTo(MIN_KEY_VALUE) < 0 ? distance.add(KEYSPACE_SIZE) : distance;
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

    @Override
    public String toString() {

        return value.toString();
    }

    public int bitLength() {

        return value.bitLength();
    }
}
