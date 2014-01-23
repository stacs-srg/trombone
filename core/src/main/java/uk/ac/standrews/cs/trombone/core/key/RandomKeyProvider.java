package uk.ac.standrews.cs.trombone.core.key;

import java.util.Random;
import javax.inject.Provider;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class RandomKeyProvider implements Provider<Key> {

    private static final int DEFAULT_KEY_LENGTH_IN_BITS = 4 * Byte.SIZE;
    private final Random random;
    private final int key_length_in_bytes;

    public RandomKeyProvider() {

        this(new Random(), DEFAULT_KEY_LENGTH_IN_BITS);
    }

    public RandomKeyProvider(final long seed) {

        this(new Random(seed), DEFAULT_KEY_LENGTH_IN_BITS);
    }

    public RandomKeyProvider(final Random random, int key_length_in_bits) {

        this.random = random;
        key_length_in_bytes = key_length_in_bits / Byte.SIZE;
    }

    @Override
    public synchronized Key get() {

        final byte[] key_value = new byte[key_length_in_bytes];
        random.nextBytes(key_value);
        return Key.valueOf(key_value);
    }
    
    public Key[] generate(final int count) {

        final Key[] keys = new Key[count];
        for (int i = 0; i < count; i++) {
            keys[i] = get();
        }
        return keys;
    }
}
