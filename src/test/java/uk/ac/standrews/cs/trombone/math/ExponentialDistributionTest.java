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
 * Tests {@link ExponentialDistribution}.
 *
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class ExponentialDistributionTest {

    public static final double DELTA = 0.0d;
    //@formatter:off
    private static final double[][] TEST_INPUT_OUTPUT = {
        //rate, prob. & cumulative in, prob. out, cumulative out, quantile in, quantile out, median, mode, skewness, variance
        {0.0011261261261261261, 654.0, 5.391815429000562E-4, 0.5212067899047501, 0.654, 942.4490554848786, 615.5146963372314, 0.0, 2.0, 788544.0}
    };
    //@formatter:on

    /**
     * Tests {@link ExponentialDistribution#ExponentialDistribution(Number)}.
     */
    @Test
    public void testExponentialDistribution() {

        final Number[] bad_args = {Double.NEGATIVE_INFINITY, -Double.MAX_VALUE, -1.0, -0.0, +0.0, Double.NaN};
        for (final Number bad_arg : bad_args) {
            try {
                new ExponentialDistribution(bad_arg);
                fail();
            }
            catch (final IllegalArgumentException e) {
                continue;
            }
        }
        new ExponentialDistribution(HALF);
        new ExponentialDistribution(ONE / HALF);
    }

    /**
     * Tests {@link ExponentialDistribution#byMean(Duration)}.
     */
    @Test
    public void testByMeanDuration() {

        final Duration[] bad_args = {new Duration(), new Duration(-Long.MAX_VALUE, TimeUnit.DAYS)};
        for (final Duration bad_arg : bad_args) {
            try {
                ExponentialDistribution.byMean(bad_arg);
                fail();
            }
            catch (final IllegalArgumentException e) {
                continue;
            }
        }

        final Duration duration = new Duration(1, TimeUnit.DAYS);
        final ExponentialDistribution dist_1 = ExponentialDistribution.byMean(duration);
        Assert.assertEquals(dist_1.mean().longValue(), duration.getLength(TimeUnit.NANOSECONDS));
        Assert.assertEquals(dist_1.rate().longValue(), 1 / duration.getLength(TimeUnit.NANOSECONDS));
    }

    /**
     * Tests {@link ExponentialDistribution#byMean(Number)}.
     */
    @Test
    public void testByMeanNumber() {

        final Number[] bad_args = {Double.NEGATIVE_INFINITY, -Double.MAX_VALUE, -1.0, -0.0, +0.0, Double.NaN};
        for (final Number bad_arg : bad_args) {
            try {
                ExponentialDistribution.byMean(bad_arg);
                fail();
            }
            catch (final IllegalArgumentException e) {
                continue;
            }
        }
        ExponentialDistribution.byMean(HALF);
        ExponentialDistribution.byMean(1.0 / HALF);
    }

    /**
     * Tests rate, mean probability, cumulative, quantile, median,mode, skewness and variance.
     */
    @Test
    public void testCalculations() {

        for (final double[] args : TEST_INPUT_OUTPUT) {
            int i = 0;
            final double rate = args[i++];
            final ExponentialDistribution distribution = new ExponentialDistribution(rate);
            Assert.assertEquals(distribution.rate().doubleValue(), rate, DELTA);
            Assert.assertEquals(distribution.mean().doubleValue(), 1 / rate, DELTA);
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
