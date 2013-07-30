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

import java.io.Serializable;

/**
 * The Interface ProbabilityDistribution.
 *
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public interface ProbabilityDistribution extends Serializable {

    /** The zero. */
    double ZERO = 0.0D;
    /** The half. */
    double HALF = 0.5D;
    /** The one. */
    double ONE = HALF + HALF;
    /** The two. */
    double TWO = ONE + ONE;
    /** The three. */
    double THREE = TWO + ONE;
    /** The ten. */
    double TEN = THREE + THREE + THREE + ONE;
    /** The twelve. */
    double TWELVE = TEN + TWO;
    /** The one hundred. */
    double ONE_HUNDRED = TEN * TEN;

    /**
     * Probability Density Function (PDF).
     *
     * @param x the random variable
     * @return the relative likelihood of the occurrence of the random variable <code>x</code>
     */
    Number probability(Number x);

    /**
     * Cumulative Distribution Function (CDF).
     *
     * @param x the random variable
     * @return the likelihood of the occurrence of the random variables less than or equal to <code>x</code>
     */
    Number cumulative(Number x);

    /**
     * Given a probability, calculates the maximum random variable Inverse of {@link #cumulative(Number)}.
     *
     * @param probability the probability
     * @return the number
     */
    Number quantile(Number probability);

    /**
     * Mean.
     *
     * @return the number
     */
    Number mean();

    /**
     * Median.
     *
     * @return the number
     */
    Number median();

    /**
     * Mode.
     *
     * @return the number
     */
    Number mode();

    /**
     * Variance.
     *
     * @return the number
     */
    Number variance();

    /**
     * Skewness.
     *
     * @return the number
     */
    Number skewness();
}
