package uk.ac.standrews.cs.trombone.trombone.key;

import java.util.Random;
import javax.inject.Provider;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class RandomIntegerKeyProvider implements Provider<Key> {

    private static final int DEFAULT_SEED = 5456564;
    private static final RandomIntegerKeyProvider DEFAULT_KEY_FACTORY = new RandomIntegerKeyProvider(DEFAULT_SEED);
    private final Random random;

    public RandomIntegerKeyProvider(final long seed) {

        random = new Random(seed);
    }

    public static RandomIntegerKeyProvider getDefault() {

        return DEFAULT_KEY_FACTORY;
    }

    @Override
    public synchronized IntegerKey get() {

        return new IntegerKey(random.nextInt());
    }
}
