package uk.ac.standrews.cs.trombone.core.key;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;
import org.junit.Assert;
import org.junit.Test;
import uk.ac.standrews.cs.trombone.core.PeerConfiguration;
import uk.ac.standrews.cs.trombone.core.util.RelativeRingDistanceComparator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static uk.ac.standrews.cs.trombone.core.key.RingArithmeticTest.TEST_KEYS;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class KeyTest {

    static {
        System.setProperty(PeerConfiguration.PEER_KEY_LENGTH_SYSTEM_PROPERTY, "64");
    }

    private static final Key ZERO_KEY = RingArithmeticTest.ZERO_KEY;
    private static final Key ONE_KEY = RingArithmeticTest.ONE_KEY;
    private static final Key ARBITRARY_KEY = RingArithmeticTest.ARBITRARY_KEY;
    private static final Key MAX_KEY = RingArithmeticTest.MAX_KEY;

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
        assertEquals(Key.valueOf(7), five.next().next());
        assertEquals(Key.valueOf(8), five.next().next().next());
        assertEquals(Key.valueOf(7), five.next().next().next().previous());
        assertEquals(Key.MIN_VALUE, Key.MAX_VALUE.next());
        assertEquals(Key.MAX_VALUE, Key.MIN_VALUE.previous());
        assertEquals(Key.MAX_VALUE.previous(), Key.MIN_VALUE.previous().previous());
    }

    /**
     * Tests whether ring distance calculation works as expected.
     */
    @Test
    public void ringDistanceSpecific() {

        // Distances from keyspace_size - 1.
        assertEquals(MAX_KEY.ringDistance(ZERO_KEY), BigInteger.ONE);
        assertEquals(MAX_KEY.ringDistance(ONE_KEY), Key.TWO);
        assertEquals(MAX_KEY.ringDistance(ARBITRARY_KEY), RingArithmeticTest.ARBITRARY_VALUE.add(RingArithmeticTest.ONE));

        // Distances from 0.
        assertEquals(ZERO_KEY.ringDistance(ONE_KEY), BigInteger.ONE);
        assertEquals(ZERO_KEY.ringDistance(ARBITRARY_KEY), RingArithmeticTest.ARBITRARY_VALUE);
        assertEquals(ZERO_KEY.ringDistance(MAX_KEY), RingArithmeticTest.MAX_VALUE);

        // Miscellaneous pairs.
        assertEquals(RingArithmeticTest.QUARTER_KEY.ringDistance(RingArithmeticTest.HALF_KEY), RingArithmeticTest.QUARTER_VALUE);
        assertEquals(RingArithmeticTest.HALF_KEY.ringDistance(RingArithmeticTest.THREE_QUARTER_KEY), RingArithmeticTest.QUARTER_VALUE);
        assertEquals(RingArithmeticTest.THREE_QUARTER_KEY.ringDistance(RingArithmeticTest.ZERO_KEY), RingArithmeticTest.QUARTER_VALUE);
        assertEquals(RingArithmeticTest.QUARTER_KEY.ringDistance(RingArithmeticTest.THREE_QUARTER_KEY), RingArithmeticTest.HALF_VALUE);
        assertEquals(RingArithmeticTest.THREE_QUARTER_KEY.ringDistance(RingArithmeticTest.QUARTER_KEY), RingArithmeticTest.HALF_VALUE);
        assertEquals(RingArithmeticTest.HALF_KEY.ringDistance(RingArithmeticTest.QUARTER_KEY), RingArithmeticTest.THREE_QUARTER_VALUE);
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

        assertEquals(RingArithmeticTest.ZERO, k.ringDistance(k));
        assertEquals(RingArithmeticTest.ONE, k.ringDistance(RingArithmeticTest.offsetKey(k, RingArithmeticTest.ONE)));
        assertEquals(RingArithmeticTest.MAX_VALUE, k.ringDistance(RingArithmeticTest.offsetKey(k, RingArithmeticTest.MINUS_ONE)));
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

        assertTrue(value.compareTo(RingArithmeticTest.ZERO) >= 0);
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
        assertEquals(five.hashCode(), Key.valueOf(5).hashCode());
        assertNotEquals(five.hashCode(), Key.valueOf(555).hashCode());
    }

    @Test
    public void testGetValue() throws Exception {

        assertEquals(BigInteger.valueOf(5), five.getValue());
        assertEquals(BigInteger.ZERO, zero.getValue());

    }

    @Test
    public void testIntValue() throws Exception {

        for (int i = 0; i < 1000; i++) {
            final Integer value = random.nextInt();
            assertEquals(value.intValue(), Key.valueOf(value).intValue());
        }
    }

    @Test
    public void testLongValue() throws Exception {

        for (int i = 0; i < 1000; i++) {
            final Long value = random.nextLong();
            assertEquals(value.longValue(), Key.valueOf(value).longValue());
        }
    }

    @Test
    public void testShortValue() throws Exception {

        for (int i = 0; i < 1000; i++) {
            final Short value = (short) (random.nextInt() - Short.MIN_VALUE);
            assertEquals(value.shortValue(), Key.valueOf(value).shortValue());
        }
    }

    @Test
    public void testByteValue() throws Exception {

        for (int i = 0; i < 1000; i++) {
            final BigInteger big_integer = BigInteger.valueOf(Math.abs(random.nextLong()));
            final Key key = new Key(big_integer);

            assertEquals(big_integer.byteValue(), key.byteValue());
            assertEquals(big_integer.byteValue(), key.byteValue());
            assertEquals(big_integer.shortValue(), key.shortValue());
            assertEquals(big_integer.intValue(), key.intValue());
            assertEquals(big_integer.longValue(), key.longValue());
            assertEquals(big_integer.floatValue(), key.floatValue(), DELTA);
            assertEquals(big_integer.doubleValue(), key.doubleValue(), DELTA);
        }
    }

    @Test
    public void testToString() throws Exception {

        assertEquals(BigInteger.valueOf(5).toString(), five.toString());

    }
}
