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
package uk.ac.standrews.cs.trombone.churn;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.mashti.sina.distribution.ProbabilityDistribution;
import org.mashti.sina.util.NumericalRangeValidator;
import uk.ac.standrews.cs.shabdiz.util.Duration;
import uk.ac.standrews.cs.trombone.util.DurationUtil;

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
            final Duration duration = exposed ? getSessionLengthAt(time) : getDowntimeAt(time);
            return new Availability(duration.getLength(TimeUnit.NANOSECONDS), exposed);
        }
        finally {
            switchExposure();
        }
    }

    protected abstract Duration getSessionLengthAt(final long time);

    protected abstract Duration getDowntimeAt(final long time);

    protected Duration generateRandomDurationFromDistribution(final ProbabilityDistribution distribution) {

        return DurationUtil.generateDurationInNanoseconds(distribution, uniform_random);
    }

    private void switchExposure() {

        exposed = !exposed;
    }
}
