package uk.ac.standrews.cs.trombone.core.key;

import java.util.Random;
import javax.inject.Provider;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class RandomIntegerKeyProvider implements Provider<Key>, KeyDistribution {

    private final Random random;

    public RandomIntegerKeyProvider() {

        random = new Random();
    }

    public RandomIntegerKeyProvider(final long seed) {

        random = new Random(seed);
    }

    @Override
    public synchronized IntegerKey get() {

        return new IntegerKey(random.nextInt());
    }

    @Override
    public IntegerKey[] generate(final int count) {

        final IntegerKey[] keys = new IntegerKey[count];
        for (int i = 0; i < count; i++) {
            keys[i] = get();
        }
        return keys;
    }
}
