package uk.ac.standrews.cs.trombone.core;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.ac.standrews.cs.trombone.core.selector.First;
import uk.ac.standrews.cs.trombone.core.selector.Selector;
import uk.ac.standrews.cs.trombone.core.selector.Self;
import uk.ac.standrews.cs.trombone.core.state.PeerState;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class PeerTest {

    private final Random random = new Random(741456);
    private final InetSocketAddress initial_address = new InetSocketAddress("localhost", generateRandomPort());

    private int generateRandomPort() {

        return random.nextInt(0xffff);
    }

    private final Supplier<Key> key_provider = () -> Key.valueOf(random.nextLong());
    private final Key peer_key = key_provider.get();
    private final TestPeerConfiguration configuration = new TestPeerConfiguration();
    private Peer peer;
    private PeerState peer_state;
    private PeerReference peer_reference;

    @Before
    public void setUp() throws Exception {

        configuration.executor = Executors.newSingleThreadScheduledExecutor();
        peer = new Peer(initial_address, peer_key, configuration);
        peer_state = peer.getPeerState();
        peer.expose();
        peer_reference = peer.getSelfReference();
    }

    @After
    public void tearDown() throws Exception {

        peer.unexpose();
    }

    @Test
    public void testExposure() throws Exception {

        assertFalse(peer.expose());
        assertFalse(peer.expose());
        assertFalse(peer.expose());
        assertTrue(peer.unexpose());
        assertFalse(peer.unexpose());
        assertFalse(peer.unexpose());
        assertTrue(peer.expose());
        assertFalse(peer.expose());
        assertFalse(peer.expose());
    }

    @Test
    public void testGetKey() throws Exception {

        assertEquals(peer_key, peer.getKey()
                .get());
        assertEquals(peer_key, peer.key());
        assertEquals(peer_key, peer_reference.getKey());
        assertEquals(peer_key, peer.getAsynchronousRemote(peer_reference)
                .getKey()
                .toCompletableFuture()
                .get());
    }

    @Test
    public void testGetRandom() throws Exception {

        assertNotNull(peer.getRandom());
    }

    @Test
    public void testJoin() throws Exception {

        final PeerReference reference = generateRandomReference();
        final CompletableFuture<Void> future_join = peer.join(reference);
        assertEquals(peer, configuration.join_strategy_peer);
        assertEquals(reference, configuration.join_strategy_member);
        assertEquals(configuration.join_strategy_result, future_join);
    }

    @Test
    public void testPush() throws Exception {

        final PeerReference reference = generateRandomReference();
        peer.push(reference)
                .get();
        assertEquals(reference, configuration.peer_state_added);

        peer.push((PeerReference) null)
                .get();
        assertNull(configuration.peer_state_added);

        List<PeerReference> references = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            references.add(generateRandomReference());
        }
        peer.push(references)
                .get();
        final PeerReference last = references.get(references.size() - 1);
        assertEquals(last, configuration.peer_state_added);

        peer.push((List<PeerReference>) null)
                .get();
        assertEquals(last, configuration.peer_state_added);

    }

    private PeerReference generateRandomReference() {

        return new PeerReference(key_provider.get(), new InetSocketAddress(generateRandomPort()));
    }

    @Test
    public void testPull() throws Exception {

        assertEquals(peer_reference, peer.pull(Self.getInstance())
                .get()
                .get(0));

        final PeerReference reference = generateRandomReference();
        final ArrayList<PeerReference> references = new ArrayList<>();
        references.add(reference);

        configuration.peer_state_references = references;
        assertEquals(reference, peer.pull(new First(1, Selector.ReachabilityCriteria.REACHABLE))
                .get()
                .get(0));
    }

    @Test
    public void testLookup() throws Exception {

        configuration.lookup_result = CompletableFuture.completedFuture(peer_reference);
        final PeerReference reference = peer.lookup(peer_key)
                .get();
        assertEquals(peer_reference, reference);

        assertEquals(peer, configuration.lookup_strategy_peer);
        assertEquals(peer_key, configuration.lookup_strategy_target);
        assertFalse(configuration.lookup_strategy_measurement.isPresent());

    }

    @Test
    public void testLookupWithRetry() throws Exception {

        final Key target = key_provider.get();
        configuration.lookup_strategy_lookup_call = 0;
        CompletableFuture<PeerReference> failed_lookup = new CompletableFuture<>();

        final RuntimeException failure_cause = new RuntimeException("TEST");
        failed_lookup.completeExceptionally(failure_cause);
        configuration.lookup_result = failed_lookup;

        final int retry_count = 10;
        final CompletableFuture<PeerMetric.LookupMeasurement> result = peer.lookupWithRetry(target, retry_count);
        final PeerMetric.LookupMeasurement measurement = result.get();

        assertEquals(retry_count, configuration.lookup_strategy_lookup_call);
        assertEquals(retry_count, measurement.getRetryCount());
        assertTrue(measurement.isDoneInError());
        assertEquals(failure_cause, measurement.getError());

        configuration.lookup_strategy_lookup_call = 0;
        configuration.lookup_result = CompletableFuture.completedFuture(generateRandomReference());

        final PeerMetric.LookupMeasurement successful_measuremnet = peer.lookupWithRetry(target, retry_count)
                .get();
        assertEquals(1, successful_measuremnet.getRetryCount());
        assertFalse(successful_measuremnet.isDoneInError());
        assertEquals(configuration.lookup_result.get(), successful_measuremnet.getResult());
        assertEquals(1, configuration.lookup_strategy_lookup_call);

    }

    @Test
    public void testGetMaintenance() throws Exception {

        assertEquals(peer, configuration.maintenance_peer);
        assertEquals(configuration.maintenance, peer.getMaintenance());
    }

    @Test
    public void testNextHop() throws Exception {

        configuration.next_hop_result = CompletableFuture.completedFuture(new NextHopReference(peer_reference, true));
        final PeerReference reference = peer.nextHop(peer_key)
                .get();
        assertEquals(peer_reference, reference);

        assertEquals(peer, configuration.next_hop_strategy_peer);
        assertEquals(peer_key, configuration.next_hop_strategy_target);

        final Key key = key_provider.get();
        final CompletableFuture<NextHopReference> result = CompletableFuture.completedFuture(new NextHopReference(generateRandomReference(), true));
        configuration.next_hop_result = result;

        assertEquals(result, peer.nextHop(key));
        assertEquals(key, configuration.next_hop_strategy_target);

    }

    @Test
    public void testHashCode() throws Exception {

        assertEquals(peer_key.hashCode(), peer.hashCode());
    }

    @Test
    public void testEquals() throws Exception {

        assertTrue(peer.equals(peer));
        assertFalse(peer.equals(generateRandomReference()));
        assertTrue(peer.equals(new Peer(new InetSocketAddress(generateRandomPort()), peer_key, configuration)));
        assertFalse(peer.equals(new Peer(new InetSocketAddress(generateRandomPort()), key_provider.get(), configuration)));

    }

    @Test
    public void testGetMetric() throws Exception {

        assertNotNull(peer.getPeerMetric());
    }

    @Test
    public void testGetExecutor() throws Exception {

        assertEquals(configuration.executor, peer.getExecutor());
    }

    @Test
    public void testGetAddress() throws Exception {

        assertEquals(peer.getAddress(), peer_reference.getAddress());
        assertEquals(peer.getAddress(), initial_address);
    }

    @Test
    public void testGetPeerState() throws Exception {

        assertNotNull(peer_state);
        assertEquals(peer, configuration.peer_state_peer);
    }

    @Test
    public void testGetRemote() throws Exception {

        assertEquals(peer, peer.getAsynchronousRemote(peer_reference));
        assertFalse(peer.getAsynchronousRemote(generateRandomReference()) instanceof Peer);
    }

    @Test
    public void testAddExposureChangeListener() throws Exception {

        final AtomicInteger exposed_notification_count = new AtomicInteger();
        final AtomicInteger unexposed_notification_count = new AtomicInteger();

        peer.addExposureChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(final PropertyChangeEvent evt) {

                final Boolean exposed = (Boolean) evt.getNewValue();
                if (exposed) {
                    exposed_notification_count.incrementAndGet();
                }
                else {
                    unexposed_notification_count.incrementAndGet();
                }
            }
        });

        peer.unexpose();
        peer.unexpose();
        peer.unexpose();

        peer.expose();
        peer.expose();
        peer.expose();

        assertEquals(1, exposed_notification_count.get());
        assertEquals(1, unexposed_notification_count.get());
    }

    @Test
    public void testIsExposed() throws Exception {

        assertTrue(peer.isExposed());
        peer.unexpose();
        assertFalse(peer.isExposed());
    }
}
