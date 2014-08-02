package uk.ac.standrews.cs.trombone.core.key;

import java.util.NavigableMap;
import java.util.Random;
import java.util.concurrent.ConcurrentSkipListMap;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomAdaptor;
import org.mashti.sina.distribution.ZipfDistribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class ZipfKeySupplier extends KeySupplier {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZipfKeySupplier.class);
    private final NavigableMap<Double, Key> key_ranking;
    private final int elements_count;
    private final double exponent;

    public ZipfKeySupplier(final int elements_count, double exponent, long seed) {

        this(elements_count, exponent, seed, generateZipfKeyRanking(elements_count, exponent, seed));
    }

    public ZipfKeySupplier(ZipfKeySupplier factory) {

        this(factory.elements_count, factory.exponent, factory.seed, factory.key_ranking);
    }

    private ZipfKeySupplier(final int elements_count, double exponent, long seed, NavigableMap<Double, Key> key_ranking) {

        super(seed);

        this.elements_count = elements_count;
        this.exponent = exponent;
        this.key_ranking = key_ranking;
    }

    @Override
    public Key get() {

        return key_ranking.ceilingEntry(random.nextDouble()).getValue();
    }

    public double getExponent() {

        return exponent;
    }

    public int getElementsCount() {

        return elements_count;
    }

    @Override
    public String toString() {

        return "ZipfKeySupplier{elements_count=" + elements_count + ", exponent=" + exponent + ", seed=" + seed + '}';
    }

    private static NavigableMap<Double, Key> generateZipfKeyRanking(final int elements_count, final double exponent, final long seed) {

        final Random random = new RandomAdaptor(new MersenneTwister(seed));
        LOGGER.info("ranking {} keys based on zipf distribution with the exponent of {}", elements_count, exponent);
        final ConcurrentSkipListMap<Double, Key> key_rank = new ConcurrentSkipListMap<>();
        final ZipfDistribution distribution = new ZipfDistribution(elements_count, exponent);
        for (int i = 0; i < elements_count; i++) {

            final double rank = distribution.cumulative(i + 1).doubleValue();
            final Key key = generate(random);
            key_rank.put(rank, key);
            if (i % 1000 == 0) {
                LOGGER.info("ranked {}%", 100 * i / elements_count);
            }
        }
        LOGGER.info("finished ranking keys");
        return key_rank;
    }
}
