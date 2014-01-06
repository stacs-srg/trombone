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
package uk.ac.standrews.cs.trombone.evaluation.churn;

import java.util.Random;
import org.mashti.sina.distribution.ProbabilityDistribution;
import org.mashti.sina.util.NumericalRangeValidator;
import org.mashti.sina.util.RandomNumberGenerator;

public abstract class UncorrelatedChurn implements Churn {

    private final Random uniform_random;
    private boolean exposed;

    protected UncorrelatedChurn(final double availability, final long seed) {

        NumericalRangeValidator.validateRangeZeroToOneExclusive(availability);
        uniform_random = new Random(seed);
        exposed = availability > uniform_random.nextDouble();
    }

    @Override
    public synchronized final Availability getAvailabilityAt(final long time) {

        try {
            final long duration_nanos = exposed ? getSessionLengthInNanosAt(time) : getDowntimeInNanosAt(time);
            return new Availability(duration_nanos, exposed);
        }
        finally {
            switchExposure();
        }
    }

    protected abstract long getSessionLengthInNanosAt(final long time);

    protected abstract long getDowntimeInNanosAt(final long time);

    protected long generateRandomDurationFromDistribution(final ProbabilityDistribution distribution) {

        return RandomNumberGenerator.generate(distribution, uniform_random).longValue();
    }

    private void switchExposure() {

        exposed = !exposed;
    }
}
