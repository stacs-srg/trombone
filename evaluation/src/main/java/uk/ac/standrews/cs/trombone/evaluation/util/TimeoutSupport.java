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

package uk.ac.standrews.cs.trombone.evaluation.util;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import org.mashti.sina.util.NumericalRangeValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeoutSupport implements Timeoutable, Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimeoutSupport.class);
    private static final long serialVersionUID = 8490654599271676242L;
    private volatile long timeout_in_nanos;
    private volatile long start_time_in_nanos;

    public TimeoutSupport() {

        this(Long.MAX_VALUE, TimeUnit.DAYS);
    }

    public TimeoutSupport(final long timeout, TimeUnit timeout_unit) {

        setTimeout(timeout, timeout_unit);
    }

    public synchronized boolean startCountdown() {

        if (!isCountdownStarted()) {
            start_time_in_nanos = System.nanoTime();
            return true;
        }
        else {
            LOGGER.warn("unable to start countdown since it is already started");
            return false;
        }
    }

    public synchronized boolean isCountdownStarted() {

        return start_time_in_nanos > 0;
    }

    public boolean isTimedOut() {

        return getRemainingTimeInNanos() <= 0;
    }

    @Override
    public void setTimeout(final long timeout, TimeUnit timeout_unit) {

        if (!isCountdownStarted()) {
            NumericalRangeValidator.validateRangeLargerThanZeroExclusive(timeout);
            timeout_in_nanos = TimeUnit.NANOSECONDS.convert(timeout, timeout_unit);
        }
        else {
            LOGGER.warn("unable to set timeout since the countdown is already started");
        }
    }

    public long getTimeoutInNanos() {

        return timeout_in_nanos;
    }

    public void awaitTimeout() throws InterruptedException {

        Thread.sleep(getRemainingTime(TimeUnit.MILLISECONDS));
    }

    public long getRemainingTime(TimeUnit unit) {

        return unit.convert(getElapsedTimeInNanos(), TimeUnit.NANOSECONDS);

    }

    public long getRemainingTimeInNanos() {

        return timeout_in_nanos - getElapsedTimeInNanos();
    }

    public long getElapsedTimeInNanos() {

        return isCountdownStarted() ? System.nanoTime() - start_time_in_nanos : 0;
    }

    public boolean exceedsRemainingTimeInNanos(final long duration_in_nanos) {

        return getRemainingTimeInNanos() <= duration_in_nanos;
    }

    public long getMaxRemainingTimeInNanos() {

        return Math.max(getRemainingTimeInNanos(), 0);
    }
}
