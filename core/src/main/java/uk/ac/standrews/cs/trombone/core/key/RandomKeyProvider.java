package uk.ac.standrews.cs.trombone.core.key;

import java.util.Random;
import javax.inject.Provider;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class RandomKeyProvider implements Provider<Key>, KeyDistribution {

    private static final int DEFAULT_KEY_LENGTH_IN_BITS = 32;
    private final Random random;

    public RandomKeyProvider() {

        random = new Random();
    }

    public RandomKeyProvider(final long seed) {

        random = new Random(seed);
    }

    @Override
    public synchronized Key get() {

        final byte[] key_value = new byte[DEFAULT_KEY_LENGTH_IN_BITS];
        random.nextBytes(key_value);
        return new Key(key_value);
    }

    @Override
    public Key[] generate(final int count) {

        final Key[] keys = new Key[count];
        for (int i = 0; i < count; i++) {
            keys[i] = get();
        }
        return keys;
    }
}
