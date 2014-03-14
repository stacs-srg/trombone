package uk.ac.standrews.cs.trombone.core;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.ac.standrews.cs.trombone.core.key.Key;
import uk.ac.standrews.cs.trombone.core.key.KeyProvider;
import uk.ac.standrews.cs.trombone.core.selector.Self;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class PeerTest {

    private final InetSocketAddress initial_address = new InetSocketAddress(0);
    private final Key peer_key = new KeyProvider(32, DigestUtils.md5("ddd")).get();
    private final Peer peer = new Peer(initial_address, peer_key);
    private final PeerState peer_state = peer.getPeerState();
    private PeerReference peer_reference;

    @Before
    public void setUp() throws Exception {

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

        assertEquals(peer_key, peer.getKey());
        assertEquals(peer_key, peer_reference.getKey());
    }

    @Test
    public void testJoin() throws Exception {

        peer.join(peer_reference);
        assertFalse(peer_state.getReferences().contains(peer_reference));
    }

    @Test
    public void testPush() throws Exception {

        peer.push(peer_reference);
        assertFalse(peer_state.getReferences().contains(peer_reference));
    }

    @Test
    public void testPull() throws Exception {

        final List<PeerReference> reference = peer.pull(Self.getInstance());
        assertEquals(peer_reference, reference.get(0));
    }

    @Test
    public void testLookup() throws Exception {

        final PeerReference reference = peer.lookup(peer_key);
        assertEquals(peer_reference, reference);
    }

    @Test
    public void testGetAddress() throws Exception {

        assertEquals(peer.getAddress(), peer_reference.getAddress());
    }

    @Test
    public void testGetPeerState() throws Exception {

        assertNotNull(peer_state);
        assertNull(peer_state.firstReachable());
        assertNull(peer_state.lastReachable());
        assertEquals(0, peer_state.getReferences().size());
        peer_state.add(peer_reference);
        assertNull(peer_state.firstReachable());
        assertNull(peer_state.lastReachable());
        assertEquals(0, peer_state.getReferences().size());
    }

    @Test
    public void testGetRemote() throws Exception {

        assertEquals(peer, peer.getRemote(peer_reference));
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
