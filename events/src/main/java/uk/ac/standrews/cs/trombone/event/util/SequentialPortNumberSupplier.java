package uk.ac.standrews.cs.trombone.event.util;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import uk.ac.standrews.cs.trombone.core.util.Copyable;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class SequentialPortNumberSupplier implements Supplier<Integer>, Copyable{

    private final int start;
    private final AtomicInteger next_port;

    public SequentialPortNumberSupplier(int start) {

        if (start < 0 || start > 0xffff) { throw new IllegalArgumentException("invalid start port"); }
        this.start = start;
        next_port = new AtomicInteger(start);
    }

    public int getStart() {

        return start;
    }

    @Override
    public Integer get() {

        return next_port.getAndIncrement();
    }

    @Override
    public String toString() {

        return "SequentialPortNumberProvider{" + "start=" + start + '}';
    }

    @Override
    public SequentialPortNumberSupplier copy() {

        return new SequentialPortNumberSupplier(start);
    }

}
