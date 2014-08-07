package uk.ac.standrews.cs.trombone.event.environment;

import java.util.concurrent.TimeUnit;
import uk.ac.standrews.cs.shabdiz.util.Duration;

/**
 * The type Constant interval generator.
 *
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class ConstantIntervalGenerator implements IntervalGenerator {

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
}
