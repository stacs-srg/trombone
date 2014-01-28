package uk.ac.standrews.cs.trombone.core.key;

import java.util.Random;
import javax.inject.Provider;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class RandomKeyProvider implements Provider<Key> {

    private final Random random;
    private final long seed;
    private final int key_length_in_bits;
    private final int key_length_in_bytes;

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
}
