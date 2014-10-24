package uk.ac.standrews.cs.trombone.evaluation.analysis.model;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class GaugeRecord<Value> extends Record {

    private Value value;

    public Value getValue() {

        return value;
    }

    public void setValue(final Value value) {

        this.value = value;
    }
}
