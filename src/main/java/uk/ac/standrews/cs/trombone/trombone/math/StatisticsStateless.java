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
package uk.ac.standrews.cs.trombone.trombone.math;

import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.math3.distribution.TDistribution;
import uk.ac.standrews.cs.trombone.trombone.util.AtomicNumber;

public class StatisticsStateless {

    private AtomicLong sample_size;
    private AtomicNumber min;
    private AtomicNumber max;
    private AtomicNumber sum;
    private AtomicNumber sum_of_squares;

    public StatisticsStateless() {

        initialise();
    }

    public static Number arithmeticMean(final Number sum, final Number sample_size) {

        return sum.doubleValue() / sample_size.doubleValue();
    }

    public static final Number confidenceInterval(final Long sample_size, final Number standard_deviation, final Number conf_level) {

        NumericalRangeValidator.validateRangeLargerThanZeroExclusive(conf_level);
        NumericalRangeValidator.validateRangeLargerThanZeroInclusive(sample_size);

        final double confidence_interval;
        if (sample_size < 1 || NumericalRangeValidator.hasNaN(standard_deviation)) {
            confidence_interval = Double.NaN;
        }
        else {
            final long df = sample_size - 1; // calculate degree of freedom
            final double probability = 1 - (1 - conf_level.doubleValue()) / 2; // calculate equivalent probability
            confidence_interval = new TDistribution(df).inverseCumulativeProbability(probability) * standard_deviation.doubleValue() / Math.sqrt(sample_size);
        }

        return confidence_interval;
    }

    public static Number standardDeviation(final Number sum, final Number sum_of_squares, final Long sample_size) {

        return Math.sqrt((sample_size * sum_of_squares.doubleValue() - squared(sum)) / (sample_size * (sample_size - 1)));
    }

    public static Number standardDeviation(final ProbabilityDistribution distribution) {

        return Math.sqrt(distribution.variance().doubleValue());
    }

    public void addSample(final Number value) {

        sample_size.incrementAndGet();
        if (sample_size.get() == 1) {
            min.set(value);
            max.set(value);
            sum.set(value);
            sum_of_squares.set(squared(value));
        }
        else {
            min.setIfSmallerAndGet(value);
            max.setIfGreaterAndGet(value);
            sum.addAndGet(value);
            sum_of_squares.addAndGet(squared(value));
        }
    }

    public void addSamples(final Number[] samples) {

        for (final Number sample : samples) {
            addSample(sample);
        }
    }

    public synchronized void reset() {

        initialise();
    }

    public Number getMean() {

        return arithmeticMean(sum.get(), sample_size.get());
    }

    public Number getSum() {

        return sum.get();
    }

    public Number getSumOfSquares() {

        return sum_of_squares.get();
    }

    public Long getSampleSize() {

        return sample_size.get();
    }

    public Number getMax() {

        return max.get();
    }

    public Number getMin() {

        return min.get();
    }

    public Number getStandardDeviation() {

        return standardDeviation(sum.get(), sum_of_squares.get(), sample_size.get());
    }

    public Number getConfidenceInterval(final Number conf_level) {

        return confidenceInterval(sample_size.get(), getStandardDeviation(), conf_level);
    }

    protected static double squared(final Number value) {

        return Math.pow(value.doubleValue(), ProbabilityDistribution.TWO);
    }

    private void initialise() {

        sample_size = new AtomicLong();
        min = new AtomicNumber(Double.NaN);
        max = new AtomicNumber(Double.NaN);
        sum = new AtomicNumber(Double.NaN);
        sum_of_squares = new AtomicNumber(Double.NaN);

    }
}
