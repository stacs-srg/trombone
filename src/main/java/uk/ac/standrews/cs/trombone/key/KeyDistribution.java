package uk.ac.standrews.cs.trombone.key;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public interface KeyDistribution {

    Key[] generate(int count);
}
