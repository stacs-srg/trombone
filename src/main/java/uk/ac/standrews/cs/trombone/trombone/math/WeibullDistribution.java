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

public class WeibullDistribution implements ProbabilityDistribution {

    static final double NATURAL_LOGARITHM_OF_TWO = Math.log(TWO);
    private final double shape;
    private final double scale;

    public WeibullDistribution(final Number shape, final Number scale) {

        NumericalRangeValidator.validateRangeLargerThanZeroExclusive(shape);
        NumericalRangeValidator.validateRangeLargerThanZeroExclusive(scale);
        this.shape = shape.doubleValue();
        this.scale = scale.doubleValue();
    }

    public static WeibullDistribution byMean(final Number shape, final Number mean) {

        NumericalRangeValidator.validateRangeLargerThanZeroExclusive(shape);
        NumericalRangeValidator.validateRangeLargerThanZeroExclusive(mean);
        final Number scale = calculateScale(shape, mean);
        return new WeibullDistribution(shape, scale);
    }

    public static WeibullDistribution byMean(final Number shape, final Duration mean_duration) {

        return byMean(shape, mean_duration.getLength(TimeUnit.NANOSECONDS));
    }

    public Number getShape() {

        return shape;
    }

    public Number getScale() {

        return scale;
    }

    @Override
    public Number probability(final Number x) {

        NumericalRangeValidator.validateRangeLargerThanZeroInclusive(x);
        final double x_by_scale = x.doubleValue() / scale;
        return shape / scale * Math.pow(x_by_scale, shape - ONE) * Math.exp(-Math.pow(x_by_scale, shape));
    }

    @Override
    public Number cumulative(final Number x) {

        NumericalRangeValidator.validateRangeLargerThanZeroInclusive(x);
        final double x_by_scale = x.doubleValue() / scale;
        return 1 - Math.exp(-Math.pow(x_by_scale, shape));
    }

    @Override
    public Number quantile(final Number probability) {

        NumericalRangeValidator.validateRangeZeroToOneInclusive(probability);
        return scale * Math.pow(-Math.log(ONE - probability.doubleValue()), ONE / shape);
    }

    @Override
    public Number mean() {

        return scale * Gamma.gamma(ONE + ONE / shape);
    }

    @Override
    public Number median() {

        return scale * Math.pow(NATURAL_LOGARITHM_OF_TWO, ONE / shape);
    }

    @Override
    public Number mode() {

        return shape == ONE ? ZERO : scale * Math.pow((shape - ONE) / shape, ONE / shape);
    }

    @Override
    public Number variance() {

        return Math.pow(scale, TWO) * Gamma.gamma(ONE + TWO / shape) - Math.pow(mean().doubleValue(), TWO);
    }

    @Override
    public Number skewness() {

        final double mean = mean().doubleValue();
        final double sd = StatisticsStateless.standardDeviation(this).doubleValue();
        return (Gamma.gamma(ONE + TWO / shape) * Math.pow(scale, THREE) - THREE * mean * Math.pow(sd, TWO) - Math.pow(mean, THREE)) / Math.pow(sd, THREE);
    }

    @Override
    public String toString() {

        return "Weibull(shape: " + shape + ", scale: " + scale + ")";
    }

    private static double calculateScale(final Number shape, final Number mean) {

        return mean.doubleValue() / Gamma.gamma(ONE + ONE / shape.doubleValue());
    }
}
