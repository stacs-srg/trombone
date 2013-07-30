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

public class TDistribution implements ProbabilityDistribution {

    private final double df;
    private final double df_plus_1_over_2;

    public TDistribution(final Number degrees_of_freedom) {

        NumericalRangeValidator.validateRangeLargerThanZeroExclusive(degrees_of_freedom);
        df = degrees_of_freedom.doubleValue();
        df_plus_1_over_2 = (df + ONE) / TWO;
    }

    @Override
    public Number probability(final Number x) {

        final double x_d = x.doubleValue();
        return Gamma.gamma(df_plus_1_over_2) / (Math.sqrt(df * Math.PI) * Gamma.gamma(df / TWO)) * Math.pow(1 + Math.pow(x_d, TWO) / df, -df_plus_1_over_2);
    }

    @Override
    public Number cumulative(final Number x) {

        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Number quantile(final Number probability) {

        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Number mean() {

        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Number median() {

        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Number mode() {

        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Number variance() {

        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Number skewness() {

        throw new UnsupportedOperationException("not implemented");
    }

}
