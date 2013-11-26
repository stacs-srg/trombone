package uk.ac.standrews.cs.trombone.evaluation.provider;

import java.io.Serializable;
import javax.inject.Provider;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public interface SerializableProvider<T> extends Provider<T>, Serializable {
}
