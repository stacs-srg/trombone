package uk.ac.standrews.cs.trombone.event.churn;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.uncommons.maths.random.MersenneTwisterRNG;
import uk.ac.standrews.cs.shabdiz.util.Duration;
import uk.ac.standrews.cs.trombone.core.util.Named;
import uk.ac.standrews.cs.trombone.core.util.NamingUtils;

import static java.lang.Math.log;

/**
 * Generates exponentially distributed intervals with a fixed mean.
 *
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class FixedExponentialInterval implements IntervalGenerator, Named {

    private final long mean_nanos;
    private final MersenneTwisterRNG random;
    private final Duration mean;
    private final byte[] seed;

    /**
     * Instantiates a new exponentially distributed interval generator with a fixed mean.
     *
     * @param mean the mean interval
     * @param seed the random seed
     */
    public FixedExponentialInterval(Duration mean, byte[] seed) {

        this.mean = mean;
        this.seed = seed;
        mean_nanos = mean.getLength(TimeUnit.NANOSECONDS);
        random = new MersenneTwisterRNG(seed);
    }

    @Override
    public long get(final long time_nanos) {

        return (long) (-mean_nanos * log(1 - random.nextDouble()));
    }

    @Override
    public long getMeanAt(final long time_nanos) {

        return mean_nanos;
    }

    /**
     * Gets the mean interval.
     *
     * @return the mean interval
     */
    public Duration getMean() {

        return mean;
    }

    /**
     * Get the random seed.
     *
     * @return the random seed
     */
    public byte[] getSeed() {

        return seed;
    }

    @Override
    public String getName() {

        return NamingUtils.name(this);
    }

    static double nextExponential(double mean, Random random) {

        return -mean * log(1 - random.nextDouble());
    }

    @Override
    public FixedExponentialInterval copy() {

        return new FixedExponentialInterval(mean, seed);
    }

    @Override
    public String toString() {

        final StringBuilder sb = new StringBuilder("FixedExponentialInterval{");
        sb.append("mean=").append(mean);
        sb.append(", seed=").append(Arrays.toString(random.getSeed()));
        sb.append('}');
        return sb.toString();
    }
}
