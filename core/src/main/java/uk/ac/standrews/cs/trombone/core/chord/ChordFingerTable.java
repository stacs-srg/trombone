package uk.ac.standrews.cs.trombone.core.chord;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerReference;
import uk.ac.standrews.cs.trombone.core.key.Key;
import uk.ac.standrews.cs.trombone.core.util.RelativeRingDistanceComparator;

/**
 * Chord Finger table implementation.
 *
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class ChordFingerTable {

    private final Key local_key;
    //    private final PeerReference[] fingers;
    private final int max_size; // The maximum size of finger table

    private final ConcurrentSkipListMap<Key, PeerReference> fingers;
    private final BigInteger base;

    /**
     * Instantiates a new chord finger table.
     *
     * @param local_node the reference to the node that will own the finger table
     * @param finger_table_size the maximum number of entries in the finger table
     * @param base the maximum ratio between two successive keys in the finger table
     */
    ChordFingerTable(final Peer local_node, final int finger_table_size, final BigInteger base) {

        max_size = finger_table_size;
        this.base = base;
        local_key = local_node.getKeySync();
        fingers = new ConcurrentSkipListMap<>(new RelativeRingDistanceComparator(local_key));
    }

    /**
     * Gets number of fingers that are not null in this finger table.
     *
     * @return the number of non-null fingers
     */
    public Integer getFingerCount() {

        return fingers.size();
    }

    /**
     * Gets the inter-finger ratio.
     *
     * @return the inter-finger ratio
     */
    public BigInteger getBase() {

        return base;
    }

    /**
     * Gets maximum finger table size.
     *
     * @return the maximum finger table size
     */
    public int getMaxSize() {

        return max_size;
    }

    void clear() {

        fingers.clear();
    }

    PeerReference replaceClosestSuccessorWith(final Key target, final PeerReference replacement) {

        final PeerReference closest_successor = closestSuccessor(target);
        final Key replacement_key = replacement.getKey();
        if (closest_successor != null) {
            fingers.remove(closest_successor.getKey());
        }

        fingers.put(replacement_key, replacement);

        return closest_successor;
    }

    PeerReference notifyFingerFailure(final PeerReference broken_finger) {

        final Key broken_finger_key = broken_finger.getKey();
        return fingers.remove(broken_finger_key) ;
    }

    List<PeerReference> getFingers() {

        return new CopyOnWriteArrayList<PeerReference>(fingers.values());
    }

    PeerReference closestPreceding(final Key target) {

        final Map.Entry<Key, PeerReference> floor_finger = fingers.floorEntry(target);
        if (floor_finger != null) {
            return floor_finger.getValue();
        }

        throw new RuntimeException("cannot determine closest");
    }

    PeerReference closestSuccessor(final Key target) {

        final Map.Entry<Key, PeerReference> closest_successor_entry = fingers.ceilingEntry(target);
        return closest_successor_entry != null ? closest_successor_entry.getValue() : null;
    }
}
