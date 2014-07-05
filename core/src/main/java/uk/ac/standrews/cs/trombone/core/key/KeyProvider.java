package uk.ac.standrews.cs.trombone.core.key;

import java.util.Random;
import java.util.function.Supplier;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomAdaptor;
import uk.ac.standrews.cs.trombone.core.util.Copyable;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class KeyProvider implements Supplier<Key>, Copyable {

    private final int key_length_in_bits;
    protected final long seed;
    protected final Random random;

    public KeyProvider(int key_length_in_bits, long seed) {

        this.key_length_in_bits = key_length_in_bits;
        this.seed = seed;

        random = new RandomAdaptor(new MersenneTwister(seed));
    }

    static Key generate(int key_length_in_bits, Random random) {

        final byte[] key_value = new byte[key_length_in_bits / Byte.SIZE];
        random.nextBytes(key_value);
        return Key.valueOf(key_value);
    }

    static Key[] generate(final int count, int key_length_in_bits, Random random) {

        final Key[] keys = new Key[count];
        for (int i = 0; i < count; i++) {
            keys[i] = generate(key_length_in_bits, random);
        }
        return keys;
    }

    @Override
    public Key get() {

        return generate(key_length_in_bits, random);
    }

    public long getSeed() {

        return seed;
    }

    public int getKeyLengthInBits() {

        return key_length_in_bits;
    }

    @Override
    public KeyProvider copy() {

        return new KeyProvider(key_length_in_bits, seed);
    }
}
