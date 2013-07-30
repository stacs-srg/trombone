package uk.ac.standrews.cs.trombone.evaluation;

import org.mashti.jetson.exception.RPCException;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public interface ExperimentDeligate {

    void setInitialSeed() throws RPCException;

}
