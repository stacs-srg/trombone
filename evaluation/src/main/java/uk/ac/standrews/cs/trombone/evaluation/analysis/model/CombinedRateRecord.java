package uk.ac.standrews.cs.trombone.evaluation.analysis.model;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.apache.commons.math3.stat.interval.ConfidenceInterval;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class CombinedRateRecord extends CombinedCounterRecord {

    private TimeUnit unit;
    private double meanRate;
    private Optional<ConfidenceInterval> rateConfidenceInterval;

    public double getMeanRate() {

        return meanRate;
    }

    public void setMeanRate(final double meanRate) {

        this.meanRate = meanRate;
    }

    public TimeUnit getUnit() {

        return unit;
    }

    public void setUnit(final TimeUnit unit) {

        this.unit = unit;
    }

    public Optional<ConfidenceInterval> getRateConfidenceInterval() {

        return rateConfidenceInterval;
    }

    public void setRateConfidenceInterval(final Optional<ConfidenceInterval> rateConfidenceInterval) {

        this.rateConfidenceInterval = rateConfidenceInterval;
    }
}
