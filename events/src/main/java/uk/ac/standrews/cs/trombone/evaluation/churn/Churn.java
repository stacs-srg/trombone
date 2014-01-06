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

public interface Churn {

    Churn NONE = new Churn() {

        @Override
        public Availability getAvailabilityAt(final long time) {

            return Availability.MAX_AVAILABILITY;
        }
    };

    Availability getAvailabilityAt(long time_nanos);

    final class Availability {

        public static final Availability MAX_AVAILABILITY = new Availability(Long.MAX_VALUE, true);
        private final boolean available;
        private final long duration_nanos;

        public Availability(final long duration_nanos, final boolean available) {

            this.duration_nanos = duration_nanos;
            this.available = available;
        }

        public boolean isAvailable() {

            return available;
        }

        public long getDurationInNanos() {

            return duration_nanos;
        }
    }
}
