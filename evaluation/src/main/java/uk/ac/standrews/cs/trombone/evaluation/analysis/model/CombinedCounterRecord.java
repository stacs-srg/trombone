package uk.ac.standrews.cs.trombone.evaluation.analysis.model;

import java.util.Optional;
import org.apache.commons.math3.stat.interval.ConfidenceInterval;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class CombinedCounterRecord extends Record {

    private double count;
    private Optional<ConfidenceInterval> confidenceInterval;

    public double getMeanCount() {

        return count;
    }

    public void setMeanCount(final double count) {

        this.count = count;
    }

    public Optional<ConfidenceInterval> getCountConfidenceInterval() {

        return confidenceInterval;
    }

    public void setCountConfidenceInterval(final Optional<ConfidenceInterval> confidenceInterval) {

        this.confidenceInterval = confidenceInterval;
    }
}
