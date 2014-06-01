package uk.ac.standrews.cs.trombone.core.key;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.trombone.core.util.RelativeRingDistanceComparator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class KeyTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeyTest.class);
    private static final Key _5 = Key.valueOf(5);
    private static final Key _minus5 = Key.valueOf(-5);
    private static final Key _7 = Key.valueOf(7);
    private static final Key _0 = Key.valueOf(0);
    private static final double DELTA = 1e-15;
    private static final Random random = new Random(894156);


    @Test
    public void testCompareRingDistance() throws Exception {

        assertTrue(_5.compareRingDistance(_minus5, Key.valueOf(-4)) < 0);
        assertTrue(Key.valueOf(1).compareRingDistance(Key.valueOf(99), Key.valueOf(2)) > 0);
        assertTrue(Key.valueOf(1).compareRingDistance(Key.valueOf(2), Key.valueOf(99)) < 0);
        assertEquals(0, Key.valueOf(1).compareRingDistance(Key.valueOf(1), Key.valueOf(1)));
        assertEquals(0, Key.valueOf(1).compareRingDistance(Key.valueOf(-1), Key.valueOf(-1)));
        assertEquals(0, Key.valueOf(1).compareRingDistance(Key.valueOf(100), Key.valueOf(100)));

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

        assertTrue(_5.compareTo(_minus5) > 0);
        assertTrue(_minus5.compareTo(_5) < 0);
        assertTrue(_minus5.compareTo(_0) < 0);
        assertEquals(_minus5.compareTo(_5), -_5.compareTo(_minus5));
        assertTrue(_5.compareTo(_7) < 0);
        assertEquals(0, _5.compareTo(_5));
        assertEquals(0, _5.compareTo(Key.valueOf(5)));
    }

    @Test
    public void testEquals() throws Exception {

        assertEquals(_5, _5);
        assertEquals(_5, Key.valueOf(5));
        assertNotEquals(_5, Key.valueOf(555));
        assertEquals(_5.hashCode(), Key.valueOf(5).hashCode());
        assertNotEquals(_5.hashCode(), Key.valueOf(555).hashCode());
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
            final int length = random.nextInt(3) + 1;
            final byte[] value = new byte[length];
            random.nextBytes(value);
            
            final Key key = Key.valueOf(value);
            final BigInteger big_integer = new BigInteger(value);
            final int length2 = random.nextInt(3) + 1;
            final byte[] value2 = new byte[length2];
            random.nextBytes(value2);

            final Key key2 = Key.valueOf(value2);
            final BigInteger big_integer2 = new BigInteger(value2);

            long now = System.nanoTime();
            final int expected = key.compareTo(key2);
            System.out.println("1took " + (System.nanoTime() - now));
            now = System.nanoTime();
            final int actual = big_integer.compareTo(big_integer2);
            System.out.println("2took " + (System.nanoTime() - now));

            LOGGER.info("key: {}, key_length: {}, big_int: {}", key, length, big_integer);
            LOGGER.info("key: {}, key_length: {}, big_int: {}", key2, length2, big_integer2);
            System.out.println();
            assertEquals(expected, actual);
            //            assertEquals(value[length - 1], key.byteValue());
            //            assertEquals(big_integer.byteValue(), key.byteValue());
            //            assertEquals(big_integer.shortValue(), key.shortValue());
            //            assertEquals(big_integer.intValue(), key.intValue());
            //            assertEquals(big_integer.longValue(), key.longValue());
            //            assertEquals(big_integer.floatValue(), key.floatValue(), DELTA);
            //            assertEquals(big_integer.doubleValue(), key.doubleValue(), DELTA);
        }
    }

    private static Key newRandomKeyOfLengthInBits(int length) {

        final byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return new Key(bytes);
    }
}
