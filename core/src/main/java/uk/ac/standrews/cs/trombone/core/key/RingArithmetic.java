package uk.ac.standrews.cs.trombone.core.key;

/**
 * Ring arithmetic calculations.
 *
 * @author Graham Kirby (graham.kirby@st-andrews.ac.uk)
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public final class RingArithmetic {

    /** Prevent instantiation of utility class. */
    private RingArithmetic() {

    }

    /**
     * Tests whether three keys lie in ring order using {@link #ringDistanceFurther(Key, Key, Key)}.
     *
     * @param k1 the first key
     * @param k2 the second key
     * @param k3 the third key
     * @return true if, when traversing the key space in increasing key direction from k1, and allowing for wrap-around at zero, k3 is not encountered before k2
     */
    public static boolean inRingOrder(final Key k1, final Key k2, final Key k3) {

        // If the first and last keys are the same then the keys are in order regardless of the second.
        return k1.equals(k3) || !ringDistanceFurther(k1, k2, k3);
    }

    /**
     * Tests whether the second key is further from the first in ring distance than the third is from the first.
     *
     * @param k1 the first key
     * @param k2 the second key
     * @param k3 the third key
     * @return true if the ring distance from k1 to k2 is greater than the ring distance from k1 to k3
     */
    public static boolean ringDistanceFurther(final Key k1, final Key k2, final Key k3) {

        // If the first and last keys are the same then the keys are in order regardless of the second.
        return k1.compareRingDistance(k2, k3) > 0;
    }

    /**
     * Tests whether a key lies within a specified half-open ring segment using {@link #inRingOrder(Key, Key, Key)}.
     *
     * @param k1 the key after which the segment starts
     * @param k2 the key being tested
     * @param k3 the key at which the segment ends
     * @return true if k2 lies within the key segment starting after k1 and ending at k3
     */
    public static boolean inSegment(final Key k1, final Key k2, final Key k3) {

        // If k1 = k3 then the segment is the whole ring so it doesn't matter what k2 is.
        // Otherwise, if k1 = k2 then the target lies just before the segment.
        return k1.equals(k3) || !k1.equals(k2) && inRingOrder(k1, k2, k3);
    }
}
