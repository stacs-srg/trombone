package uk.ac.standrews.cs.trombone.key;

import javax.inject.Provider;
import org.apache.commons.math3.distribution.ZipfDistribution;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class ZipfIntegerKeyProvider implements Provider<Key> {

    private final ZipfDistribution distribution;
    private final IntegerKey[] keys;

    public ZipfIntegerKeyProvider(int elements_count, double exponent, long seed) {

        distribution = new ZipfDistribution(elements_count, exponent);
        distribution.reseedRandomGenerator(seed);
        keys = new RandomIntegerKeyProvider(seed).generate(elements_count);
    }

    @Override
    public synchronized IntegerKey get() {

        return keys[distribution.sample()];
    }
}
