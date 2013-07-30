package uk.ac.standrews.cs.trombone.key;

import java.io.Serializable;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public interface Key<Value extends Number> extends Comparable<Key<Value>>, Serializable {

    Value getValue();

    Number getRingDistanceTo(Key<Value> other);

    Number getKeySpaceSize();

    int compareRingDistance(Key<Value> first, Key<Value> second);
}
