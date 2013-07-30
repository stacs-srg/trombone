package uk.ac.standrews.cs.trombone.metric.core;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public interface Gauge<Value extends Number> extends Metric {

    Value get();

}
