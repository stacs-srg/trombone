package uk.ac.standrews.cs.trombone.core.key;

import java.math.BigInteger;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests ring arithmetic.
 *
 * @author Graham Kirby (graham.kirby@st-andrews.ac.uk)
 */
public class RingArithmeticTest {

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
        assertEquals(new Key(Key.KEYSPACE_SIZE.multiply(TWO).add(ONE)).getValue(), ONE);

        // -2 * keyspace_size + 1 should wrap to 1.
        assertEquals(new Key(Key.KEYSPACE_SIZE.multiply(MINUS_ONE).multiply(TWO).add(ONE)).getValue(), ONE);
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

        assertTrue(RingArithmetic.inRingOrder(k, k, k));
        assertTrue(RingArithmetic.inRingOrder(k, k, k_plus_one));
        assertTrue(RingArithmetic.inRingOrder(k, k_plus_one, k));
        assertTrue(RingArithmetic.inRingOrder(k, k_plus_one, k_plus_one));
        assertTrue(RingArithmetic.inRingOrder(k_plus_one, k, k));
        assertTrue(RingArithmetic.inRingOrder(k_plus_one, k, k_plus_one));
        assertTrue(RingArithmetic.inRingOrder(k_plus_one, k_plus_one, k));
        assertTrue(RingArithmetic.inRingOrder(k_plus_one, k_plus_one, k_plus_one));

        assertTrue(RingArithmetic.inRingOrder(k, k_plus_one, k_plus_two));
        assertFalse(RingArithmetic.inRingOrder(k, k_plus_two, k_plus_one));
    }

    /**
     * Tests conditions that should hold for all triples of different key values.
     *
     * @param k1 the first key
     * @param k2 the second key
     * @param k3 the third key
     */
    private static void assertRingOrderingInvariantsForArbitraryKeys(final Key k1, final Key k2, final Key k3) {

        assertTrue(xor(RingArithmetic.inRingOrder(k1, k2, k3), RingArithmetic.inRingOrder(k1, k3, k2)));

        if (RingArithmetic.inRingOrder(k1, k2, k3)) {
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

        assertTrue(RingArithmetic.inRingOrder(k1, k2, k3));
        assertTrue(RingArithmetic.inRingOrder(k2, k3, k1));
        assertTrue(RingArithmetic.inRingOrder(k3, k1, k2));

        assertTrue(RingArithmetic.ringDistanceFurther(k1, k3, k2));
        assertTrue(RingArithmetic.ringDistanceFurther(k3, k2, k1));
        assertTrue(RingArithmetic.ringDistanceFurther(k2, k1, k3));

        assertFalse(RingArithmetic.ringDistanceFurther(k1, k2, k3));
        assertFalse(RingArithmetic.ringDistanceFurther(k2, k3, k1));
        assertFalse(RingArithmetic.ringDistanceFurther(k3, k1, k2));
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

        assertTrue(RingArithmetic.inSegment(k, k, k));
        assertFalse(RingArithmetic.inSegment(k, k, k_plus_one));
        assertTrue(RingArithmetic.inSegment(k, k_plus_one, k));
        assertTrue(RingArithmetic.inSegment(k, k_plus_one, k_plus_one));
        assertTrue(RingArithmetic.inSegment(k_plus_one, k, k));
        assertTrue(RingArithmetic.inSegment(k_plus_one, k, k_plus_one));
        assertFalse(RingArithmetic.inSegment(k_plus_one, k_plus_one, k));
        assertTrue(RingArithmetic.inSegment(k_plus_one, k_plus_one, k_plus_one));

        assertTrue(RingArithmetic.inSegment(k, k_plus_one, k_plus_two));
        assertFalse(RingArithmetic.inSegment(k, k_plus_two, k_plus_one));
    }

    /**
     * Tests conditions that should hold for all triples of different key values.
     *
     * @param k1 the first key
     * @param k2 the second key
     * @param k3 the third key
     */
    private static void assertRingSegmentInvariantsForArbitraryKeys(final Key k1, final Key k2, final Key k3) {

        assertTrue(xor(RingArithmetic.inSegment(k1, k2, k3), RingArithmetic.inSegment(k1, k3, k2)));

        if (RingArithmetic.inRingOrder(k1, k2, k3)) {
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

        assertTrue(RingArithmetic.inSegment(k1, k2, k3));
        assertTrue(RingArithmetic.inSegment(k2, k3, k1));
        assertTrue(RingArithmetic.inSegment(k3, k1, k2));
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

        return new Key(k.getValue().add(i));
    }
}