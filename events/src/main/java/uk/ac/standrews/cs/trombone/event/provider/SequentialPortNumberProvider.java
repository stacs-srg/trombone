package uk.ac.standrews.cs.trombone.event.provider;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Provider;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class SequentialPortNumberProvider implements Provider<Integer>, Serializable, Cloneable {

    private static final long serialVersionUID = -7586002873803828004L;
    private final int start;
    private final AtomicInteger next_port;

    public SequentialPortNumberProvider(int start) {

        if (start < 0 || start > 0xffff) { throw new IllegalArgumentException("invalid start port"); }
        this.start = start;
        next_port = new AtomicInteger(start);
    }

    @Override
    public Integer get() {

        return next_port.getAndIncrement();
    }

    @Override
    public String toString() {

        final StringBuilder sb = new StringBuilder("SequentialPortNumberProvider{");
        sb.append("start=").append(start);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public SequentialPortNumberProvider clone() {

        return new SequentialPortNumberProvider(start);
    }
}
