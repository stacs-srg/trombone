/*
 * Copyright 2013 Masih Hajiarabderkani
 *
 * This file is part of Trombone.
 *
 * Trombone is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Trombone is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Trombone.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.ac.standrews.cs.trombone.math;

import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Test;
import uk.ac.standrews.cs.shabdiz.util.Duration;

import static org.junit.Assert.fail;
import static uk.ac.standrews.cs.trombone.math.ProbabilityDistribution.HALF;
import static uk.ac.standrews.cs.trombone.math.ProbabilityDistribution.ONE;

/**
 * Tests {@link UniformDistribution}.
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class UniformDistributionTest {

    //@formatter:off
    private static final double[][] TEST_INPUT_OUTPUT = {
        {0.8842, 852456, 426228.4421, 1.5, 1.1730822907450489E-6, 7.223840746408012E-7, 2.4778507908955887E-6, 2.9964565828880207, 426228.4421, 0.8842, 0.0, 6.055664370446595E10},
        {1.5, 50, 25.75, 21.0, 0.020618556701030927, 0.4020618556701031, 0.012691650910201193, 2.115545069144758, 25.75, 1.5, 0.0, 196.02083333333334},
        {-1.2, -1.0, -1.1, 0.30000000000000004, 0.0, 1.0, 0.012691650910201193, -1.1974616698179597, -1.1, -1.2, 0.0, 0.003333333333333332},
        {-1.0, -1.0, -1.0, 11.0, 0.0, 1.0, 0.012691650910201193, -1.0, -1.0, -1.0, 0.0, 0.0}
    };
    public static final double DELTA = 0.0d;

    /**
     * Tests {@link UniformDistribution#UniformDistribution(Number, Number)}.
     */
    @Test
    public void testUniformDistributionDurationDuration() {

        final Number[][] bad_args = {{Double.NaN, Double.NaN}, {Double.NaN, Double.NEGATIVE_INFINITY}, {Double.NEGATIVE_INFINITY, Double.NaN}, {Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}, {5.7, 5.6}, {-55.1, -56.0}};
        for (final Number[] bad_arg : bad_args) {
            try {
                new UniformDistribution(bad_arg[0], bad_arg[1]);
                fail();
            }
            catch (final IllegalArgumentException e) {
                continue;
            }
        }

        final Number[][] good_args = {{Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY}, {Double.MAX_VALUE, Double.MAX_VALUE}, {ONE, ONE}, {HALF, HALF}, {-HALF, -HALF}, {-HALF, ONE}};
        for (final Number[] good_arg : good_args) {
            final Number low = good_arg[0].doubleValue();
            final Number scale = good_arg[1];
            final UniformDistribution test_dist = new UniformDistribution(low, scale);
            Assert.assertEquals(test_dist.getLow().doubleValue(), low);
            Assert.assertEquals(test_dist.getHigh().doubleValue(), scale);
        }
    }

    /**
     * Tests {@link UniformDistribution#UniformDistribution(Duration, Duration)}.
     */
    @Test
    public void testUniformDistributionNumberNumber() {

        ////@formatter:off
        final Duration[][] bad_args = {
            {Duration.MAX_DURATION, new Duration()},
            {new Duration(), new Duration(-1, TimeUnit.NANOSECONDS)},
            {new Duration(-1, TimeUnit.NANOSECONDS), new Duration(-2, TimeUnit.NANOSECONDS)}
        };
        ////@formatter:on
        for (final Duration[] bad_arg : bad_args) {
            try {
                new UniformDistribution(bad_arg[0], bad_arg[1]);
                fail();
            }
            catch (final IllegalArgumentException e) {
                continue;
            }
        }

        ////@formatter:off
        final Duration[][] good_args = {
                        {Duration.MAX_DURATION, Duration.MAX_DURATION},
                        {Duration.ZERO, Duration.MAX_DURATION},
                        {new Duration(1, TimeUnit.NANOSECONDS), new Duration(1, TimeUnit.NANOSECONDS)},
                        {new Duration(-1, TimeUnit.NANOSECONDS), new Duration(-1, TimeUnit.NANOSECONDS)},
        };
        ////@formatter:on
        for (final Duration[] good_arg : good_args) {
            final long low = good_arg[0].getLength(TimeUnit.NANOSECONDS);
            final long high = good_arg[1].getLength(TimeUnit.NANOSECONDS);
            final UniformDistribution test_dist = new UniformDistribution(good_arg[0], good_arg[1]);
            Assert.assertTrue(test_dist.getLow().longValue() - low == 0.0);
            Assert.assertTrue(test_dist.getHigh().longValue() - high == 0.0);
        }
    }

    //@formatter:on

    /**
     * Tests rate, mean probability, cumulative, quantile, median,mode, skewness and variance.
     */
    @Test
    public void testCalculations() {

        for (final double[] args : TEST_INPUT_OUTPUT) {
            int i = 0;
            final double low = args[i++];
            final double high = args[i++];
            final UniformDistribution distribution = new UniformDistribution(low, high);
            Assert.assertEquals(distribution.getLow().doubleValue(), low, DELTA);
            Assert.assertEquals(distribution.getHigh().doubleValue(), high, DELTA);
            Assert.assertEquals(distribution.mean().doubleValue(), args[i++], DELTA);
            final double x = args[i++];
            Assert.assertEquals(distribution.probability(x).doubleValue(), args[i++], DELTA);
            Assert.assertEquals(distribution.cumulative(x).doubleValue(), args[i++], DELTA);
            Assert.assertEquals(distribution.quantile(args[i++]).doubleValue(), args[i++], DELTA);
            Assert.assertEquals(distribution.median().doubleValue(), args[i++], DELTA);
            Assert.assertEquals(distribution.mode().doubleValue(), args[i++], DELTA);
            Assert.assertEquals(distribution.skewness().doubleValue(), args[i++], DELTA);
            Assert.assertEquals(distribution.variance().doubleValue(), args[i++], DELTA);
        }
    }

}
