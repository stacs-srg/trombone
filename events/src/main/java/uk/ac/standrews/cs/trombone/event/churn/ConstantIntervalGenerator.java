package uk.ac.standrews.cs.trombone.event.churn;

import java.util.concurrent.TimeUnit;
import uk.ac.standrews.cs.shabdiz.util.Duration;
import uk.ac.standrews.cs.trombone.core.util.Named;
import uk.ac.standrews.cs.trombone.core.util.NamingUtils;

/**
 * The type Constant interval generator.
 *
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class ConstantIntervalGenerator implements IntervalGenerator, Named {

    private final long constant_interval_nanos;
    private final Duration constant_interval;

    /**
     * Instantiates a new constant interval generator.
     *
     * @param constant_interval the constant interval
     */
    public ConstantIntervalGenerator(Duration constant_interval) {

        this.constant_interval = constant_interval;
        constant_interval_nanos = constant_interval.getLength(TimeUnit.NANOSECONDS);
    }

    public Duration getConstantInterval() {

        return constant_interval;
    }

    @Override
    public long get(final long time_nanos) {

        return constant_interval_nanos;
    }

    @Override
    public long getMeanAt(final long time_nanos) {

        return constant_interval_nanos;
    }

    @Override
    public ConstantIntervalGenerator copy() {

        return new ConstantIntervalGenerator(constant_interval);
    }

    @Override
    public String toString() {

        final StringBuilder sb = new StringBuilder("ConstantIntervalGenerator{");
        sb.append("constant_interval=").append(constant_interval_nanos);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public String getName() {

        return NamingUtils.name(this);
    }
}
