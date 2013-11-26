package uk.ac.standrews.cs.trombone.core.key;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public interface KeyDistribution {

    Key[] generate(int count);
}
