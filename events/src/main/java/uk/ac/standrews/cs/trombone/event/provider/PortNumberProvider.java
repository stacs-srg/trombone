package uk.ac.standrews.cs.trombone.event.provider;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Provider;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class PortNumberProvider implements Provider<Integer>, Serializable {

    private static final long serialVersionUID = -7586002873803828004L;
    private final int start;
    private final AtomicInteger next_port;

    public PortNumberProvider(int start) {

        if (start < 0 || start > 0xffff) { throw new IllegalArgumentException("invalid start port"); }
        this.start = start;
        next_port = new AtomicInteger(start);
    }

    @Override
    public Integer get() {

        return next_port.getAndIncrement();
    }

    public PortNumberProvider copy() {

        return new PortNumberProvider(start);
    }
}
