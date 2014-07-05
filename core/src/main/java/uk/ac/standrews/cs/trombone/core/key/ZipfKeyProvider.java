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
public class ZipfKeyProvider extends KeyProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZipfKeyProvider.class);
    private final NavigableMap<Double, Key> key_rank;
    private final int elements_count;
    private final double exponent;

    public ZipfKeyProvider(final int elements_count, double exponent, int key_length_in_bits, long seed) {

        this(elements_count, exponent, key_length_in_bits, seed, generateZipfKeyRank(elements_count, exponent, seed, key_length_in_bits));
    }

    private ZipfKeyProvider(final int elements_count, double exponent, int key_length_in_bits, long seed, NavigableMap<Double, Key> key_rank) {

        super(key_length_in_bits, seed);

        this.elements_count = elements_count;
        this.exponent = exponent;
        this.key_rank = key_rank;
    }

    @Override
    public Key get() {

        return key_rank.ceilingEntry(random.nextDouble()).getValue();
    }

    public double getExponent() {

        return exponent;
    }

    public int getElementsCount() {

        return elements_count;
    }

    @Override
    public String toString() {

        final StringBuilder sb = new StringBuilder("ZipfKeyProvider{");
        sb.append("key_length_in_bits=").append(getKeyLengthInBits());
        sb.append(", elements_count=").append(elements_count);
        sb.append(", exponent=").append(exponent);
        sb.append(", seed=").append(getSeed());
        sb.append('}');
        return sb.toString();
    }

    @Override
    public ZipfKeyProvider copy() {

        return new ZipfKeyProvider(elements_count, exponent, getKeyLengthInBits(), getSeed(), key_rank);
    }

    private static NavigableMap<Double, Key> generateZipfKeyRank(final int elements_count, final double exponent, final long seed, int key_length_in_bits) {

        final Random random = new RandomAdaptor(new MersenneTwister(seed));
        LOGGER.info("ranking {} keys based on zipf distribution with the exponent of {}", elements_count, exponent);
        final ConcurrentSkipListMap<Double, Key> key_rank = new ConcurrentSkipListMap<>();
        final ZipfDistribution distribution = new ZipfDistribution(elements_count, exponent);
        for (int i = 0; i < elements_count; i++) {

            final double rank = distribution.cumulative(i + 1).doubleValue();
            final Key key = generate(key_length_in_bits, random);
            key_rank.put(rank, key);
            if (i % 1000 == 0) {
                LOGGER.info("ranked {}%", 100 * i / elements_count);
            }
        }
        LOGGER.info("finished ranking keys");
        return key_rank;
    }

}
