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

/**
 * Implements Exponential probability distribution.
 *
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class ExponentialDistribution implements ProbabilityDistribution {

    private static final long serialVersionUID = -8780132031221192523L;
    private final double rate;

    public ExponentialDistribution(final Number rate) {

        NumericalRangeValidator.validateRangeLargerThanZeroExclusive(rate);
        this.rate = rate.doubleValue();
    }

    public static ExponentialDistribution byMean(final Duration mean_duration) {

        return byMean(mean_duration.getLength(TimeUnit.NANOSECONDS));
    }

    public static ExponentialDistribution byMean(final Number mean) {

        NumericalRangeValidator.validateRangeLargerThanZeroExclusive(mean);
        return new ExponentialDistribution(Math.pow(mean.doubleValue(), -ONE));
    }

    public Number rate() {

        return rate;
    }

    @Override
    public Number probability(final Number x) {

        NumericalRangeValidator.validateRangeLargerThanZeroInclusive(x);
        return rate * Math.exp(-rate * x.doubleValue());
    }

    @Override
    public Number cumulative(final Number x) {

        NumericalRangeValidator.validateRangeLargerThanZeroInclusive(x);
        return ONE - Math.exp(-rate * x.doubleValue());
    }

    @Override
    public Number quantile(final Number probability) {

        NumericalRangeValidator.validateRangeZeroToOneInclusive(probability);
        return -Math.log(ONE - probability.doubleValue()) / rate;
    }

    @Override
    public Number mean() {

        return Math.pow(rate, -ONE);
    }

    @Override
    public Number median() {

        return mean().doubleValue() * WeibullDistribution.NATURAL_LOGARITHM_OF_TWO;
    }

    @Override
    public Number mode() {

        return ZERO;
    }

    @Override
    public Number variance() {

        return Math.pow(rate, -TWO);
    }

    @Override
    public Number skewness() {

        return TWO;
    }

    @Override
    public String toString() {

        return "Exponential(mean: " + mean() + ")";
    }
}
