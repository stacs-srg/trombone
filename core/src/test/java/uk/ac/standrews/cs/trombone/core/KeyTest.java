package uk.ac.standrews.cs.trombone.core;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;
import org.junit.Assert;
import org.junit.Test;
import uk.ac.standrews.cs.trombone.core.util.RelativeRingDistanceComparator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

/**
 * @author Graham Kirby (graham.kirby@st-andrews.ac.uk)
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class KeyTest {

    static final BigInteger MINUS_ONE = new BigInteger("-1");
    static final BigInteger ZERO = BigInteger.ZERO;
    static final BigInteger ONE = BigInteger.ONE;
    static final BigInteger TWO = new BigInteger("2");
    static final BigInteger THREE = new BigInteger("3");
    static final BigInteger FOUR = new BigInteger("4");

    static final BigInteger ARBITRARY_VALUE = new BigInteger("12358");
    static final BigInteger MAX_VALUE = Key.KEYSPACE_SIZE.subtract(BigInteger.ONE);

    static final BigInteger QUARTER_VALUE = Key.KEYSPACE_SIZE.divide(FOUR);
    static final BigInteger HALF_VALUE = QUARTER_VALUE.multiply(TWO);
    static final BigInteger THREE_QUARTER_VALUE = QUARTER_VALUE.multiply(THREE);

    static final Key ZERO_KEY = new Key(BigInteger.ZERO);
    static final Key ONE_KEY = new Key(BigInteger.ONE);
    static final Key ARBITRARY_KEY = new Key(ARBITRARY_VALUE);
    static final Key MAX_KEY = new Key(MAX_VALUE);

    static final Key QUARTER_KEY = new Key(QUARTER_VALUE);
    static final Key HALF_KEY = new Key(HALF_VALUE);
    static final Key THREE_QUARTER_KEY = new Key(THREE_QUARTER_VALUE);
    static final Key[] TEST_KEYS = {
            ZERO_KEY, ONE_KEY, ARBITRARY_KEY, MAX_KEY, QUARTER_KEY, HALF_KEY, THREE_QUARTER_KEY
    };

    private static final Key five = Key.valueOf(5);
    private static final Key one = Key.valueOf(1);
    private static final Key seven = Key.valueOf(7);
    private static final Key zero = Key.valueOf(0);
    private static final double DELTA = 1e-15;
    private static final Random random = new Random(894156);

    /**
     * Tests whether the bit lengths of the keys are as expected.
     */
    @Test
    public void bitLength() {

        final int arbitrary_length = 14;

        assertEquals(0, ZERO_KEY.bitLength());
        assertEquals(1, ONE_KEY.bitLength());
        assertEquals(arbitrary_length, ARBITRARY_KEY.bitLength());
        assertEquals(MAX_KEY.bitLength(), Key.KEY_LENGTH);
    }

    /**
     * Tests whether key comparison works as expected.
     */
    @Test
    public void comparison() {

        assertEquals(1, MAX_KEY.compareTo(ZERO_KEY));
        assertEquals(1, MAX_KEY.compareTo(ONE_KEY));
        assertEquals(1, MAX_KEY.compareTo(ARBITRARY_KEY));

        assertEquals(-1, ZERO_KEY.compareTo(MAX_KEY));
        assertEquals(-1, ZERO_KEY.compareTo(ONE_KEY));
        assertEquals(-1, ZERO_KEY.compareTo(ARBITRARY_KEY));

        assertEquals(0, ZERO_KEY.compareTo(ZERO_KEY));
        assertEquals(0, MAX_KEY.compareTo(MAX_KEY));

        // Miscellaneous pairs.
        assertEquals(-1, ARBITRARY_KEY.compareTo(MAX_KEY));
        assertEquals(1, ARBITRARY_KEY.compareTo(ZERO_KEY));
        assertEquals(1, ARBITRARY_KEY.compareTo(ONE_KEY));
        assertEquals(0, ARBITRARY_KEY.compareTo(ARBITRARY_KEY));
    }

    @Test
    public void testNextPrevious() throws Exception {

        assertEquals(Key.valueOf(6), five.next());
        assertEquals(Key.valueOf(7), five.next()
                .next());
        assertEquals(Key.valueOf(8), five.next()
                .next()
                .next());
        assertEquals(Key.valueOf(7), five.next()
                .next()
                .next()
                .previous());
        assertEquals(Key.MIN_VALUE, Key.MAX_VALUE.next());
        assertEquals(Key.MAX_VALUE, Key.MIN_VALUE.previous());
        assertEquals(Key.MAX_VALUE.previous(), Key.MIN_VALUE.previous()
                .previous());
    }

    /**
     * Tests whether ring distance calculation works as expected.
     */
    @Test
    public void ringDistanceSpecific() {

        // Distances from keyspace_size - 1.
        assertEquals(MAX_KEY.ringDistance(ZERO_KEY), BigInteger.ONE);
        assertEquals(MAX_KEY.ringDistance(ONE_KEY), Key.TWO);
        assertEquals(MAX_KEY.ringDistance(ARBITRARY_KEY), ARBITRARY_VALUE.add(ONE));

        // Distances from 0.
        assertEquals(ZERO_KEY.ringDistance(ONE_KEY), BigInteger.ONE);
        assertEquals(ZERO_KEY.ringDistance(ARBITRARY_KEY), ARBITRARY_VALUE);
        assertEquals(ZERO_KEY.ringDistance(MAX_KEY), MAX_VALUE);

        // Miscellaneous pairs.
        assertEquals(QUARTER_KEY.ringDistance(HALF_KEY), QUARTER_VALUE);
        assertEquals(HALF_KEY.ringDistance(THREE_QUARTER_KEY), QUARTER_VALUE);
        assertEquals(THREE_QUARTER_KEY.ringDistance(ZERO_KEY), QUARTER_VALUE);
        assertEquals(QUARTER_KEY.ringDistance(THREE_QUARTER_KEY), HALF_VALUE);
        assertEquals(THREE_QUARTER_KEY.ringDistance(QUARTER_KEY), HALF_VALUE);
        assertEquals(HALF_KEY.ringDistance(QUARTER_KEY), THREE_QUARTER_VALUE);
    }

    /**
     * Tests whether ring distance calculation works as expected.
     */
    @Test
    public void ringDistanceGeneral() {

        assertRingDistanceInvariantsForSingleKeys(TEST_KEYS);
        assertRingDistanceInvariantsForPairsOfDifferentKeys(TEST_KEYS);
    }

    /**
     * Tests conditions that should hold for all key values.
     *
     * @param k a key
     */
    private void assertRingDistanceInvariants(final Key k) {

        assertEquals(ZERO, k.ringDistance(k));
        assertEquals(ONE, k.ringDistance(offsetKey(k, ONE)));
        assertEquals(MAX_VALUE, k.ringDistance(offsetKey(k, MINUS_ONE)));
    }

    /**
     * Tests conditions that should hold for all pairs of different key values.
     *
     * @param k1 the first key
     * @param k2 the second key
     */
    private static void assertRingDistanceInvariantsForDifferentKeys(final Key k1, final Key k2) {

        // Check assumption for this test.
        assertNotSame(k1, k2);

        final BigInteger distance_forwards = k1.ringDistance(k2);
        final BigInteger distance_backwards = k2.ringDistance(k1);

        // Distances must themselves lie within the key range.
        assertWithinKeyRange(distance_forwards);
        assertWithinKeyRange(distance_backwards);

        // For different keys, the sum of the two distances must equal the ring size.
        Assert.assertEquals(Key.KEYSPACE_SIZE, distance_forwards.add(distance_backwards));
    }

    static void assertWithinKeyRange(final BigInteger value) {

        assertTrue(value.compareTo(ZERO) >= 0);
        assertTrue(value.compareTo(Key.KEYSPACE_SIZE) < 0);
    }

    private void assertRingDistanceInvariantsForSingleKeys(final Key[] keys) {

        for (final Key k : keys) {
            assertRingDistanceInvariants(k);
        }
    }

    private void assertRingDistanceInvariantsForPairsOfDifferentKeys(final Key[] keys) {

        for (final Key k1 : keys) {
            for (final Key k2 : keys) {

                if (!k1.equals(k2)) {
                    assertRingDistanceInvariantsForDifferentKeys(k1, k2);
                }
            }
        }
    }

    @Test
    public void testCompareRingDistance() throws Exception {

        assertTrue(five.compareRingDistance(zero, one) < 0);
        assertTrue(one.compareRingDistance(Key.valueOf(99), Key.valueOf(2)) > 0);
        assertTrue(one.compareRingDistance(Key.valueOf(2), Key.valueOf(99)) < 0);
        assertEquals(0, one.compareRingDistance(Key.valueOf(1), Key.valueOf(1)));
        assertEquals(0, one.compareRingDistance(Key.valueOf(-1), Key.valueOf(-1)));
        assertEquals(0, one.compareRingDistance(Key.valueOf(100), Key.valueOf(100)));

        final Key start_key = Key.valueOf(random.nextInt());
        final TreeSet<Key> keys = new TreeSet<Key>(new RelativeRingDistanceComparator(start_key));

        for (int i = 0; i < 1000; i++) {
            keys.add(Key.valueOf(random.nextInt()));
        }

        final List<Key> keys_as_list = new ArrayList<Key>(keys);

        for (int i = 0; i < keys_as_list.size() - 1; i++) {
            final Key ith = keys_as_list.get(i);
            final Key i_plus_one_th = keys_as_list.get(i + 1);
            assertTrue(start_key.compareRingDistance(ith, i_plus_one_th) < 0);
        }
    }

    @Test
    public void testCompareTo() throws Exception {

        assertTrue(five.compareTo(one) > 0);
        assertTrue(one.compareTo(five) < 0);
        assertTrue(one.compareTo(zero) > 0);
        assertEquals(one.compareTo(five), -five.compareTo(one));
        assertTrue(five.compareTo(seven) < 0);
        assertEquals(0, five.compareTo(five));
        assertEquals(0, five.compareTo(Key.valueOf(5)));
    }

    @Test
    public void testEquals() throws Exception {

        assertEquals(five, five);
        assertEquals(five, Key.valueOf(5));
        assertNotEquals(five, Key.valueOf(555));
        assertEquals(five.hashCode(), Key.valueOf(5)
                .hashCode());
        assertNotEquals(five.hashCode(), Key.valueOf(555)
                .hashCode());
    }

    @Test
    public void testGetValue() throws Exception {

        assertEquals(BigInteger.valueOf(5), five.getValue());
        assertEquals(BigInteger.ZERO, zero.getValue());

    }

    @Test
    public void testIntValue() throws Exception {

        for (int i = 0; i < 1000; i++) {
            final Integer value = random.nextInt(2556);
            assertEquals(value.intValue(), Key.valueOf(value)
                    .intValue());
        }
    }

    @Test
    public void testLongValue() throws Exception {

        for (int i = 0; i < 1000; i++) {
            final Integer value = Math.abs(random.nextInt(8524));
            assertEquals(value.longValue(), Key.valueOf(value)
                    .longValue());
        }
    }

    @Test
    public void testShortValue() throws Exception {

        for (int i = 0; i < 1000; i++) {
            final Short value = (short) (random.nextInt() - Short.MIN_VALUE);
            assertEquals(value.shortValue(), Key.valueOf(value)
                    .shortValue());
        }
    }

    @Test
    public void testByteValue() throws Exception {

        for (int i = 0; i < 1000; i++) {
            final BigInteger big_integer = BigInteger.valueOf(Math.abs(random.nextInt()));
            final Key key = new Key(big_integer);

            assertEquals(big_integer.byteValue(), key.byteValue());
        }
    }

    @Test
    public void testToString() throws Exception {

        assertEquals(BigInteger.valueOf(5)
                .toString(), five.toString());

    }

    /**
     * Tests whether keys created with values within the key range retain their original values.
     */
    @Test
    public void creation() {

        assertEquals(ZERO_KEY.getValue(), BigInteger.ZERO);
        assertEquals(ONE_KEY.getValue(), BigInteger.ONE);
        assertEquals(ARBITRARY_KEY.getValue(), ARBITRARY_VALUE);
        assertEquals(MAX_KEY.getValue(), MAX_VALUE);
    }

    /**
     * Tests whether keys created with values outwith the key range are wrapped as expected.
     */
    @Test
    public void creationWithValuesOutwithKeyRange() {

        // -1 should wrap to keyspace_size - 1.
        assertEquals(new Key(new BigInteger("-1")).getValue(), MAX_VALUE);

        // keyspace_size should wrap to 0.
        assertEquals(new Key(Key.KEYSPACE_SIZE).getValue(), ZERO);

        // keyspace_size + 1 should wrap to 1.
        assertEquals(new Key(Key.KEYSPACE_SIZE.add(ONE)).getValue(), ONE);

        // 2 * keyspace_size + 1 should wrap to 1.
        assertEquals(new Key(Key.KEYSPACE_SIZE.multiply(TWO)
                .add(ONE)).getValue(), ONE);

        // -2 * keyspace_size + 1 should wrap to 1.
        assertEquals(new Key(Key.KEYSPACE_SIZE.multiply(MINUS_ONE)
                .multiply(TWO)
                .add(ONE)).getValue(), ONE);
    }

    /**
     * Tests whether ring ordering calculation works as expected.
     */
    @Test
    public void ringOrderingGeneral() {

        assertRingOrderingInvariantsForSingleKeys(TEST_KEYS);
        assertRingOrderingInvariantsForTriplesOfDifferentKeys(TEST_KEYS);
    }

    /**
     * Tests whether ring ordering calculation works as expected.
     */
    @Test
    public void ringOrderingSpecific() {

        assertRingOrderingInvariantsForOrderedKeys(MAX_KEY, ZERO_KEY, ONE_KEY);
        assertRingOrderingInvariantsForOrderedKeys(MAX_KEY, ONE_KEY, ARBITRARY_KEY);
        assertRingOrderingInvariantsForOrderedKeys(MAX_KEY, ZERO_KEY, ARBITRARY_KEY);

        assertRingOrderingInvariantsForOrderedKeys(ZERO_KEY, ONE_KEY, ARBITRARY_KEY);
        assertRingOrderingInvariantsForOrderedKeys(ZERO_KEY, ARBITRARY_KEY, MAX_KEY);
        assertRingOrderingInvariantsForOrderedKeys(ZERO_KEY, ONE_KEY, MAX_KEY);

        assertRingOrderingInvariantsForOrderedKeys(ARBITRARY_KEY, MAX_KEY, ZERO_KEY);
        assertRingOrderingInvariantsForOrderedKeys(ARBITRARY_KEY, ZERO_KEY, ONE_KEY);
        assertRingOrderingInvariantsForOrderedKeys(ARBITRARY_KEY, MAX_KEY, ONE_KEY);

        assertRingOrderingInvariantsForOrderedKeys(QUARTER_KEY, HALF_KEY, THREE_QUARTER_KEY);
    }

    /**
     * Tests whether ring ordering calculation works as expected.
     */
    @Test
    public void ringSegmentsGeneral() {

        assertRingSegmentInvariantsForSingleKeys(TEST_KEYS);
        assertRingSegmentInvariantsForTriplesOfDifferentKeys(TEST_KEYS);
    }

    /**
     * Tests whether segment calculation works as expected.
     */
    @Test
    public void segmentsSpecific() {

        assertRingSegmentInvariantsForOrderedDifferentKeys(MAX_KEY, ZERO_KEY, ONE_KEY);
        assertRingSegmentInvariantsForOrderedDifferentKeys(MAX_KEY, ONE_KEY, ARBITRARY_KEY);
        assertRingSegmentInvariantsForOrderedDifferentKeys(MAX_KEY, ZERO_KEY, ARBITRARY_KEY);

        assertRingSegmentInvariantsForOrderedDifferentKeys(ZERO_KEY, ONE_KEY, ARBITRARY_KEY);
        assertRingSegmentInvariantsForOrderedDifferentKeys(ZERO_KEY, ARBITRARY_KEY, MAX_KEY);
        assertRingSegmentInvariantsForOrderedDifferentKeys(ZERO_KEY, ONE_KEY, MAX_KEY);

        assertRingSegmentInvariantsForOrderedDifferentKeys(ARBITRARY_KEY, MAX_KEY, ZERO_KEY);
        assertRingSegmentInvariantsForOrderedDifferentKeys(ARBITRARY_KEY, ZERO_KEY, ONE_KEY);
        assertRingSegmentInvariantsForOrderedDifferentKeys(ARBITRARY_KEY, MAX_KEY, ONE_KEY);

        assertRingSegmentInvariantsForOrderedDifferentKeys(QUARTER_KEY, HALF_KEY, THREE_QUARTER_KEY);
    }

    /**
     * Tests conditions that should hold for all key values.
     *
     * @param k a key
     */
    private static void assertRingOrderingInvariants(final Key k) {

        final Key k_plus_one = offsetKey(k, ONE);
        final Key k_plus_two = offsetKey(k, TWO);

        // Elaborate the combinations of k/k+1 in each of the 3 positions.

        assertTrue(Key.inRingOrder(k, k, k));
        assertTrue(Key.inRingOrder(k, k, k_plus_one));
        assertTrue(Key.inRingOrder(k, k_plus_one, k));
        assertTrue(Key.inRingOrder(k, k_plus_one, k_plus_one));
        assertTrue(Key.inRingOrder(k_plus_one, k, k));
        assertTrue(Key.inRingOrder(k_plus_one, k, k_plus_one));
        assertTrue(Key.inRingOrder(k_plus_one, k_plus_one, k));
        assertTrue(Key.inRingOrder(k_plus_one, k_plus_one, k_plus_one));

        assertTrue(Key.inRingOrder(k, k_plus_one, k_plus_two));
        assertFalse(Key.inRingOrder(k, k_plus_two, k_plus_one));
    }

    /**
     * Tests conditions that should hold for all triples of different key values.
     *
     * @param k1 the first key
     * @param k2 the second key
     * @param k3 the third key
     */
    private static void assertRingOrderingInvariantsForArbitraryKeys(final Key k1, final Key k2, final Key k3) {

        assertTrue(xor(Key.inRingOrder(k1, k2, k3), Key.inRingOrder(k1, k3, k2)));

        if (Key.inRingOrder(k1, k2, k3)) {
            assertRingOrderingInvariantsForOrderedKeys(k1, k2, k3);
        }
        else {
            assertRingOrderingInvariantsForOrderedKeys(k1, k3, k2);
        }
    }

    /**
     * Tests conditions that should hold for triples of different key values that are ordered.
     *
     * @param k1 the first key
     * @param k2 the second key
     * @param k3 the third key
     */
    private static void assertRingOrderingInvariantsForOrderedKeys(final Key k1, final Key k2, final Key k3) {

        assertTrue(Key.inRingOrder(k1, k2, k3));
        assertTrue(Key.inRingOrder(k2, k3, k1));
        assertTrue(Key.inRingOrder(k3, k1, k2));

        assertTrue(Key.ringDistanceFurther(k1, k3, k2));
        assertTrue(Key.ringDistanceFurther(k3, k2, k1));
        assertTrue(Key.ringDistanceFurther(k2, k1, k3));

        assertFalse(Key.ringDistanceFurther(k1, k2, k3));
        assertFalse(Key.ringDistanceFurther(k2, k3, k1));
        assertFalse(Key.ringDistanceFurther(k3, k1, k2));
    }

    /**
     * Tests conditions that should hold for all key values.
     *
     * @param k a key
     */
    private static void assertRingSegmentInvariants(final Key k) {

        final Key k_plus_one = offsetKey(k, ONE);
        final Key k_plus_two = offsetKey(k, TWO);

        // Elaborate the combinations of k/k+1 in each of the 3 positions.

        assertTrue(Key.inSegment(k, k, k));
        assertFalse(Key.inSegment(k, k, k_plus_one));
        assertTrue(Key.inSegment(k, k_plus_one, k));
        assertTrue(Key.inSegment(k, k_plus_one, k_plus_one));
        assertTrue(Key.inSegment(k_plus_one, k, k));
        assertTrue(Key.inSegment(k_plus_one, k, k_plus_one));
        assertFalse(Key.inSegment(k_plus_one, k_plus_one, k));
        assertTrue(Key.inSegment(k_plus_one, k_plus_one, k_plus_one));

        assertTrue(Key.inSegment(k, k_plus_one, k_plus_two));
        assertFalse(Key.inSegment(k, k_plus_two, k_plus_one));
    }

    /**
     * Tests conditions that should hold for all triples of different key values.
     *
     * @param k1 the first key
     * @param k2 the second key
     * @param k3 the third key
     */
    private static void assertRingSegmentInvariantsForArbitraryKeys(final Key k1, final Key k2, final Key k3) {

        assertTrue(xor(Key.inSegment(k1, k2, k3), Key.inSegment(k1, k3, k2)));

        if (Key.inRingOrder(k1, k2, k3)) {
            assertRingSegmentInvariantsForOrderedDifferentKeys(k1, k2, k3);
        }
        else {
            assertRingSegmentInvariantsForOrderedDifferentKeys(k1, k3, k2);
        }
    }

    /**
     * Tests conditions that should hold for triples of different key values that are ordered.
     *
     * @param k1 the first key
     * @param k2 the second key
     * @param k3 the third key
     */
    private static void assertRingSegmentInvariantsForOrderedDifferentKeys(final Key k1, final Key k2, final Key k3) {

        // Check assumption for this test.
        assertTrue(allDifferent(k1, k2, k3));

        assertTrue(Key.inSegment(k1, k2, k3));
        assertTrue(Key.inSegment(k2, k3, k1));
        assertTrue(Key.inSegment(k3, k1, k2));
    }

    // -------------------------------------------------------------------------------------------------------

    private static boolean xor(final boolean a, final boolean b) {

        return a && !b || !a && b;
    }

    private void assertRingOrderingInvariantsForSingleKeys(final Key[] keys) {

        for (final Key k : keys) {
            assertRingOrderingInvariants(k);
        }
    }

    private void assertRingOrderingInvariantsForTriplesOfDifferentKeys(final Key[] keys) {

        for (final Key k1 : keys) {
            for (final Key k2 : keys) {
                for (final Key k3 : keys) {
                    if (allDifferent(k1, k2, k3)) {
                        assertRingOrderingInvariantsForArbitraryKeys(k1, k2, k3);
                    }
                }
            }
        }
    }

    private void assertRingSegmentInvariantsForSingleKeys(final Key[] keys) {

        for (final Key k : keys) {
            assertRingSegmentInvariants(k);
        }
    }

    private void assertRingSegmentInvariantsForTriplesOfDifferentKeys(final Key[] keys) {

        for (final Key k1 : keys) {
            for (final Key k2 : keys) {
                for (final Key k3 : keys) {
                    if (allDifferent(k1, k2, k3)) {
                        assertRingSegmentInvariantsForArbitraryKeys(k1, k2, k3);
                    }
                }
            }
        }
    }

    private static boolean allDifferent(final Key k1, final Key k2, final Key k3) {

        return !(k1.equals(k2) || k1.equals(k3) || k2.equals(k3));
    }

    static Key offsetKey(final Key k, final BigInteger i) {

        return new Key(k.getValue()
                .add(i));
    }
}
