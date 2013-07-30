package uk.ac.standrews.cs.trombone.metric;

import io.netty.util.internal.chmv8.LongAdder;
import uk.ac.standrews.cs.trombone.metric.core.Counter;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class LookupCounter extends Counter {

    private static final LookupCounter GLOBAL_LOOKUP_COUNTER = new LookupCounter();
    LongAdder long_adder = new LongAdder();

    public LookupCounter() {

    }

    public void mark() {

        mark(1);
    }

    public void mark(long n) {

        long_adder.add(n);
        GLOBAL_LOOKUP_COUNTER.mark(n);
    }

    public long get() {

        return long_adder.sum();
    }

    public long getAndReset() {

        return long_adder.sumThenReset();
    }
}
