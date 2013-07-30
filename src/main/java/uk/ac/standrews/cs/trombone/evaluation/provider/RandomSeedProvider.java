package uk.ac.standrews.cs.trombone.evaluation.provider;

import java.util.Random;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class RandomSeedProvider implements SerializableProvider<Long> {

    private static final long serialVersionUID = -289135604316719562L;
    private final Random random;

    public RandomSeedProvider(final long initial_seed) {

        random = new Random(initial_seed);
    }

    @Override
    public synchronized Long get() {

        return random.nextLong();
    }
}
