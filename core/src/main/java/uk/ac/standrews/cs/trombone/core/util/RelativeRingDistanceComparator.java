package uk.ac.standrews.cs.trombone.core.util;

import java.io.Serializable;
import java.util.Comparator;
import uk.ac.standrews.cs.trombone.core.key.Key;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class RelativeRingDistanceComparator implements Comparator<Key>, Serializable {

    private static final long serialVersionUID = -1051743700684807791L;
    private final Key start;

    public RelativeRingDistanceComparator(final Key start) {

        this.start = start;
    }

    @Override
    public int compare(final Key first, final Key second) {

        return start.compareRingDistance(first, second);
    }
}
