package uk.ac.standrews.cs.trombone.metric.core;

import io.netty.util.internal.chmv8.LongAdder;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class Counter implements Metric {

    LongAdder long_adder = new LongAdder();

    public Counter() {

    }

    public void increment() {

        add(1);
    }

    public void decrement() {

        subtract(1);
    }

    public void subtract(long n) {

        add(-1 * n);
    }

    public void add(long n) {

        long_adder.add(n);
    }

    public long get() {

        return long_adder.sum();
    }

    public long getAndReset() {

        return long_adder.sumThenReset();
    }
}
