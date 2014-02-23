package uk.ac.standrews.cs.trombone.event.provider;

import java.util.Random;
import javax.inject.Provider;
import uk.ac.standrews.cs.trombone.core.util.Copyable;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class RandomSeedProvider implements Provider<Long>, Copyable {

    private final Random random;
    private volatile Long seed;

    public RandomSeedProvider(Long seed) {

        this.seed = seed;
        random = new Random(seed);
    }

    @Override
    public synchronized Long get() {

        return random.nextLong();
    }

    @Override
    public String toString() {

        final StringBuilder sb = new StringBuilder("RandomSeedProvider{");
        sb.append("seed=").append(seed);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public RandomSeedProvider copy() {

        return new RandomSeedProvider(seed);
    }
}
