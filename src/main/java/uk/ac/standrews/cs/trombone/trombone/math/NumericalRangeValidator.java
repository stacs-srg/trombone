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

public final class NumericalRangeValidator {

    private NumericalRangeValidator() {

    }

    public static void validateRangeLargerThanZeroInclusive(final Number value) {

        validateRangeLargerThan(value, 0.0D, true);
    }

    public static void validateRangeLargerThanZeroExclusive(final Number value) {

        validateRangeLargerThan(value, 0.0D, false);
    }

    public static void validateRangeLargerThanOneInclusive(final Number value) {

        validateRangeLargerThan(value, 1.0D, true);
    }

    public static void validateRangeLargerThan(final Number value, final Number min, final boolean min_inclusive) {

        validateRange(value, min, Double.POSITIVE_INFINITY, min_inclusive, true);
    }

    public static void validateRange(final Number value, final Number min, final Number max, final boolean min_inclusive, final boolean max_inclusive) {

        final double value_d = value.doubleValue();
        final double min_d = min.doubleValue();
        final double max_d = max.doubleValue();

        validateNumber(value_d, min_d, max_d);
        if (min_d > max_d) { throw new IllegalArgumentException("invalid range"); }
        if ((min_inclusive ? value_d < min_d : value_d <= min_d) || (max_inclusive ? value_d > max_d : value_d >= max_d)) {
            final StringBuilder sb = new StringBuilder();
            sb.append("value ");
            sb.append(value);
            sb.append(" must be between ");
            sb.append(min);
            sb.append(min_inclusive ? " (inclusive) " : " (exclusive) ");
            sb.append("and ");
            sb.append(max);
            sb.append(max_inclusive ? " (inclusive)" : " (exclusive)");
            throw new IllegalArgumentException(sb.toString());
        }
    }

    public static void validateRangeZeroToOneExclusive(final Number value) {

        validateRange(value, 0.0D, 1.0D, false, false);
    }

    public static void validateRangeZeroToOneInclusive(final Number value) {

        validateRange(value, 0.0D, 1.0D, true, true);
    }

    public static void validateNumber(final Number... values) {

        if (hasNaN(values)) { throw new IllegalArgumentException("NaN is not allowed"); }
    }

    public static boolean hasNaN(final Number... values) {

        for (final Number value : values) {
            if (Double.isNaN(value.doubleValue())) { return true; }
        }
        return false;
    }
}
