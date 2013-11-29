package uk.ac.standrews.cs.trombone.core.key;

import java.util.Random;
import javax.inject.Provider;
import uk.ac.standrews.cs.trombone.core.util.BinaryUtils;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class RandomKeyProvider implements Provider<Key>, KeyDistribution {

    private final Random random;

    public RandomKeyProvider() {

        random = new Random();
    }

    public RandomKeyProvider(final long seed) {

        random = new Random(seed);
    }

    @Override
    public synchronized Key get() {

        return new Key(BinaryUtils.toBytes(random.nextInt()));
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
