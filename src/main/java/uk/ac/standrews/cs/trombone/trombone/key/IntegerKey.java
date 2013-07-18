package uk.ac.standrews.cs.trombone.trombone.key;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class IntegerKey implements Key<Integer> {

    private static final Long INTEGER_KEY_SPACE_SIZE = Double.valueOf(Math.pow(2, Integer.SIZE)).longValue();
    private final Integer value;

    public IntegerKey(final Integer value) {

        this.value = value;
    }

    @Override
    public int compareTo(final Key<Integer> other) {

        return value.compareTo(other.getValue());
    }

    @Override
    public Integer getValue() {

        return value;
    }

    @Override
    public Long getRingDistanceTo(final Key<Integer> other) {

        final Integer other_value = other.getValue();
        final Long distance = other_value.longValue() - value.longValue();
        return distance < 0 ? distance + getKeySpaceSize() : distance;
    }

    @Override
    public Long getKeySpaceSize() {

        return INTEGER_KEY_SPACE_SIZE;
    }

    @Override
    public int compareRingDistance(final Key<Integer> first, final Key<Integer> second) {

        final Long distance_to_first = getRingDistanceTo(first);
        final Long distance_to_second = getRingDistanceTo(second);
        return distance_to_first.compareTo(distance_to_second);
    }

    @Override
    public int hashCode() {

        return value.hashCode();
    }

    @Override
    public boolean equals(final Object other) {

        if (this == other) { return true; }
        if (!(other instanceof IntegerKey)) { return false; }
        final IntegerKey that = (IntegerKey) other;
        return value.equals(that.value);
    }

    @Override
    public String toString() {

        return String.valueOf(value);
    }
}
