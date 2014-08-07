package uk.ac.standrews.cs.trombone.core.state;

import java.math.BigInteger;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import uk.ac.standrews.cs.trombone.core.PeerReference;
import uk.ac.standrews.cs.trombone.core.Key;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public interface ChordFingerTable {

    /**
     * Removes all fingers.
     */
    void clear();

    /**
     * Gets number of fingers that are not null in this finger table.
     *
     * @return the number of non-null fingers
     */
    int size();

    /**
     * Gets maximum finger table size.
     *
     * @return the maximum finger table size
     */
    int getMaxSize();

    /**
     * Gets the inter-finger ratio.
     *
     * @return the inter-finger ratio
     */
    BigInteger getBase();

    /**
     * Asynchronously fixes the next finger in the finger table.
     *
     * @return result of an asynchronous computation that returns {@code true} if a finger has changed.
     */
    CompletableFuture<Boolean> fixNextFinger();

    /**
     * Returns the finger that extends the furthest round the ring from this node without passing the given key.
     *
     * @param target the target key
     * @return the closest preceding finger to the key
     * @throws NoSuchElementException if no such finger exists
     */
    PeerReference closestPreceding(Key target);

    /**
     * Returns the contents of the finger table as a list.
     *
     * @return the contents of the finger table as a list
     */
    List<PeerReference> getFingers();

    /**
     * Notifies the finger table of a broken finger.
     *
     * @param broken_finger the finger that is suspected to have failed
     */
    PeerReference notifyFailure(PeerReference broken_finger);

    /**
     * Returns the truncated log of an integer to a given base.
     *
     * @param n an integer
     * @param base the required base
     * @return the log to the given base
     */
    static int log(int n, int base) {

        return (int) (Math.log10(n) / Math.log10(base));
    }
}
