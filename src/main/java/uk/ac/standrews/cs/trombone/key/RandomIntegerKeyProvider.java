package uk.ac.standrews.cs.trombone.key;

import java.util.Random;
import uk.ac.standrews.cs.trombone.evaluation.provider.SerializableProvider;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class RandomIntegerKeyProvider implements SerializableProvider<Key>, KeyDistribution {

    private static final long serialVersionUID = 7941134745699805272L;
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
