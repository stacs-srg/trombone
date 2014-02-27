package uk.ac.standrews.cs.trombone.core;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.shabdiz.ApplicationDescriptor;
import uk.ac.standrews.cs.shabdiz.ApplicationState;

import static org.junit.Assert.assertEquals;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class RecoveryTest {

    private static final double KILL_PORTION = 0.5;
    private static final Logger LOGGER = LoggerFactory.getLogger(RecoveryTest.class);
    private P2PNetwork network;


    @Before
    public void setUp() throws Exception {

        network = new SingleProcessLocalP2PNetwork(50);
        network.populate();
        network.deployAll();
        network.awaitAnyOfStates(ApplicationState.RUNNING);
    }

    @Test
    public void testSelfLookup() throws Exception {

        for (ApplicationDescriptor descriptor : network) {
            final PeerReference reference = descriptor.getApplicationReference();
            final PeerReference lookup_result = PeerFactory.bind(reference).lookup(reference.getKey());
            assertEquals(reference, lookup_result);
        }
    }

    @Test
    public void testStabilization() throws Exception {

        final int network_size = network.size();
        LOGGER.info("awaiting stabilized ring of size {}", network_size);
        awaitRingSize(network_size);

        killPortionOfNetwork();

        final int network_size_after_kill =  network.size();
        LOGGER.info("awaiting stabilized ring of size {} after kill", network_size_after_kill);
        awaitRingSize(network_size_after_kill);
    }

    private void killPortionOfNetwork() throws Exception {

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

    @After
    public void tearDown() throws Exception {

        if (network != null) {
            network.shutdown();
        }
    }

    private void awaitRingSize(final int expected_ring_size) throws InterruptedException {

        if (expected_ring_size <= 1) { return; }

        final CountDownLatch stable_ring_latch = new CountDownLatch(1);
        final RingSizeScanner ring_size_scanner = new RingSizeScanner();
        final RingSizeChangeListener ring_size_change_listener = new RingSizeChangeListener(stable_ring_latch, expected_ring_size);
        network.addScanner(ring_size_scanner);
        ring_size_scanner.addRingSizeChangeListener(ring_size_change_listener);
        ring_size_scanner.setEnabled(true);

        try {
            stable_ring_latch.await();
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
