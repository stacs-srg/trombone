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

package uk.ac.standrews.cs.trombone.event.environment;

import java.util.concurrent.TimeUnit;
import org.uncommons.maths.random.Probability;
import uk.ac.standrews.cs.shabdiz.util.Duration;
import uk.ac.standrews.cs.trombone.core.util.Copyable;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class Churn implements Copyable {

    public static final Churn NONE = new Churn(new ConstantIntervalGenerator(Duration.MAX_DURATION), new ConstantIntervalGenerator(new Duration(0, TimeUnit.SECONDS)));

    private final IntervalGenerator session_lengths;
    private final IntervalGenerator downtimes;

    public Churn(final IntervalGenerator session_lengths, final IntervalGenerator downtimes) {

        this.session_lengths = session_lengths;
        this.downtimes = downtimes;
    }

    public IntervalGenerator getSessionLength() {

        return session_lengths;
    }

    public IntervalGenerator getDowntime() {

        return downtimes;
    }

    public Probability getAvailabilityAt(final long time_nanos) {

        checkPositiveTime(time_nanos);

        final double mean_session_length = session_lengths.getMeanAt(time_nanos);
        final double mean_downtime = downtimes.getMeanAt(time_nanos);
        final double sum_of_mean_downtime_and_session_length = mean_downtime + mean_session_length;

        return new Probability(sum_of_mean_downtime_and_session_length != 0 ? mean_session_length / sum_of_mean_downtime_and_session_length : 1);
    }

    public long getSessionLengthAt(final long time_nanos) {

        checkPositiveTime(time_nanos);
        return session_lengths.get(time_nanos);
    }

    public long getDowntimeAt(final long time_nanos) {

        checkPositiveTime(time_nanos);
        return downtimes.get(time_nanos);
    }

    private static void checkPositiveTime(final long time_nanos) {

        if (time_nanos < 0) {
            throw new IllegalArgumentException("time through experiment cannot be negative: " + time_nanos);
        }
    }

    @Override
    public Churn copy() {

        return new Churn(session_lengths.copy(), downtimes.copy());
    }

    @Override
    public String toString() {

        final StringBuilder sb = new StringBuilder("Churn{");
        sb.append("session_lengths=").append(session_lengths);
        sb.append(", downtimes=").append(downtimes);
        sb.append('}');
        return sb.toString();
    }
}
