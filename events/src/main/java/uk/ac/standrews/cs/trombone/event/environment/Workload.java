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

import uk.ac.standrews.cs.shabdiz.util.Duration;
import uk.ac.standrews.cs.trombone.core.key.Key;
import uk.ac.standrews.cs.trombone.core.key.KeyProvider;
import uk.ac.standrews.cs.trombone.core.util.Copyable;

/**
 * Presents a synthetic pattern of a peer workload.
 *
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class Workload implements Copyable {

    /** No workload. */
    public static final Workload NONE = new Workload(null, new ConstantIntervalGenerator(Duration.MAX_DURATION));

    private final KeyProvider key_provider;
    private final IntervalGenerator intervals;

    /**
     * Instantiates a new workload pattern.
     *
     * @param key_provider the provider of target lookup keys
     * @param intervals the intervals between successive lookups
     */
    public Workload(final KeyProvider key_provider, final IntervalGenerator intervals) {

        this.key_provider = key_provider;
        this.intervals = intervals;
    }

    /**
     * Gets the provider of target lookup keys.
     *
     * @return the provider of target lookup keys
     */
    public KeyProvider getKeyProvider() {

        return key_provider;
    }

    /**
     * Gets the intervals between successive lookups.
     *
     * @return the intervals between successive lookups
     */
    public IntervalGenerator getIntervals() {

        return intervals;
    }

    /**
     * Gets the interval between the given time through the experiment and the occurrence of the next lookup.
     *
     * @param time_nanos the time through experiment in {@code nanoseconds}
     * @return the amount of time  to wait in {@code nanoseconds} before executing the next lookup
     */
    public long getIntervalAt(final long time_nanos) {

        return intervals.get(time_nanos);
    }

    /**
     * Gets target key to be looked up at the give time through the experiment.
     *
     * @param time_nanos the time through experiment in {@code nanoseconds}
     * @return the key to be looked up
     */
    public Key getTargetKeyAt(final long time_nanos) {

        return key_provider == null ? null : key_provider.get();
    }

    @Override
    public Workload copy() {

        return new Workload(key_provider == null ? null : key_provider.copy(), intervals.copy());
    }

    @Override
    public String toString() {

        final StringBuilder sb = new StringBuilder("Workload{");
        sb.append("key_provider=").append(key_provider);
        sb.append(", intervals=").append(intervals);
        sb.append('}');
        return sb.toString();
    }
}
