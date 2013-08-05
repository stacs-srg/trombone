package uk.ac.standrews.cs.trombone.metric.core;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class Rate implements Metric {

    private final Counter counter;
    private final AtomicLong start_time_nanos;
    private final TimeUnit unit;

    public Rate() {

        this(TimeUnit.SECONDS);
    }

    public Rate(TimeUnit unit) {

        this.unit = unit;
        counter = new Counter();
        start_time_nanos = new AtomicLong(System.nanoTime());
    }

    public void mark() {

        mark(1);
    }

    public void mark(long n) {

        counter.add(n);
    }

    public double getRate() {

        return getRate(false);
    }

    public double getRateAndReset() {

        return getRate(true);
    }

    public long getCount() {

        return counter.get();
    }

    public TimeUnit getUnit() {

        return unit;
    }

    private double getRate(boolean reset) {

        final double count = reset ? counter.getAndReset() : getCount();
        if (count == 0) { return 0; }

        final long now = System.nanoTime();
        final long elapsed = now - (reset ? start_time_nanos.getAndSet(now) : start_time_nanos.get());
        final long time = unit.convert(elapsed, TimeUnit.NANOSECONDS);
        return count / (time < 1 ? 1 : time);
    }
}
