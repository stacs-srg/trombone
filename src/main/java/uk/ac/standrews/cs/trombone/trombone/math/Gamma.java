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

import org.apache.commons.math3.util.FastMath;

public final class Gamma {

    /** Lanczos coefficients. */
    private static final double[] LANCZOS_COEFFICIENTS = {0.99999999999999709182, 57.156235665862923517, -59.597960355475491248, 14.136097974741747174, -0.49191381609762019978, .33994649984811888699e-4, .46523628927048575665e-4, -.98374475304879564677e-4, .15808870322491248884e-3,
                    -.21026444172410488319e-3, .21743961811521264320e-3, -.16431810653676389022e-3, .84418223983852743293e-4, -.26190838401581408670e-4, .36899182659531622704e-5,};
    private static final double LANCZOS_G = 607.0 / 128.0;
    /** Avoid repeated computation of log of 2 PI in logGamma. */
    private static final double HALF_LOG_2_PI = ProbabilityDistribution.HALF * FastMath.log(ProbabilityDistribution.TWO * FastMath.PI);

    private Gamma() {

    }

    public static double gamma(final double x) {

        return Math.exp(logGamma(x));
    }

    /**
     * Returns the natural logarithm of the gamma function &#915;(x).
     *
     * The implementation of this method is based on:
     * <ul>
     * <li><a href="http://mathworld.wolfram.com/GammaFunction.html">
     * Gamma Function</a>, equation (28).</li>
     * <li><a href="http://mathworld.wolfram.com/LanczosApproximation.html">
     * Lanczos Approximation</a>, equations (1) through (5).</li>
     * <li><a href="http://my.fit.edu/~gabdo/gamma.txt">Paul Godfrey, A note on
     * the computation of the convergent Lanczos complex Gamma approximation
     * </a></li>
     * </ul>
     *
     * @param x Value.
     * @return log(&#915;(x))
     */
    public static double logGamma(final double x) {

        final double result;

        if (Double.isNaN(x) || x <= 0.0) {
            result = Double.NaN;
        }
        else {

            double sum = 0.0;
            for (int i = LANCZOS_COEFFICIENTS.length - 1; i > 0; --i) {
                sum = sum + LANCZOS_COEFFICIENTS[i] / (x + i);
            }
            sum = sum + LANCZOS_COEFFICIENTS[0];

            final double tmp = x + LANCZOS_G + ProbabilityDistribution.HALF;
            result = (x + ProbabilityDistribution.HALF) * Math.log(tmp) - tmp + HALF_LOG_2_PI + Math.log(sum / x);
        }

        return result;
    }
}
