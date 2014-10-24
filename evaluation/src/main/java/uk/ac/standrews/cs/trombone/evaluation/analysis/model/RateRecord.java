package uk.ac.standrews.cs.trombone.evaluation.analysis.model;

import java.util.concurrent.TimeUnit;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class RateRecord extends CounterRecord {

    private double rate;
    private TimeUnit unit = TimeUnit.SECONDS;

    public double getRate() {

        return rate;
    }

    public void setRate(final double rate) {

        this.rate = rate;
    }

    public TimeUnit getUnit() {

        return unit;
    }

    public void setUnit(final TimeUnit unit) {

        this.unit = unit;
    }
}
