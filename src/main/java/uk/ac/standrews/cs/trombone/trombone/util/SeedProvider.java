package uk.ac.standrews.cs.trombone.trombone.util;

import java.util.Random;
import javax.inject.Provider;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class SeedProvider implements Provider<Long> {

    private final Random random;

    public SeedProvider(Long initial_seed) {

        random = new Random(initial_seed);
    }

    @Override
    public synchronized Long get() {

        return random.nextLong();
    }
}
