package uk.ac.standrews.cs.trombone.core.key;

import java.util.Random;
import java.util.TreeMap;
import javax.inject.Provider;
import org.mashti.sina.distribution.ZipfDistribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class ZipfKeyProvider implements Provider<Key> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZipfKeyProvider.class);
    private final Random random;
    private final TreeMap<Double, Key> key_rank;

    public ZipfKeyProvider(final int elements_count, double exponent, long seed) {

        random = new Random(seed);
        key_rank = new TreeMap<>();
        populateKeyRankMap(elements_count, exponent);
    }

    @Override
    public Key get() {

        final double dice;
        synchronized (random) {
            dice = random.nextDouble();
        }
        return key_rank.ceilingEntry(dice).getValue();
    }

    private void populateKeyRankMap(final int elements_count, final double exponent) {

        LOGGER.info("populating keys with zipf frequency distribution");
        final ZipfDistribution distribution = new ZipfDistribution(elements_count, exponent);
        final RandomKeyProvider randomKeyProvider = new RandomKeyProvider(random.nextLong());
        for (int i = 0; i < elements_count; i++) {
            final double rank = distribution.cumulative(i + 1).doubleValue();
            final Key key = randomKeyProvider.get();
            key_rank.put(rank, key);
        }
    }
}
