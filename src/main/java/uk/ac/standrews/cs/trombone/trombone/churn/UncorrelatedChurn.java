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
package uk.ac.standrews.cs.trombone.trombone.churn;

import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;
import uk.ac.standrews.cs.trombone.trombone.math.ProbabilityDistribution;
import uk.ac.standrews.cs.trombone.trombone.math.RandomNumberGenerator;
import uk.ac.standrews.cs.shabdiz.util.Duration;

public abstract class UncorrelatedChurn implements Churn {

    private final Duration first_arrival_delay;
    private final ReentrantLock cycle_lock;
    private final Random uniform_random;
    private boolean exposed;
    private boolean first_arrival;

    protected UncorrelatedChurn(final Duration first_arrival_delay, final long seed) {

        this.first_arrival_delay = first_arrival_delay;
        cycle_lock = new ReentrantLock();
        uniform_random = new Random(seed);
        first_arrival = true;
    }

    @Override
    public final Availability getAvailabilityAt(final long time) {

        cycle_lock.lock();
        try {
            final Duration duration = exposed ? getSessionLengthAt(time) : first_arrival ? getfirstArrivalDelay() : getDowntimeAt(time);
            return new Availability(duration, exposed);
        }
        finally {
            switchExposure();
            cycle_lock.unlock();
        }
    }

    protected abstract Duration getSessionLengthAt(final long time);

    protected abstract Duration getDowntimeAt(final long time);

    private Duration getfirstArrivalDelay() {

        assert first_arrival;
        first_arrival = false;
        return first_arrival_delay;
    }

    protected Duration generateRandomDurationFromDistribution(final ProbabilityDistribution distribution) {

        return RandomNumberGenerator.generateDurationInNanoseconds(distribution, uniform_random);
    }

    private void switchExposure() {

        exposed = !exposed;
    }
}
