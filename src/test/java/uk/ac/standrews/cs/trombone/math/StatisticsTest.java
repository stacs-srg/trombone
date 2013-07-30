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

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests  {@link Statistics} and {@link Statistics}.
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class StatisticsTest {

    private static final double CONFIDENCE_LEVEL_95 = .95;
    private static final double CONFIDENCE_LEVEL_96 = .96;
    private static final double CONFIDENCE_LEVEL_97 = .97;
    private static final double CONFIDENCE_LEVEL_98 = .98;
    private static final double CONFIDENCE_LEVEL_99 = .99;
    private static final double PERCENTILE_10 = 10;
    private static final double PERCENTILE_20 = 20;
    private static final double PERCENTILE_30 = 30;
    private static final double PERCENTILE_40 = 40;
    private static final double PERCENTILE_50 = 50;
    private static final double PERCENTILE_60 = 60;
    private static final double PERCENTILE_70 = 70;
    private static final double PERCENTILE_80 = 80;
    private static final double PERCENTILE_90 = 90;
    private static final double PERCENTILE_100 = 100;

    //@formatter:off
    private static final Number[][] TEST_SAMPLES = {
        {},
        {1.0, 5.64, 989.321, .6546, 65, -45, -.5566, -656.544, 77777, 213, 5654},
        {Double.NaN, 544.0, 5.674, 2589.3211, 0.54, 4565, -475, Double.NaN, -55.55366, -6.544, 87.577, 21.3, 54.57},
        {Double.POSITIVE_INFINITY, 544.0, 5.674, 2589.3211, 0.54, 4565, -475, -55.55366, -6.544, 87.577, 21.3, 54.57},
        {Double.NEGATIVE_INFINITY, 544.0, 5.674, 2589.3211, 0.54, 4565, -475, -55.55366, -6.544, 87.577, 21.3, 54.57}
    };

    private static final Number[][] EXPECTED_OUTPUT = {
        //mean, min, max, sample_size, st_dev, sum, sum_sq, ci_95, ci_96, ci_97, ci_98, ci_99, percentile 10 to 100
        {Double.NaN, Double.NaN, Double.NaN, 0L, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN},
        {7636.683181818182, -656.544, 77777.0, 11L, 23326.342776780868, 84003.515, 6.082690903612882E9, 15670.850307467577, 16593.430101146976, 17776.19343980587, 19438.02444082271, 22289.992202212048, -45.0, -0.5566, 0.6546, 1.0, 5.64, 65.0, 213.0, 989.321, 5654.0, 77777.0},
        {Double.NaN, Double.NaN, Double.NaN, 13L, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, -45.75172799999999, -3.7104000000000004, 3.6203999999999983, 18.174799999999998, 54.57, 178.86159999999967, 1362.1284400000009, 3774.7284399999994, Double.NaN, Double.NaN},
        {Double.POSITIVE_INFINITY, -475.0, Double.POSITIVE_INFINITY, 12L, Double.NaN, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, -50.652694, -5.127199999999998, 2.080199999999999, 11.924400000000006, 37.935, 74.37419999999999, 407.07309999999967, 2180.2568800000017, 4367.432110000001, Double.POSITIVE_INFINITY},
        {Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, 4565.0, 12L, Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, -433.05536599999994, -45.75172799999999, -4.418800000000001, 2.593600000000002, 13.487000000000002, 41.261999999999986, 77.67489999999998, 452.71540000000033, 2384.788990000001, 4565.0}
    };
    //@formatter:on

    private Statistics[] test_statistics;
    private DescriptiveStatistics[] test_statistics2;

    /**
     * Initialises test statistics objects and adds test samples to each.
     */
    @Before
    public void setup() {

        test_statistics = new Statistics[TEST_SAMPLES.length];
        test_statistics2 = new DescriptiveStatistics[TEST_SAMPLES.length];
        for (int i = 0; i < test_statistics.length; i++) {
            final Statistics stats = new Statistics();
            stats.addSamples(TEST_SAMPLES[i]);
            test_statistics[i] = stats;
            final DescriptiveStatistics statistics = new DescriptiveStatistics();
            for (final Number n : TEST_SAMPLES[i]) {
                statistics.addValue(n.doubleValue());
            }
            test_statistics2[i] = statistics;

        }

    }

    /**
     * Tests {@link Statistics#addSample(double)}.
     */
    @Test
    public void testAddSample() {

        int i = 0;
        for (final Statistics stats : test_statistics) {
            final long sample_length = TEST_SAMPLES[i++].length;
            Assert.assertEquals(stats.getSampleSize().longValue(), sample_length);
            stats.addSample(Double.NaN);
            Assert.assertEquals(stats.getSampleSize().longValue(), sample_length + 1);
        }
    }

    /**
     * Tests {@link Statistics#reset()}.
     */
    @Test
    public void testReset() {

        for (final Statistics stats : test_statistics) {
            stats.reset();
            testInitialState(stats);
        }
    }

    /**
     * Tests arithmetic operations in {@link Statistics}.
     */
    @Test
    public void testArithmetics() {

        for (int i = 0, j = 0; i < test_statistics.length; i++) {
            final Statistics stats = test_statistics[i];
            Assert.assertEquals(stats.getMean(), EXPECTED_OUTPUT[i][j++]);
            Assert.assertEquals(stats.getMin(), EXPECTED_OUTPUT[i][j++]);
            Assert.assertEquals(stats.getMax(), EXPECTED_OUTPUT[i][j++]);
            Assert.assertEquals(stats.getSampleSize(), EXPECTED_OUTPUT[i][j++]);
            Assert.assertEquals(stats.getStandardDeviation(), EXPECTED_OUTPUT[i][j++]);
            Assert.assertEquals(stats.getSum(), EXPECTED_OUTPUT[i][j++]);
            Assert.assertEquals(stats.getSumOfSquares(), EXPECTED_OUTPUT[i][j++]);
            Assert.assertEquals(stats.getConfidenceInterval(CONFIDENCE_LEVEL_95), EXPECTED_OUTPUT[i][j++]);
            Assert.assertEquals(stats.getConfidenceInterval(CONFIDENCE_LEVEL_96), EXPECTED_OUTPUT[i][j++]);
            Assert.assertEquals(stats.getConfidenceInterval(CONFIDENCE_LEVEL_97), EXPECTED_OUTPUT[i][j++]);
            Assert.assertEquals(stats.getConfidenceInterval(CONFIDENCE_LEVEL_98), EXPECTED_OUTPUT[i][j++]);
            Assert.assertEquals(stats.getConfidenceInterval(CONFIDENCE_LEVEL_99), EXPECTED_OUTPUT[i][j++]);
            Assert.assertEquals(stats.getPercentile(PERCENTILE_10), EXPECTED_OUTPUT[i][j++]);
            Assert.assertEquals(stats.getPercentile(PERCENTILE_20), EXPECTED_OUTPUT[i][j++]);
            Assert.assertEquals(stats.getPercentile(PERCENTILE_30), EXPECTED_OUTPUT[i][j++]);
            Assert.assertEquals(stats.getPercentile(PERCENTILE_40), EXPECTED_OUTPUT[i][j++]);
            final Number number = EXPECTED_OUTPUT[i][j++];
            System.out.println(test_statistics2[i].getPercentile(PERCENTILE_50));
            System.out.println(stats.getPercentile(PERCENTILE_50));
            //            System.out.println(test_statistics2[i].getPercentile(0));
            System.out.println(stats.getPercentile(0));

            System.out.println();
            Assert.assertEquals(stats.getPercentile(PERCENTILE_50), number);
            Assert.assertEquals(stats.getPercentile(PERCENTILE_60), EXPECTED_OUTPUT[i][j++]);
            Assert.assertEquals(stats.getPercentile(PERCENTILE_70), EXPECTED_OUTPUT[i][j++]);
            Assert.assertEquals(stats.getPercentile(PERCENTILE_80), EXPECTED_OUTPUT[i][j++]);
            Assert.assertEquals(stats.getPercentile(PERCENTILE_90), EXPECTED_OUTPUT[i][j++]);
            Assert.assertEquals(stats.getPercentile(PERCENTILE_100), EXPECTED_OUTPUT[i][j++]);
            j = 0;
        }
    }

    private static void testInitialState(final Statistics empty_statistics) {

        Assert.assertEquals(empty_statistics.getSampleSize().longValue(), 0L);
        Assert.assertEquals(empty_statistics.getMax(), Double.NaN);
        Assert.assertEquals(empty_statistics.getMin(), Double.NaN);
        Assert.assertEquals(empty_statistics.getSum(), Double.NaN);
        Assert.assertEquals(empty_statistics.getSumOfSquares(), Double.NaN);
    }

}
