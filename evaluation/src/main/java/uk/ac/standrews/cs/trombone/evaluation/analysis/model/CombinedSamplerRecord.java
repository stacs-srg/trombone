package uk.ac.standrews.cs.trombone.evaluation.analysis.model;

import java.util.Optional;
import org.apache.commons.math3.stat.interval.ConfidenceInterval;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class CombinedSamplerRecord extends CombinedCounterRecord {

    private double minOfMins;
    private double meanOfMeans;
    private double overallMean;
    private double maxOfMaxes;
    private double overallStandardDeviation;
    private Optional<ConfidenceInterval> overallConfidenceInterval;
    private Optional<ConfidenceInterval> meanConfidenceInterval;

    public double getMinOfMins() {

        return minOfMins;
    }

    public void setMinOfMins(final double minOfMins) {

        this.minOfMins = minOfMins;
    }

    public double getMeanOfMeans() {

        return meanOfMeans;
    }

    public void setMeanOfMeans(final double meanOfMeans) {

        this.meanOfMeans = meanOfMeans;
    }

    public double getMaxOfMaxes() {

        return maxOfMaxes;
    }

    public void setMaxOfMaxes(final double maxOfMaxes) {

        this.maxOfMaxes = maxOfMaxes;
    }

    public double getOverallStandardDeviation() {

        return overallStandardDeviation;
    }

    public void setOverallStandardDeviation(final double overallStandardDeviation) {

        this.overallStandardDeviation = overallStandardDeviation;
    }

    public Optional<ConfidenceInterval> getOverallConfidenceInterval() {

        return overallConfidenceInterval;
    }

    public void setOverallConfidenceInterval(final Optional<ConfidenceInterval> overallConfidenceInterval) {

        this.overallConfidenceInterval = overallConfidenceInterval;
    }

    public Optional<ConfidenceInterval> getMeanConfidenceInterval() {

        return meanConfidenceInterval;
    }

    public void setMeanConfidenceInterval(final Optional<ConfidenceInterval> meanConfidenceInterval) {

        this.meanConfidenceInterval = meanConfidenceInterval;
    }

    public double getOverallMean() {

        return overallMean;
    }

    public void setOverallMean(final double overallMean) {

        this.overallMean = overallMean;
    }
}
