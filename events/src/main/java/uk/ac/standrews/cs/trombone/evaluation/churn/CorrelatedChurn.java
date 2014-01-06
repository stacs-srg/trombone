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
import org.mashti.sina.util.RandomNumberGenerator;

public class CorrelatedChurn implements Churn {

    private final ProbabilityDistribution arrival_preference;
    private final ProbabilityDistribution session_lengths;
    private final ProbabilityDistribution downtimes;
    private final long bin_length;
    private final Random uniform_random;
    private long wait_duration;
    private long session_duration;
    private State next_state;

    public CorrelatedChurn(final long bin_length, final ProbabilityDistribution arrival_preference, final ProbabilityDistribution session_lengths, final ProbabilityDistribution downtimes, final long experiment_start_time_nanos, final long seed) {

        this(bin_length, arrival_preference, session_lengths, downtimes, seed);
        final long offset = calculateBinOffset(System.nanoTime() - experiment_start_time_nanos);
        wait_duration += offset;
    }

    public CorrelatedChurn(final long bin_length, final ProbabilityDistribution arrival_preference, final ProbabilityDistribution session_lengths, final ProbabilityDistribution downtimes, final long seed) {

        this.bin_length = bin_length;
        this.arrival_preference = arrival_preference;
        this.session_lengths = session_lengths;
        this.downtimes = downtimes;
        uniform_random = new Random(seed);
        next_state = State.WAIT;
    }

    public boolean isOnline() {

        return false; // never initially online, because the initial state is always WAIT
    }

    public synchronized long nextSessionLength() {

        final long next_session_length = RandomNumberGenerator.generate(session_lengths, uniform_random).longValue();
        session_duration = next_session_length;
        System.out.println("next session : " + session_duration);
        return next_session_length;
    }

    public synchronized long nextDowntime() {

        final long next_downtime;
        switch (next_state) {
            case WAIT:
                next_downtime = RandomNumberGenerator.generate(arrival_preference, uniform_random).longValue();
                wait_duration = next_downtime;
                next_state = State.ON;
                break;
            case ON:
                next_downtime = nextCorrelatedDowntime();
                next_state = State.REST;
                break;
            case REST:
                next_downtime = nextCorrelatedDowntime();
                next_state = State.ON;
                break;
            default:
                throw new IllegalStateException("unknown corelated churn state");
        }
        System.out.println("next downtime : " + next_downtime);
        return next_downtime;
    }

    @Override
    public Availability getAvailabilityAt(final long time) {

        // TODO Auto-generated method stub
        return null;
    }

    private long nextCorrelatedDowntime() {

        final int downtime_bin_count = nextDowntimeBinCount();
        final long elapsed = wait_duration + session_duration;
        final long offset = calculateBinOffset(elapsed);
        wait_duration = RandomNumberGenerator.generate(arrival_preference, uniform_random).longValue();
        return bin_length * downtime_bin_count + offset + wait_duration;

    }

    private int nextDowntimeBinCount() {

        final int downtime_bin_count = RandomNumberGenerator.generate(downtimes, uniform_random).intValue();
        if (downtime_bin_count > -1) { return downtime_bin_count; }
        throw new IllegalArgumentException("downtime bin count must be at least zero");
    }

    private long calculateBinOffset(final long elapsed) {

        final long passed_since_last_bin = elapsed % bin_length; // time passed since the start of the last bin
        return bin_length - passed_since_last_bin; //time until the next bin starts
    }

    private enum State {
        WAIT, ON, REST
    }
}
