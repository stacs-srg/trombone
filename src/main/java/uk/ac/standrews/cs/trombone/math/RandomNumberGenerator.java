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

import java.util.Random;
import java.util.concurrent.TimeUnit;
import uk.ac.standrews.cs.shabdiz.util.Duration;

/**
 * A Random Number Generator (RNG) where the generated numbers are derived from a given {@link ProbabilityDistribution}.
 * This RNG uses <a href="http://en.wikipedia.org/wiki/Inverse_transform_sampling">Inverse Transform Sampling</a> to generate random numbers:
 * <blockquote>
 * The problem that the inverse transform sampling method solves is as follows:<br/>
 * <ul>
 *    <li>Let {@code X} be a random variable whose distribution can be described by the cumulative distribution function {@code F}
 *    <li>We want to generate values of {@code X} which are distributed according to this distribution.</li>
 * </ul> The inverse transform sampling method works as follows: <br/>
 * <ol>
 *    <li>Generate a random number {@code u} from the standard uniform distribution in the interval {@code [0,1]}
 *    <li>Compute the value {@code x} such that {@code F(x) = u}
 *    <li>Take {@code x} to be the random number drawn from the distribution described by {@code F}
 * </ol>
 * </blockquote>
 *
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class RandomNumberGenerator {

    private final ProbabilityDistribution distribution;
    private final Random uniform_random;

    /**
     * Instantiates a non-deterministic random number generator where the generated numbers are distributed according to the given probability distribution.
     *
     * @param distribution the distribution
     */
    public RandomNumberGenerator(final ProbabilityDistribution distribution) {

        this(distribution, new Random());
    }

    /**
     * Instantiates a deterministic random number generator where the generated numbers are distributed according to the given probability distribution.
     *
     * @param distribution the probability distribution
     * @param seed the uniform random generator's seed
     */
    public RandomNumberGenerator(final ProbabilityDistribution distribution, final long seed) {

        this(distribution, new Random(seed));
    }

    /**
     * Instantiates a random number generator where the generated numbers are distributed according to the given probability distribution and the given {@link Random} is used to generate uniform random numbers.
     * @param distribution the probability distribution
     * @param uniform_random a uniform random number generator
     */
    public RandomNumberGenerator(final ProbabilityDistribution distribution, final Random uniform_random) {

        this.distribution = distribution;
        this.uniform_random = uniform_random;
    }

    /**
     * Generates a random number that is distributed according to the given probability distribution.
     *
     * @return a random number derived from the given probability distribution
     */
    public Number generate() {

        return generate(distribution, uniform_random);
    }

    /**
     * Gets the probability distribution.
     *
     * @return the probability distribution
     */
    public ProbabilityDistribution getProbabilityDistribution() {

        return distribution;
    }

    /**
     * Generates duration with a random length, and time unit of {@link TimeUnit#NANOSECONDS}.
     * @param distribution the probability distribution based on which the length should be randomly generated
     * @param uniform_random a uniform random number generator
     * @return a duration with a random length, and time unit of {@link TimeUnit#NANOSECONDS}
     */
    public static Duration generateDurationInNanoseconds(final ProbabilityDistribution distribution, final Random uniform_random) {

        return generateDuration(distribution, uniform_random, TimeUnit.NANOSECONDS);
    }

    /**
     * Generates duration with a random length, and the given time unit.
     *
     * @param distribution the probability distribution based on which the length should be randomly generated
     * @param uniform_random a uniform random number generator
     * @param unit the time unit of the generated duration
     * @return a duration with a random length, and the given time unit
     */
    public static Duration generateDuration(final ProbabilityDistribution distribution, final Random uniform_random, final TimeUnit unit) {

        return new Duration(generate(distribution, uniform_random).longValue(), unit);
    }

    /**
     * Generates a random number that is distributed according to the given probability distribution.
     *
     * @param distribution the distribution
     * @param uniform_random a uniform random number generator
     * @return a random number derived from the given probability distribution
     */
    public static Number generate(final ProbabilityDistribution distribution, final Random uniform_random) {

        synchronized (uniform_random) {
            return distribution.quantile(uniform_random.nextDouble());
        }
    }
}
