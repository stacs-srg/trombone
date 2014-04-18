package uk.ac.standrews.cs.trombone.core.key;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.trombone.core.util.RelativeRingDistanceComparator;

import static org.junit.Assert.assertArrayEquals;
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
    public void testGetValue() throws Exception {

        assertEquals(Integer.SIZE / Byte.SIZE, _5.getValue().length);
        assertArrayEquals(ByteBuffer.allocate(Integer.SIZE / Byte.SIZE).putInt(5).array(), _5.getValue());
        assertArrayEquals(ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(Long.MAX_VALUE).array(), Key.valueOf(Long.MAX_VALUE).getValue());
    }

    @Test
    public void testGetLength() throws Exception {

        assertEquals(Short.SIZE, Key.valueOf((short) 56).getLength());
        assertEquals(Integer.SIZE, Key.valueOf(Integer.valueOf(56)).getLength());
        assertEquals(Long.SIZE, Key.valueOf(Long.valueOf(56)).getLength());
        assertEquals(160, newRandomKeyOfLengthInBits(160 / Byte.SIZE).getLength());
        assertEquals(8, newRandomKeyOfLengthInBits(8 / Byte.SIZE).getLength());
        assertEquals(320, newRandomKeyOfLengthInBits(320 / Byte.SIZE).getLength());
        assertEquals(1024, newRandomKeyOfLengthInBits(1024 / Byte.SIZE).getLength());
        assertEquals(65536, newRandomKeyOfLengthInBits(65536 / Byte.SIZE).getLength());
    }

    @Test
    public void testGetKeySpaceSize() throws Exception {

        assertEquals((long) Math.pow(2, Short.SIZE), Key.getKeySpaceSize(Key.valueOf((short) 56)).longValue());
        assertEquals((long) Math.pow(2, Integer.SIZE), Key.getKeySpaceSize(Key.valueOf(Integer.valueOf(56))).longValue());
    }

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
    public void testDoubleValue() throws Exception {

        for (int i = 0; i < 1000; i++) {
            final Double value = random.nextDouble();
            assertEquals(value, Key.valueOf(value).doubleValue(), DELTA);
        }
    }

    @Test
    public void testFloatValue() throws Exception {

        for (int i = 0; i < 1000; i++) {
            final float value = random.nextFloat();
            assertEquals(value, Key.valueOf(value).floatValue(), DELTA);
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

        System.out.println(Key.valueOf(1.0d).floatValue());
        System.out.println(Key.valueOf(1.0f));
        System.out.println(Key.valueOf(1));
        System.out.println(Key.valueOf(Long.valueOf(1)));
        
        for (int i = 0; i < 1000; i++) {
            final int length = random.nextInt(99) + 1;
            final byte[] value = new byte[length];
            random.nextBytes(value);

            final Key key = Key.valueOf(value);
            final BigInteger big_integer = new BigInteger(value);

            LOGGER.info("key: {}, key_length: {}, big_int: {}", key, length, big_integer);

            assertEquals(value[length - 1], key.byteValue());
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
