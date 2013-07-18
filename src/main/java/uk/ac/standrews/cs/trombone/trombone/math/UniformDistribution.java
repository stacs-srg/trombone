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

import java.util.concurrent.TimeUnit;
import uk.ac.standrews.cs.shabdiz.util.Duration;

public class UniformDistribution implements ProbabilityDistribution {

    public static final UniformDistribution ZERO_UNIFORM_DISTRIBUTION = new UniformDistribution(0, 0);

    private final double low;
    private final double high;

    public UniformDistribution(final Duration low, final Duration high) {

        this(low.getLength(TimeUnit.NANOSECONDS), high.getLength(TimeUnit.NANOSECONDS));
    }

    public UniformDistribution(final Number low, final Number high) {

        NumericalRangeValidator.validateNumber(low, high);
        this.low = low.doubleValue();
        this.high = high.doubleValue();
        // Allow high = low
        if (this.high < this.low) { throw new IllegalArgumentException("lower bound must be less than higher bound"); }
    }

    public Number getLow() {

        return low;
    }

    public Number getHigh() {

        return high;
    }

    @Override
    public Number probability(final Number x) {

        final double x_d = x.doubleValue();
        return x_d < low || x_d > high ? ZERO : ONE / (high - low);
    }

    @Override
    public Number cumulative(final Number x) {

        final double x_d = x.doubleValue();
        return x_d < low ? ZERO : x_d >= high ? ONE : (x_d - low) / (high - low);
    }

    @Override
    public Number quantile(final Number probability) {

        NumericalRangeValidator.validateRangeZeroToOneInclusive(probability);
        return low + probability.doubleValue() * (high - low);
    }

    @Override
    public Number mean() {

        return (low + high) / TWO;
    }

    @Override
    public Number median() {

        return mean();
    }

    @Override
    public Number mode() {

        return low; // Any value between low to high (inclusive) may be mode
    }

    @Override
    public Number variance() {

        return Math.pow(high - low, TWO) / TWELVE;
    }

    @Override
    public Number skewness() {

        return ZERO;
    }

    @Override
    public String toString() {

        return "Uniform, low: " + low + ", high: " + high;
    }
}
