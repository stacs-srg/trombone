package uk.ac.standrews.cs.trombone.core.key;

import java.util.Random;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class RandomKeyProvider implements KeyProvider {

    private final Random random;
    private final int key_length_in_bits;
    private final int key_length_in_bytes;
    private volatile long seed;

    public RandomKeyProvider(final long seed, int key_length_in_bits) {

        this.seed = seed;
        this.key_length_in_bits = key_length_in_bits;
        random = new Random(seed);
        key_length_in_bytes = key_length_in_bits / Byte.SIZE;
    }

    @Override
    public Key get() {

        final byte[] key_value = new byte[key_length_in_bytes];
        synchronized (random) {
            random.nextBytes(key_value);
        }
        return Key.valueOf(key_value);
    }

    public int getKeyLengthInBits() {

        return key_length_in_bits;
    }

    public Key[] generate(final int count) {

        final Key[] keys = new Key[count];
        for (int i = 0; i < count; i++) {
            keys[i] = get();
        }
        return keys;
    }

    @Override
    public String toString() {

        final StringBuilder sb = new StringBuilder("RandomKeyProvider{");
        sb.append("seed=").append(seed);
        sb.append(", key_length_in_bits=").append(key_length_in_bits);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public synchronized void setSeed(final long seed) {

        this.seed = seed;
        random.setSeed(seed);
    }

    @Override
    public synchronized long getSeed() {

        return seed;
    }

    @Override
    public RandomKeyProvider clone() {

        return new RandomKeyProvider(seed, key_length_in_bits);
    }
}
