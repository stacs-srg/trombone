package uk.ac.standrews.cs.trombone.core.state;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerReference;
import uk.ac.standrews.cs.trombone.core.key.Key;
import uk.ac.standrews.cs.trombone.core.key.RingArithmetic;
import uk.ac.standrews.cs.trombone.core.util.RelativeRingDistanceComparator;

/**
 * Chord Finger table implementation using {@link ConcurrentSkipListMap}.
 *
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class ChordFingerTableMapBased implements ChordFingerTable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChordFingerTableMapBased.class);
    private final Peer local;
    private final Key local_key;
    private final int max_size;
    private final ConcurrentSkipListMap<Key, PeerReference> fingers;
    private final BigInteger base;
    private final List<Key> finger_targets;
    private final Collection<PeerReference> descending_fingers;

    /**
     * Constructs a new chord finger table.
     *
     * @param local the peer to which this finger table belongs
     * @param finger_table_size the maximum number of entries in the finger table
     * @param base the maximum ratio between two successive keys in the finger table
     */
    public ChordFingerTableMapBased(final Peer local, final int finger_table_size, final BigInteger base) {

        if (finger_table_size < 1) { throw new IllegalArgumentException("finger_table_size");}

        this.local = local;
        this.base = base;
        max_size = finger_table_size;
        local_key = local.key();
        fingers = new ConcurrentSkipListMap<>(new RelativeRingDistanceComparator(local_key));
        finger_targets = ChordFingerTableArrayBased.generateKeysLogarithmically(local_key, finger_table_size, base);
        descending_fingers = fingers.descendingMap()
                .values();
    }

    @Override
    public int size() {

        return fingers.size();
    }

    @Override
    public BigInteger getBase() {

        return base;
    }

    @Override
    public CompletableFuture<Boolean> fixNextFinger() {

        final Key target = nextTargetKey();
        return local.lookup(target)
                .thenApply(replacement -> {
                    final PeerReference replaced = replace(target, replacement);
                    return replaced == null || !replaced.equals(replacement);
                })
                .exceptionally(error -> {
                    LOGGER.debug("fix finger failed", error);
                    return false;
                });
    }

    @Override
    public int getMaxSize() {

        return max_size;
    }

    @Override
    public void clear() {

        fingers.clear();
    }

    @Override
    public List<PeerReference> getFingers() {

        return new CopyOnWriteArrayList<PeerReference>(fingers.values());
    }

    @Override
    public PeerReference closestPreceding(final Key target) {

        return descending_fingers.stream()
                .filter(finger -> {
                    final Key finger_key = finger.getKey();
                    return !finger_key.equals(local_key) && RingArithmetic.inRingOrder(local_key, finger_key, target);
                })
                .findFirst()
                .get();
    }

    @Override
    public PeerReference notifyFailure(final PeerReference broken_finger) {

        final Key broken_finger_key = broken_finger.getKey();
        return fingers.remove(broken_finger_key);
    }

    private Key nextTargetKey() {

        final Key next_finger_key = finger_targets.remove(0);
        finger_targets.add(next_finger_key);
        return next_finger_key;
    }

    PeerReference replace(final Key target, final PeerReference replacement) {

        return fingers.put(target, replacement);
    }

    PeerReference closestSuccessor(final Key target) {

        final Map.Entry<Key, PeerReference> closest_successor_entry = fingers.ceilingEntry(target);
        return closest_successor_entry != null ? closest_successor_entry.getValue() : null;
    }
}
