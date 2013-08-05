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
package uk.ac.standrews.cs.trombone.workload;

import uk.ac.standrews.cs.shabdiz.util.Duration;
import uk.ac.standrews.cs.trombone.key.Key;

public interface Workload {

    Lookup getLookupAt(long time);

    public class Lookup {

        private final int retry_threshold;
        private final Key target;
        private final Duration interval;

        Lookup(final Duration interval, final Key target, final int retry_threshold) {

            this.interval = interval;
            this.target = target;
            this.retry_threshold = retry_threshold;
        }

        public Duration getInterval() {

            return interval;
        }

        public Key getTarget() {

            return target;
        }

        public int getRetryThreshold() {

            return retry_threshold;
        }
    }
}
