package uk.ac.standrews.cs.trombone.evaluation.analysis.model;

import java.util.concurrent.TimeUnit;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class TimerRecord extends SamplerRecord {

    private TimeUnit unit;

    public TimeUnit getUnit() {

        return unit;
    }

    public void setUnit(final TimeUnit unit) {

        this.unit = unit;
    }
}
