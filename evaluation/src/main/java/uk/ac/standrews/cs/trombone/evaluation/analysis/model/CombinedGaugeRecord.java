package uk.ac.standrews.cs.trombone.evaluation.analysis.model;

import java.util.Optional;
import org.apache.commons.math3.stat.interval.ConfidenceInterval;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class CombinedGaugeRecord<Value> extends Record {

    private Value value;
    private Optional<ConfidenceInterval> confidenceInterval;

    public Value getMeanValue() {

        return value;
    }

    public void setMeanValue(final Value value) {

        this.value = value;
    }

    public Optional<ConfidenceInterval> getValueConfidenceInterval() {

        return confidenceInterval;
    }

    public void setValueConfidenceInterval(final Optional<ConfidenceInterval> confidenceInterval) {

        this.confidenceInterval = confidenceInterval;
    }

    public String getCSVHeader() {

        return "time,value_mean,value_ci_lower,value_ci_upper";
    }


}
