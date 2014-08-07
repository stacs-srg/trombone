package uk.ac.standrews.cs.trombone.core.state;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerReference;
import uk.ac.standrews.cs.trombone.core.Key;

/**
 * Chord Finger table implementation using array.
 *
 * @author Graham Kirby (graham.kirby@st-andrews.ac.uk)
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class ChordFingerTableArrayBased implements ChordFingerTable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChordFingerTableArrayBased.class);
    private final Peer local;
    private final Key local_key;
    private final PeerReference[] fingers;
    private final Key[] finger_targets;
    private final int number_of_fingers;
    private final BigInteger base;
    private volatile int next_finger_index;

    /**
     * Constructs a new finger table.
     *
     * @param local the peer to which this finger table belongs
     * @param finger_table_size the maximum number of entries in the finger table
     * @param base the maximum ratio between two successive keys in the finger table
     */
    public ChordFingerTableArrayBased(final Peer local, final int finger_table_size, BigInteger base) {

        this.local = local;
        this.base = base;
        local_key = local.key();

        number_of_fingers = finger_table_size;
        fingers = new PeerReference[number_of_fingers];
        finger_targets = generateKeysLogarithmically(local_key, number_of_fingers, base).toArray(new Key[number_of_fingers]);
    }

    @Override
    public void clear() {

        for (int i = 0; i < number_of_fingers; i++) {
            fingers[i] = null;
        }
    }

    @Override
    public int size() {

        int size = 0;

        for (int i = 0; i < number_of_fingers; i++) {
            if (fingers[i] != null) {
                size++;
            }
        }

        return size;
    }

    @Override
    public int getMaxSize() {

        return number_of_fingers;
    }

    @Override
    public BigInteger getBase() {

        return base;
    }

    @Override
    public CompletableFuture<Boolean> fixNextFinger() {

        next_finger_index--;
        if (next_finger_index < 0) {
            next_finger_index = number_of_fingers - 1;
        }

        return fixFinger(next_finger_index).exceptionally(error -> {
            LOGGER.debug("fix finger failed", error);
            return false;
        });
    }

    @Override
    public PeerReference closestPreceding(final Key target) {

        for (int i = number_of_fingers - 1; i >= 0; i--) {

            final PeerReference finger = fingers[i];

            // Finger may be null if it hasn't been fixed for the first time, or if its failure has been detected.
            // Looking for finger that lies before target from position of this node.
            // Ignore fingers pointing to this node.

            if (finger != null) {
                final Key finger_key = finger.getKey();
                if (!finger_key.equals(local_key) && Key.inRingOrder(local_key, finger_key, target)) { return finger; }
            }
        }

        throw new NoSuchElementException();
    }

    @Override
    public PeerReference notifyFailure(final PeerReference broken_finger) {

        for (int i = number_of_fingers - 1; i >= 0; i--) {

            final PeerReference finger = fingers[i];

            if (finger != null && finger.equals(broken_finger)) {
                fingers[i] = null;
            }
        }
        return broken_finger;
    }

    @Override
    public List<PeerReference> getFingers() {

        //        final List<PeerReference> references = new ArrayList<>();
        //        for (int i = 0; i < number_of_fingers; i++) {
        //            references.add(fingers[i]);
        //        }
        //
        //        return references;
        //
        return new CopyOnWriteArrayList<>(fingers);
    }

    /**
     * Generates a fixed number of logarithmically distributed keys.
     *
     * @param start_key the start key
     * @param number_of_keys the number of keys
     * @param base the logarithmic base
     * @return a fixed number of clustered keys
     */
    static List<Key> generateKeysLogarithmically(final Key start_key, final int number_of_keys, final BigInteger base) {

        final BigInteger start_value = start_key.getValue();
        final List<Key> keys = new ArrayList<>(number_of_keys);
        BigInteger offset = Key.KEYSPACE_SIZE;

        for (int i = number_of_keys - 1; i >= 0; i--) {
            offset = offset.divide(base);
            keys.add(new Key(start_value.add(offset)));
        }
        Collections.reverse(keys);
        return keys;
    }

    /**
     * Sets the correct finger for a given index in the finger table, by routing to the corresponding key.
     *
     * @param finger_index the index
     * @return true if a new finger was established
     */
    private CompletableFuture<Boolean> fixFinger(final int finger_index) {

        final Key target_key = finger_targets[finger_index];
        return local.lookup(target_key)
                .thenApply(new_finger -> {

                    assert new_finger != null;

                    final PeerReference old_finger = fingers[finger_index];
                    fingers[finger_index] = new_finger;
                    LOGGER.debug("finger table size {}", size());
                    return !new_finger.equals(old_finger);
                });
    }
}
