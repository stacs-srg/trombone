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

import org.junit.Test;

import static org.junit.Assert.fail;

/**
 * Tests {@link NumericalRangeValidator}.
 * 
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class NumberValidatorTest {

    /** 
     * Tests {@link NumericalRangeValidator#validateRangeLargerThanZeroInclusive(Number)}.
     */
    @Test
    public void testValidateRangeLargerThanZeroInclusive() {

        final double[] bad_args = {Double.NEGATIVE_INFINITY, -Double.MAX_VALUE, -1.0, -100.0, Double.NaN};

        for (final double arg : bad_args) {
            try {
                NumericalRangeValidator.validateRangeLargerThanZeroInclusive(arg);
                fail();
            }
            catch (final IllegalArgumentException e) {
                continue;
            }
        }

        final double[] good_args = {Double.POSITIVE_INFINITY, Double.MAX_VALUE, 1.0, 100.0, -0.0, +0.0};
        for (final double arg : good_args) {
            NumericalRangeValidator.validateRangeLargerThanZeroInclusive(arg);
        }
    }

    /**
     * Tests {@link NumericalRangeValidator#validateRangeLargerThanZeroExclusive(Number)}.
     */
    @Test
    public void testValidateRangeLargerThanZeroExclusive() {

        final double[] bad_args = {Double.NEGATIVE_INFINITY, -Double.MAX_VALUE, -1.0, -100.0, -0.0, +0.0, Double.NaN};

        for (final double arg : bad_args) {
            try {
                NumericalRangeValidator.validateRangeLargerThanZeroExclusive(arg);
                fail();
            }
            catch (final IllegalArgumentException e) {
                continue;
            }
        }

        final double[] good_args = {Double.POSITIVE_INFINITY, Double.MAX_VALUE, 1.0, 100.0};
        for (final double arg : good_args) {
            NumericalRangeValidator.validateRangeLargerThanZeroExclusive(arg);
        }
    }

    /**
     * Tests {@link NumericalRangeValidator#validateRangeLargerThan(Number, Number, boolean)}.
     */
    @Test
    public void testValidateRangeLargerThan() {

        //@formatter:off
        final Object[][] bad_args = {
                        {Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, false},
                        {Double.NEGATIVE_INFINITY, Double.NaN, false},
                        {0.0, Double.POSITIVE_INFINITY, false},
                        {-100.0, 5.0, true}
        }; //@formatter:on

        for (final Object[] arg : bad_args) {
            try {
                NumericalRangeValidator.validateRangeLargerThan((Number) arg[0], (Number) arg[1], (Boolean) arg[2]);
                fail();
            }
            catch (final IllegalArgumentException e) {
                continue;
            }
        }

        //@formatter:off
        final Object[][] good_args = {
                        {Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, true},
                        {Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, true},
                        {Double.POSITIVE_INFINITY, 0.0, false},
                        {-5.0, -500.0, true}
        }; //@formatter:on
        for (final Object[] arg : good_args) {
            NumericalRangeValidator.validateRangeLargerThan((Number) arg[0], (Number) arg[1], (Boolean) arg[2]);
        }
    }

    /**
     * Tests {@link NumericalRangeValidator#validateRange(Number, Number, Number, boolean, boolean)}.
     */
    @Test
    public void testValidateRange() {

        //@formatter:off
        final Object[][] bad_args = {
                        {Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, false, false},
                        {Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, false, false},
                        {Double.NEGATIVE_INFINITY, 10, Double.POSITIVE_INFINITY, false, false},
                        {Double.NaN, Double.NaN, Double.NaN, false, false},
                        {Double.NaN, Double.NaN, Double.NaN, true, true},
                        {Double.NaN, Double.NaN, Double.NaN, false, true},
                        {Double.NaN, Double.NaN, Double.NaN, true, false},
                        {0.0, 100, -100, true, false},
                        {-100.0, 5.0, 10, true, true}
        }; //@formatter:on

        for (final Object[] arg : bad_args) {
            int i;
            try {
                i = 0;
                NumericalRangeValidator.validateRange((Number) arg[i++], (Number) arg[i++], (Number) arg[i++], (Boolean) arg[i++], (Boolean) arg[i++]);
                fail();
            }
            catch (final IllegalArgumentException e) {
                continue;
            }
        }

        //@formatter:off
        final Object[][] good_args = {
                        {Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, true, true},
                        {Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, true, true},
                        {10, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, false, false},
                        {5, -9, 5, false, true},
                        {5, -9, 50, false, false},
                        {5, 5, 5, true, true}
        }; //@formatter:on
        int i;
        for (final Object[] arg : good_args) {
            i = 0;
            NumericalRangeValidator.validateRange((Number) arg[i++], (Number) arg[i++], (Number) arg[i++], (Boolean) arg[i++], (Boolean) arg[i++]);
        }
    }

    /**
     * Tests {@link NumericalRangeValidator#validateRangeZeroToOneInclusive(Number)}.
     */
    @Test
    public void testValidateRangeZeroToOneInclusive() {

        final double[] bad_args = {Double.NEGATIVE_INFINITY, -Double.MAX_VALUE, Double.POSITIVE_INFINITY, Double.MAX_VALUE, -1.0, -100.0, 1.000001, Double.NaN};

        for (final double arg : bad_args) {
            try {
                NumericalRangeValidator.validateRangeZeroToOneInclusive(arg);
                fail();
            }
            catch (final IllegalArgumentException e) {
                continue;
            }
        }

        final double[] good_args = {+0.0, -0.0, 0.999999999999999999, 1.0};
        for (final double arg : good_args) {
            NumericalRangeValidator.validateRangeZeroToOneInclusive(arg);
        }
    }

    /**
     * Tests {@link NumericalRangeValidator#validateNumber(Number...)}.
     */
    @Test
    public void testValidateNumber() {

        try {
            NumericalRangeValidator.validateNumber(Double.NaN, Double.NaN, Double.NaN);
            fail();
        }
        catch (final IllegalArgumentException e) {
            //ignore; expected
        }

        final Number[] good_args = {+0.0, -0.0, 0.999999999999999999, 1.0, Double.NEGATIVE_INFINITY, -Double.MAX_VALUE, Double.POSITIVE_INFINITY, Double.MAX_VALUE, -1.0, -100.0, 1.000001};
        for (final Number arg : good_args) {
            NumericalRangeValidator.validateNumber(arg);
        }
        NumericalRangeValidator.validateNumber(good_args);
    }

}
