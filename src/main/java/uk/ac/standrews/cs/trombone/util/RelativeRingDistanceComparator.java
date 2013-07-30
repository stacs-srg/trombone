package uk.ac.standrews.cs.trombone.util;

import java.util.Comparator;
import uk.ac.standrews.cs.trombone.key.Key;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class RelativeRingDistanceComparator implements Comparator<Key> {

    private final Key key;

    public RelativeRingDistanceComparator(Key key) {

        this.key = key;
    }

    @Override
    public int compare(final Key k1, final Key k2) {

        return key.compareRingDistance(k1, k2);
    }
}
