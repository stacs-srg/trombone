package uk.ac.standrews.cs.trombone.core.key;

import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;
import org.mashti.sina.distribution.ZipfDistribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class ZipfKeyProvider implements KeyProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZipfKeyProvider.class);
    private final Random random;
    private final NavigableMap<Double, Key> key_rank;
    private final int elements_count;
    private final double exponent;
    private final int key_length_in_bits;
    private volatile long seed;

    public ZipfKeyProvider(final int elements_count, double exponent, int key_length_in_bits, long seed) {

        this(elements_count, exponent, key_length_in_bits, seed, generateZipfKeyRank(elements_count, exponent, seed, key_length_in_bits));
    }

    private ZipfKeyProvider(final int elements_count, double exponent, int key_length_in_bits, long seed, NavigableMap<Double, Key> key_rank) {

        this.elements_count = elements_count;
        this.exponent = exponent;
        this.key_length_in_bits = key_length_in_bits;
        this.seed = seed;
        this.key_rank = key_rank;

        random = new Random(seed);
    }

    @Override
    public Key get() {

        final double dice;

        synchronized (random) {
            dice = random.nextDouble();
        }

        return key_rank.ceilingEntry(dice).getValue();
    }

    @Override
    public synchronized void setSeed(long seed) {

        random.setSeed(seed);
        this.seed = seed;
    }

    @Override
    public synchronized long getSeed() {

        return seed;
    }

    @Override
    public String toString() {

        final StringBuilder sb = new StringBuilder("ZipfKeyProvider{");
        sb.append("key_length_in_bits=").append(key_length_in_bits);
        sb.append(", elements_count=").append(elements_count);
        sb.append(", exponent=").append(exponent);
        sb.append(", seed=").append(seed);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public ZipfKeyProvider clone() {

        return new ZipfKeyProvider(elements_count, exponent, key_length_in_bits, seed, key_rank);
    }

    private static NavigableMap<Double, Key> generateZipfKeyRank(final int elements_count, final double exponent, final long seed, int key_length_in_bits) {

        LOGGER.info("ranking {} keys based on zipf distribution with the exponent of {}", elements_count, exponent);
        final RandomKeyProvider key_provider = new RandomKeyProvider(seed, key_length_in_bits);
        final TreeMap<Double, Key> key_rank = new TreeMap<>();

        final ZipfDistribution distribution = new ZipfDistribution(elements_count, exponent);
        for (int i = 0; i < elements_count; i++) {
            final double rank = distribution.cumulative(i + 1).doubleValue();
            final Key key = key_provider.get();
            key_rank.put(rank, key);
        }
        LOGGER.info("finished ranking keys");
        return key_rank;
    }
}
