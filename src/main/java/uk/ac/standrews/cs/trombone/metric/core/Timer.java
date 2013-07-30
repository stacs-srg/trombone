package uk.ac.standrews.cs.trombone.metric.core;

import java.util.concurrent.TimeUnit;
import uk.ac.standrews.cs.trombone.math.Statistics;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class Timer implements Metric {

    private final Sampler sampler;
    private final TimeUnit unit;

    public Timer() {

        this(TimeUnit.NANOSECONDS);
    }

    public Timer(TimeUnit unit) {

        this.unit = unit;
        sampler = new Sampler();
    }

    public void update(long length, TimeUnit unit) {

        sampler.update(normalizeUnit(length, unit));
    }

    public Statistics getAndReset() {

        return sampler.getAndReset();
    }

    protected Statistics get() {

        return sampler.get();
    }

    public Time time() {

        return new Time();
    }

    public TimeUnit getUnit() {

        return unit;
    }

    private long normalizeUnit(final long length, final TimeUnit unit) {

        return this.unit.convert(length, unit);
    }

    public class Time {

        private final long start_time;

        private Time() {

            start_time = System.nanoTime();
        }

        /**
         * Stops this timing and returns the elapsed time in nanoseconds.
         *
         * @return the elapsed time since the construction if this instance in nanoseconds.
         */
        public long stop() {

            final long elapsed = System.nanoTime() - start_time;
            update(elapsed, TimeUnit.NANOSECONDS);
            return elapsed;
        }
    }
}
