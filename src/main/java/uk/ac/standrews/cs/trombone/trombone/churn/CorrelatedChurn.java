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
import java.util.concurrent.TimeUnit;
import uk.ac.standrews.cs.trombone.trombone.math.ProbabilityDistribution;
import uk.ac.standrews.cs.trombone.trombone.math.RandomNumberGenerator;
import uk.ac.standrews.cs.shabdiz.util.Duration;

public class CorrelatedChurn implements Churn {

    private enum State {
        WAIT, ON, REST
    }

    private final ProbabilityDistribution arrival_preference;
    private final ProbabilityDistribution session_lengths;
    private final ProbabilityDistribution downtimes;
    private final Duration bin_length;
    private final Random uniform_random;
    private Duration wait_duration;
    private Duration session_duration;
    private State next_state;

    public CorrelatedChurn(final Duration bin_length, final ProbabilityDistribution arrival_preference, final ProbabilityDistribution session_lengths, final ProbabilityDistribution downtimes, final long experiment_start_time_nanos, final long seed) {

        this(bin_length, arrival_preference, session_lengths, downtimes, seed);
        final Duration offset = calculateBinOffset(Duration.elapsedNano(experiment_start_time_nanos));
        wait_duration.add(offset);
    }

    public CorrelatedChurn(final Duration bin_length, final ProbabilityDistribution arrival_preference, final ProbabilityDistribution session_lengths, final ProbabilityDistribution downtimes, final long seed) {

        this.bin_length = bin_length.convertTo(TimeUnit.NANOSECONDS);
        this.arrival_preference = arrival_preference;
        this.session_lengths = session_lengths;
        this.downtimes = downtimes;
        uniform_random = new Random(seed);
        next_state = State.WAIT;
        wait_duration = new Duration();
        session_duration = new Duration();
    }

    public boolean isOnline() {

        return false; // never initially online, because the initial state is always WAIT
    }

    public synchronized Duration nextSessionLength() {

        final Duration next_session_length = RandomNumberGenerator.generateDurationInNanoseconds(session_lengths, uniform_random);
        session_duration = next_session_length;
        System.out.println("next session : " + session_duration);
        return next_session_length;
    }

    public synchronized Duration nextDowntime() {

        final Duration next_downtime;
        switch (next_state) {
            case WAIT:
                next_downtime = RandomNumberGenerator.generateDurationInNanoseconds(arrival_preference, uniform_random);
                wait_duration = next_downtime;
                next_state = State.ON;
                break;
            case ON:
                next_downtime = nextCorelatedDowntime();
                next_state = State.REST;
                break;
            case REST:
                next_downtime = nextCorelatedDowntime();
                next_state = State.ON;
                break;
            default:
                throw new IllegalStateException("unknown corelated churn state");
        }
        System.out.println("next downtime : " + next_downtime);
        return next_downtime;
    }

    private Duration nextCorelatedDowntime() {

        final int downtime_bin_count = nextDowntimeBinCount();
        final Duration elapsed = wait_duration.add(session_duration);
        final Duration offset = calculateBinOffset(elapsed);
        wait_duration = RandomNumberGenerator.generateDurationInNanoseconds(arrival_preference, uniform_random);
        return bin_length.times(downtime_bin_count).add(offset).add(wait_duration);

    }

    private int nextDowntimeBinCount() {

        final int downtime_bin_count = RandomNumberGenerator.generate(downtimes, uniform_random).intValue();
        if (downtime_bin_count > -1) { return downtime_bin_count; }
        throw new IllegalArgumentException("downtime bin count must be at least zero");
    }

    private Duration calculateBinOffset(final Duration elapsed) {

        final Duration passed_since_last_bin = elapsed.mod(bin_length); // time passed since the start of the last bin
        return bin_length.subtract(passed_since_last_bin); //time until the next bin starts
    }

    @Override
    public Availability getAvailabilityAt(final long time) {

        // TODO Auto-generated method stub
        return null;
    }
}
