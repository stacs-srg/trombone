package uk.ac.standrews.cs.trombone.event.provider;

import java.util.Random;
import javax.inject.Provider;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class RandomSeedProvider implements Provider<Long> {

    private final Random random;

    public RandomSeedProvider(Long seed) {

        random = new Random(seed);

    }

    @Override
    public synchronized Long get() {

        return random.nextLong();
    }
}
