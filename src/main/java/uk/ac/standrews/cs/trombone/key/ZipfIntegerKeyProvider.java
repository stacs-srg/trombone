package uk.ac.standrews.cs.trombone.key;

import java.util.Random;
import javax.inject.Provider;
import org.mashti.sina.distribution.ZipfDistribution;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class ZipfIntegerKeyProvider implements Provider<Key> {

    private final ZipfDistribution distribution;
    private final IntegerKey[] keys;
    private final Random random;
    private final int elements_count;

    public ZipfIntegerKeyProvider(int elements_count, double exponent, long seed) {

        this.elements_count = elements_count;
        distribution = new ZipfDistribution(elements_count, exponent);
        keys = new RandomIntegerKeyProvider(seed).generate(elements_count);
        random = new Random(seed); //TODO use Well19937c

    }

    @Override
    public synchronized IntegerKey get() {

        return keys[nextIndex()];
    }

    private int nextIndex() {

        int rank;
        double friquency;
        double dice;
        do {
            rank = random.nextInt(elements_count) + 1;
            friquency = distribution.density(rank).doubleValue();
            dice = random.nextDouble();
        }
        while (!(dice < friquency));

        return rank - 1;
    }
}
