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

import org.junit.Assert;
import org.junit.Test;

import static uk.ac.standrews.cs.trombone.math.Gamma.gamma;
import static uk.ac.standrews.cs.trombone.math.Gamma.logGamma;

/**
 * Tests {@link Gamma} function's implementation.
 *
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class GammaTest {

    public static final double DELTA = 0.0d;
    //@formatter:off
    private static final double[][] TEST_INPUT_OUTPUTS = {
        // in                           out logGamma            out gamma
        {Double.NaN,                    Double.NaN,             Double.NaN},
        {-1.0,                          Double.NaN,             Double.NaN},
        {0.0,                           Double.NaN,             Double.NaN},
        {Double.NEGATIVE_INFINITY,      Double.NaN,             Double.NaN},
        {Double.POSITIVE_INFINITY,      Double.NaN,             Double.NaN},
        {1.0,                           -4.440892098500626E-16, 0.9999999999999996},
        {1.654546546,                   -0.1044578864710679,    0.9008127330111706},
        {998877445566332211.987456321,  4.040000631630226E19,   Double.POSITIVE_INFINITY}
    };
    //@formatter:on

    /**
     * Tests {@link Gamma#logGamma(double)}.
     */
    @Test
    public void testLogGamma() {

        for (final double[] args : TEST_INPUT_OUTPUTS) {
            Assert.assertEquals(logGamma(args[0]), args[1], DELTA);
        }
    }

    /**
     * Tests {@link Gamma#gamma(double)}.
     */
    @Test
    public void testGamma() {

        for (final double[] args : TEST_INPUT_OUTPUTS) {
            Assert.assertEquals(gamma(args[0]), args[2], DELTA);
        }
    }
}
