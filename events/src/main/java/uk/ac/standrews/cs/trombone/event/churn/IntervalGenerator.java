package uk.ac.standrews.cs.trombone.event.churn;

import uk.ac.standrews.cs.trombone.core.util.Copyable;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public interface IntervalGenerator extends Copyable {

    long get(long time_nanos);

    long getMeanAt(final long time_nanos);

    @Override
    IntervalGenerator copy();
}