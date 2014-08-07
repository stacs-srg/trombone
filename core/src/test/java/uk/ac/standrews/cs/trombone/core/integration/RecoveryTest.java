package uk.ac.standrews.cs.trombone.core.integration;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.shabdiz.ApplicationDescriptor;
import uk.ac.standrews.cs.shabdiz.ApplicationState;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerFactory;
import uk.ac.standrews.cs.trombone.core.PeerReference;
import uk.ac.standrews.cs.trombone.core.Key;
import uk.ac.standrews.cs.trombone.core.selector.ChordPredecessorSelector;
import uk.ac.standrews.cs.trombone.core.selector.ChordSuccessorSelector;
import uk.ac.standrews.cs.trombone.core.state.ChordFingerTable;
import uk.ac.standrews.cs.trombone.core.state.ChordPeerState;
import uk.ac.standrews.cs.trombone.core.state.ChordSuccessorList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RecoveryTest {

    private static final double KILL_PORTION = 0.5;
    private static final Logger LOGGER = LoggerFactory.getLogger(RecoveryTest.class);
    public static final ChordPredecessorSelector CHORD_PREDECESSOR_SELECTOR = ChordPredecessorSelector.getInstance();
    public static final ChordSuccessorSelector CHORD_SUCCESSOR_SELECTOR = ChordSuccessorSelector.getInstance();
    private static P2PNetwork network;

    @BeforeClass
    public static void setUp() throws Exception {

        network = new SingleProcessLocalP2PNetwork(30);
        network.populate();
        network.deployAll();
        network.awaitAnyOfStates(ApplicationState.RUNNING);
    }

    @Test
    public void _1stTestSelfLookup() throws Exception {

        for (ApplicationDescriptor descriptor : network) {
            final PeerReference reference = descriptor.getApplicationReference();
            final PeerReference lookup_result = PeerFactory.bind(reference)
                    .lookup(reference.getKey())
                    .get();
            assertEquals(reference, lookup_result);
            LOGGER.info("OK {}", reference);
        }
    }

    @Test
    public void _2ndTestStabilization() throws Exception {

        final int network_size = network.size();
        LOGGER.info("awaiting stabilized ring of size {}", network_size);
        awaitRingSize(network_size);
    }

    @Test
    public void _3thTestLookupCorrectness() throws Exception {

        CompletableFuture.runAsync(() -> {
            while (!routingCorrect()) {
                try {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        })
                .get(10, TimeUnit.MINUTES);
    }

    boolean routingCorrect() {

        for (final ApplicationDescriptor host_descriptor1 : network) {
            for (final ApplicationDescriptor host_descriptor2 : network) {

                final PeerReference node1 = host_descriptor1.getApplicationReference();
                final PeerReference node2 = host_descriptor2.getApplicationReference();

                try {
                    assertRoutingCorrect(node1, node2);
                }
                catch (Exception e) {
                    return false;
                }

                LOGGER.info("routing from {} to {} is correct", node1.getKey(), node2.getKey());
            }
        }
        return true;
    }

    CompletableFuture<Void> assertRoutingCorrect(final PeerReference source, final PeerReference target) {

        return assertRoutingToSmallerKeyCorrect(source, target).thenCompose(smaller_correct -> assertRoutingToSameKeyCorrect(source, target))
                .thenCompose(same_correct -> assertRoutingToLargerKeyCorrect(source, target));
    }

    CompletableFuture<Void> assertRoutingToSmallerKeyCorrect(final PeerReference source, final PeerReference target) {

        // Check that a slightly smaller key than the target's key routes to the target, except
        // in the pathological case where the target has a predecessor with a key one less than it.
        return getPredecessor(target).thenCompose(predecessor_of_target -> {

            final Key one_before_key = new Key(target.getKey()
                    .getValue()
                    .subtract(BigInteger.ONE));
            final boolean pathological = predecessor_of_target != null && predecessor_of_target.getKey()
                    .equals(one_before_key);

            return lookup(source, one_before_key).thenAccept(result_for_smaller_key -> {

                assertTrue(!pathological && result_for_smaller_key.getKey()
                        .equals(target.getKey()) || pathological && result_for_smaller_key.getKey()
                        .equals(predecessor_of_target.getKey()));
            });

        });

    }

    private static CompletableFuture<PeerReference> getPredecessor(final PeerReference target) {

        return PeerFactory.bind(target)
                .pull(CHORD_PREDECESSOR_SELECTOR)
                .thenApply(selection -> selection.get(0));
    }

    private static CompletableFuture<Void> assertRoutingToSameKeyCorrect(final PeerReference source, final PeerReference target) {

        return lookup(source, target.getKey()).thenAccept(result_for_same_key -> {

            assertEquals(target.getKey(), result_for_same_key.getKey());
        });
    }

    private static CompletableFuture<Void> assertRoutingToLargerKeyCorrect(final PeerReference source, final PeerReference target) {

        // Check that a slightly larger key than the node's key routes to the node's successor.
        return getSuccessor(target).thenCompose(successor_of_target -> {

            final Key target_key = target.getKey();
            final Key one_after_target_key = target_key.next();

            return lookup(source, one_after_target_key).thenAccept(result_for_larger_key -> {
                assertEquals(result_for_larger_key.getKey(), successor_of_target.getKey());
            });
        });
    }

    private static CompletableFuture<PeerReference> getSuccessor(final PeerReference target) {

        return PeerFactory.bind(target)
                .pull(CHORD_SUCCESSOR_SELECTOR)
                .thenApply(selection -> selection.get(0));
    }

    private static CompletableFuture<PeerReference> lookup(final PeerReference source, final Key key) {

        return PeerFactory.bind(source)
                .lookup(key);
    }

    @Test
    public void _4rdTestFingerTableCompleteness() throws Exception {

        CompletableFuture.runAsync(() -> {
            while (!checkFingerTableCompleteness()) {
                try {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        })
                .get(10, TimeUnit.MINUTES);

    }

    private static boolean checkFingerTableCompleteness() {

        for (ApplicationDescriptor descriptor : network) {
            final Peer peer = network.getPeer(descriptor);
            if (!isFingerTableComplete(peer)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isFingerTableComplete(final Peer peer) {

        PeerReference previous_finger_reference = null;
        final ChordPeerState peerState = (ChordPeerState) peer.getPeerState();
        final ChordFingerTable table = peerState.getFingerTable();

        for (final PeerReference finger_reference : table.getFingers()) {

            // Check that the finger is not null.
            if (finger_reference == null) { return false; }

            // Check that the finger is not closer in ring distance than the previous non-null finger.
            // Treat self-reference as the full ring distance, so ignore case where finger points to this node.

            final Key node_key = peer.key();
            final Key finger_key = finger_reference.getKey();

            if (previous_finger_reference != null && !finger_key.equals(node_key)) {

                if (Key.ringDistanceFurther(node_key, previous_finger_reference.getKey(), finger_key)) { return false; }
            }

            previous_finger_reference = finger_reference;
        }

        return true;
    }

    @Test
    public void _5thTestSuccessorListCompleteness() throws Exception {

        CompletableFuture.runAsync(() -> {
            while (!successorListComplete()) {
                try {
                    Thread.sleep(2000);
                }
                catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        })
                .get(10, TimeUnit.MINUTES);
    }

    boolean successorListComplete() {

        for (final ApplicationDescriptor host_descriptor : network) {
            if (!successorListComplete(host_descriptor)) { return false; }
        }

        return true;
    }

    boolean successorListComplete(final ApplicationDescriptor descriptor) {

        final Peer peer = network.getPeer(descriptor);
        final ChordPeerState peerState = (ChordPeerState) peer.getPeerState();
        final ChordSuccessorList successorList = peerState.getSuccessorList();
        List<PeerReference> successor_list = successorList.values();

        // The length of the successor lists should be MIN(max_successor_list_length, number_of_nodes - 1).
        if (successor_list.size() != Math.min(successorList.getMaxSize(), network.size() - 1)) { return false; }

        // Check that the successors follow the node round the ring.
        PeerReference ring_node = peerState.getSuccessor();

        for (final PeerReference successor_list_node : successor_list) {

            if (!successor_list_node.equals(ring_node)) { return false; }
            try {
                ring_node = getSuccessor(ring_node).get();
            }
            catch (Exception e) {
                return false;
            }
        }

        return true;
    }

    @Test
    public void _6thTestStabilizationAfterKill() throws Exception {

        killPortionOfNetwork();

        final int network_size_after_kill = network.size();
        LOGGER.info("awaiting stabilized ring of size {} after kill", network_size_after_kill);
        awaitRingSize(network_size_after_kill);
    }

    @Test
    public void _7thTestLookupCorrectnessAfterKill() throws Exception {

        _3thTestLookupCorrectness();
    }

    private static void killPortionOfNetwork() throws Exception {

        int kill_count = (int) (network.getMaxSize() * KILL_PORTION);
        LOGGER.info("killing {}% of the network", KILL_PORTION * 100);
        final Iterator<ApplicationDescriptor> network_iterator = network.iterator();
        while (network_iterator.hasNext() && kill_count != 0) {
            final ApplicationDescriptor kill_candidate = network_iterator.next();
            network.kill(kill_candidate);
            network.remove(kill_candidate);

            LOGGER.info("killed {}", kill_candidate);
            kill_count--;
        }
    }

    @AfterClass
    public static void tearDown() throws Exception {

        if (network != null) {
            network.shutdown();
        }
    }

    private static void awaitRingSize(final int expected_ring_size) throws InterruptedException {

        if (expected_ring_size <= 1) { return; }

        final CountDownLatch stable_ring_latch = new CountDownLatch(1);
        final RingSizeScanner ring_size_scanner = new RingSizeScanner();
        final RingSizeChangeListener ring_size_change_listener = new RingSizeChangeListener(stable_ring_latch, expected_ring_size);
        network.addScanner(ring_size_scanner);
        ring_size_scanner.addRingSizeChangeListener(ring_size_change_listener);
        ring_size_scanner.setEnabled(true);

        try {
            stable_ring_latch.await(10, TimeUnit.MINUTES);
        }
        finally {
            network.removeScanner(ring_size_scanner);
        }
    }

    private static final class RingSizeChangeListener implements PropertyChangeListener {

        private static final Logger LOGGER = LoggerFactory.getLogger(RingSizeChangeListener.class);
        private final CountDownLatch stable_ring_latch;
        private final int expected_ring_size;

        private RingSizeChangeListener(final CountDownLatch stable_ring_latch, final int expected_ring_size) {

            this.stable_ring_latch = stable_ring_latch;
            this.expected_ring_size = expected_ring_size;
        }

        @Override
        public void propertyChange(final PropertyChangeEvent evt) {

            final Integer new_ring_size = Integer.class.cast(evt.getNewValue());
            LOGGER.info("ring size changed from  " + evt.getOldValue() + " to " + new_ring_size);
            if (new_ring_size.equals(expected_ring_size)) {
                stable_ring_latch.countDown();
            }
        }
    }
}
