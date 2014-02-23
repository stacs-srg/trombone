package uk.ac.standrews.cs.trombone.event.churn;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import org.uncommons.maths.random.MersenneTwisterRNG;
import uk.ac.standrews.cs.shabdiz.util.Duration;
import uk.ac.standrews.cs.trombone.core.util.Named;
import uk.ac.standrews.cs.trombone.core.util.NamingUtils;

import static java.lang.Math.PI;
import static uk.ac.standrews.cs.trombone.event.churn.FixedExponentialInterval.nextExponential;

/**
 * Generates exponentially distributed intervals with oscillating mean interval.
 *
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class OscillatingExponentialInterval implements IntervalGenerator, Named {

    private final long max_mean_nanos;
    private final long min_mean_nanos;
    private final long cycle_length_nanos;
    private final MersenneTwisterRNG random;
    private final double curve;
    private final double offset;
    private final double half_cycle;
    private final Duration max_mean;
    private final Duration min_mean;
    private final Duration cycle_length;
    private final byte[] seed;

    /**
     * Instantiates a new exponentially distributed interval generator with oscillating mean interval.
     *
     * @param max_mean the maximum mean interval
     * @param min_mean the minimum mean interval
     * @param cycle_length the oscillating cycle length
     * @param seed the random seed
     */
    public OscillatingExponentialInterval(Duration max_mean, Duration min_mean, Duration cycle_length, byte[] seed) {

        this.max_mean = max_mean;
        this.min_mean = min_mean;
        this.cycle_length = cycle_length;
        this.seed = seed;

        max_mean_nanos = max_mean.getLength(TimeUnit.NANOSECONDS);
        min_mean_nanos = min_mean.getLength(TimeUnit.NANOSECONDS);
        cycle_length_nanos = cycle_length.getLength(TimeUnit.NANOSECONDS);

        half_cycle = cycle_length_nanos / 2d;
        curve = (max_mean_nanos - min_mean_nanos) / 2d;
        offset = max_mean_nanos - curve;

        random = new MersenneTwisterRNG(seed);
    }

    @Override
    public long get(final long time_nanos) {

        final long mean = getMeanAt(time_nanos);
        assert mean >= 0;
        return (long) nextExponential(mean, random);
    }

    @Override
    public long getMeanAt(final long time_nanos) {

        return (long) (curve * Math.cos(time_nanos * PI / half_cycle) + offset);
    }

    /**
     * Gets the oscillating cycle length.
     *
     * @return the oscillating cycle length
     */
    public Duration getCycleLength() {

        return cycle_length;
    }

    /**
     * Gets maximum mean interval.
     *
     * @return the maximum mean interval
     */
    public Duration getMaxMean() {

        return max_mean;
    }

    /**
     * Gets minimum mean interval.
     *
     * @return the minimum mean interval
     */
    public Duration getMinMean() {

        return min_mean;
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
    public OscillatingExponentialInterval copy() {

        return new OscillatingExponentialInterval(max_mean, min_mean, cycle_length, seed);
    }

    @Override
    public String toString() {

        final StringBuilder sb = new StringBuilder("OscillatingExponentialInterval{");
        sb.append("max_mean=").append(max_mean);
        sb.append(", min_mean=").append(min_mean);
        sb.append(", cycle_length=").append(cycle_length);
        sb.append(", seed=").append(Arrays.toString(random.getSeed()));
        sb.append('}');
        return sb.toString();
    }

    @Override
    public String getName() {

        return NamingUtils.name(this);
    }
}
