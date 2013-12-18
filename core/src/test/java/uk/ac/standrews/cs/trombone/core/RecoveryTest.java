package uk.ac.standrews.cs.trombone.core;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.shabdiz.ApplicationDescriptor;
import uk.ac.standrews.cs.shabdiz.ApplicationState;
import uk.ac.standrews.cs.shabdiz.util.Combinations;
import uk.ac.standrews.cs.test.category.Ignore;

import static org.junit.Assert.assertEquals;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
@RunWith(Parameterized.class)
@Category(Ignore.class)
public class RecoveryTest {

    private final P2PNetwork network;

    public RecoveryTest(P2PNetwork network) {

        this.network = network;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> getParameters() throws IOException {

        return Combinations.generateArgumentCombinations(new Object[][] {{new SingleProcessLocalP2PNetwork(10)}});
    }

    @Before
    public void setUp() throws Exception {

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

        awaitRingSize(network.getSize());

        int i = 0;
        for (ApplicationDescriptor descriptor : network) {

            if (i >= 3) {
                break;
            }

            network.kill(descriptor);
            network.remove(descriptor);

            System.out.println("killed " + descriptor);

            i++;
        }

        System.out.println("awaiting stabilized ring of size " + network.size());

        awaitRingSize(network.getSize());

    }

    @After
    public void tearDown() throws Exception {

        network.shutdown();
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
