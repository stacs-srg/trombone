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
package uk.ac.standrews.cs.trombone.trombone.measurement;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;

public class TimedActMeasurement<Result> {

    private volatile long start_time_in_nanos;
    private volatile long duration_in_nanos;
    private volatile Status status;
    private volatile Result result;
    private volatile Throwable error;

    protected TimedActMeasurement() {

        status = Status.INIT;
    }

    public synchronized void start() {

        checkStatus(Status.INIT);
        start_time_in_nanos = System.nanoTime();
        status = Status.STARTED;
    }

    public synchronized void stop(final Result result) {

        checkStatus(Status.STARTED);
        initElapsedDuration();
        setResult(result);
    }

    public synchronized void stop(final Throwable error) {

        checkStatus(Status.STARTED);
        initElapsedDuration();
        setError(error);
    }

    public Throwable getError() {

        return error;
    }

    public Result getResult() {

        return result;
    }

    public Long getDutationInNanos() {

        return duration_in_nanos;
    }

    public synchronized boolean isDone() {

        return status.equals(Status.ENDED_IN_RESULT) || status.equals(Status.ENDED_IN_ERROR);
    }

    public synchronized Status getStatus() {

        return status;
    }

    protected void checkStatus(final Status expected_status) {

        if (!status.equals(expected_status)) { throw new IllegalStateException("expected " + expected_status + ", current status " + status); }
    }

    private void initElapsedDuration() {

        duration_in_nanos = System.nanoTime() - start_time_in_nanos;
    }

    private void setResult(final Result result) {

        assert status == Status.STARTED;
        this.result = result;
        status = Status.ENDED_IN_RESULT;
    }

    private void setError(final Throwable error) {

        assert status == Status.STARTED;
        this.error = error;
        status = Status.ENDED_IN_ERROR;
    }

    public enum Status {
        INIT, STARTED, ENDED_IN_ERROR, ENDED_IN_RESULT
    }
}
