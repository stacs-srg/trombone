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
 * Tests {@link WeibullDistribution}.
 *
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class WeibullDistributionTest {

    public static final double DELTA = 0.0d;
    //@formatter:off
    private static final double[][] TEST_INPUT_OUTPUT = {
        {0.8842, 852456, 905853.2908924981, 457, 2.4778507908955887E-6, 0.0012815021029850548, 0.457, 487985.81582461746, 563184.9037453446, Double.NaN, -1.8566473246120316, 1.0545908823268892E12},
        {1.5, 50, 45.13726464754669, 11, 0.012691650910201193, 0.09804364473659022, 0.11, 11.929016803104716, 39.160988438732566, 24.037492838456807, -2.4427852784427633, 939.2257120348302}
    };
    //@formatter:on

    /**
     * Tests {@link WeibullDistribution#WeibullDistribution(Number, Number)}.
     */
    @Test
    public void testWeibullDistribution() {

        final Number[][] bad_args = {{Double.NEGATIVE_INFINITY, -Double.MAX_VALUE}, {-Double.MAX_VALUE, -Double.MAX_VALUE}, {-Double.MAX_VALUE, Double.NEGATIVE_INFINITY}, {0.0, -0.0}, {-1.0, -0.0}, {-1.0, -0.0}, {Double.NaN, Double.NaN}, {+0.0, Double.NaN}};
        for (final Number[] bad_arg : bad_args) {
            try {
                new WeibullDistribution(bad_arg[0], bad_arg[1]);
                fail();
            }
            catch (final IllegalArgumentException e) {
                continue;
            }
        }

        final Number[][] good_args = {{Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY}, {Double.MAX_VALUE, Double.MAX_VALUE}, {ONE, ONE}, {HALF, HALF}, {ONE / HALF, ONE}};
        for (final Number[] good_arg : good_args) {
            final Number shape = good_arg[0];
            final Number scale = good_arg[1];
            final WeibullDistribution test_dist = new WeibullDistribution(shape, scale);
            Assert.assertEquals(test_dist.getShape().doubleValue(), shape);
            Assert.assertEquals(test_dist.getScale().doubleValue(), scale);
        }
    }

    /**
     * Tests {@link WeibullDistribution#byMean(Number, Duration)}.
     */
    @Test
    public void testByMeanDuration() {

        ////@formatter:off
        final Object[][] bad_args = {
            {Double.NEGATIVE_INFINITY, new Duration()},
            {-Double.MAX_VALUE, new Duration()},
            {-Double.MAX_VALUE, new Duration()},
            {0.0, new Duration(-1, TimeUnit.NANOSECONDS)},
            {-1.0, new Duration(-1, TimeUnit.NANOSECONDS)},
            {-1.0, new Duration(-1, TimeUnit.NANOSECONDS)},
            {Double.NaN, new Duration(-1, TimeUnit.NANOSECONDS)},
            {+0.0, new Duration(-1, TimeUnit.NANOSECONDS)}
        };
        ////@formatter:on
        for (final Object[] bad_arg : bad_args) {
            try {
                WeibullDistribution.byMean((Number) bad_arg[0], (Duration) bad_arg[1]);
                fail();
            }
            catch (final IllegalArgumentException e) {
                continue;
            }
        }

        ////@formatter:off
        final Object[][] good_args = {
                        {Double.POSITIVE_INFINITY, Duration.MAX_DURATION},
                        {Double.MAX_VALUE, Duration.MAX_DURATION},
                        {1.0, new Duration(1, TimeUnit.NANOSECONDS)},
                        {1, new Duration(1, TimeUnit.NANOSECONDS)},
        };
        ////@formatter:on
        for (final Object[] good_arg : good_args) {
            final Duration duration = (Duration) good_arg[1];
            final double shape = ((Number) good_arg[0]).doubleValue();
            final double mean = duration.getLength(TimeUnit.NANOSECONDS);
            final WeibullDistribution test_dist = WeibullDistribution.byMean(shape, duration);
            Assert.assertTrue(test_dist.mean().longValue() - mean == 0.0);
            Assert.assertEquals(test_dist.getShape().doubleValue(), shape, DELTA);
        }
    }

    /**
     * Tests {@link WeibullDistribution#byMean(Number, Number)}.
     */
    @Test
    public void testByMeanNumber() {

        ////@formatter:off
        final Number[][] bad_args = {
            {Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY},
            {-Double.MAX_VALUE, -Double.MAX_VALUE},
            {-Double.MAX_VALUE, Double.NEGATIVE_INFINITY},
            {Double.NEGATIVE_INFINITY, -Double.MAX_VALUE},
            {0.0, -1},
            {Double.NaN, +0.0},
            {-1.0, Double.NaN},
            {Double.NaN, Double.NaN}
        };
        ////@formatter:on
        for (final Number[] bad_arg : bad_args) {
            try {
                WeibullDistribution.byMean(bad_arg[0], bad_arg[1]);
                fail();
            }
            catch (final IllegalArgumentException e) {
                continue;
            }
        }

        ////@formatter:off
        final Number[][] good_args = {
                        {Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY},
                        {Double.MAX_VALUE, Double.POSITIVE_INFINITY},
                        {1.0, 1000.0},
                        {1.0, 1000.0}
        };
        ////@formatter:on
        for (final Number[] good_arg : good_args) {
            final Number shape = good_arg[0];
            final Number mean = good_arg[1];
            final WeibullDistribution test_dist = WeibullDistribution.byMean(shape, mean);
            Assert.assertEquals(test_dist.mean(), mean);
            Assert.assertEquals(test_dist.getShape(), shape);
        }
    }

    /**
     * Tests rate, mean probability, cumulative, quantile, median,mode, skewness and variance.
     */
    @Test
    public void testCalculations() {

        for (final double[] args : TEST_INPUT_OUTPUT) {
            int i = 0;
            final double shape = args[i++];
            final double scale = args[i++];
            final WeibullDistribution distribution = new WeibullDistribution(shape, scale);
            Assert.assertEquals(distribution.getShape().doubleValue(), shape, DELTA);
            Assert.assertEquals(distribution.getScale().doubleValue(), scale, DELTA);
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
