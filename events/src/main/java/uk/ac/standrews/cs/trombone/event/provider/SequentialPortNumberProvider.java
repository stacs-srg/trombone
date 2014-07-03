package uk.ac.standrews.cs.trombone.event.provider;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import uk.ac.standrews.cs.trombone.core.util.Copyable;
import uk.ac.standrews.cs.trombone.core.util.Named;
import uk.ac.standrews.cs.trombone.core.util.NamingUtils;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class SequentialPortNumberProvider implements Supplier<Integer>, Copyable, Named {

    private final int start;
    private final AtomicInteger next_port;

    public SequentialPortNumberProvider(int start) {

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

        final StringBuilder sb = new StringBuilder("SequentialPortNumberProvider{");
        sb.append("start=").append(start);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public SequentialPortNumberProvider copy() {

        return new SequentialPortNumberProvider(start);
    }

    @Override
    public String getName() {

        return NamingUtils.name(this);
    }
}
