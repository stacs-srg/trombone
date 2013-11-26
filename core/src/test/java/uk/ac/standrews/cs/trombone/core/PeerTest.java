package uk.ac.standrews.cs.trombone.core;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.ac.standrews.cs.trombone.core.gossip.selector.Self;
import uk.ac.standrews.cs.trombone.core.key.IntegerKey;
import uk.ac.standrews.cs.trombone.core.key.RandomIntegerKeyProvider;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class PeerTest {

    private final InetSocketAddress initial_address = new InetSocketAddress(0);
    private final IntegerKey peer_key = new RandomIntegerKeyProvider(555).get();
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

        Assert.assertFalse(peer.expose());
        Assert.assertFalse(peer.expose());
        Assert.assertFalse(peer.expose());
        Assert.assertTrue(peer.unexpose());
        Assert.assertFalse(peer.unexpose());
        Assert.assertFalse(peer.unexpose());
        Assert.assertTrue(peer.expose());
        Assert.assertFalse(peer.expose());
        Assert.assertFalse(peer.expose());
    }

    @Test
    public void testGetKey() throws Exception {

        Assert.assertEquals(peer_key, peer.getKey());
        Assert.assertEquals(peer_key, peer_reference.getKey());
    }

    @Test
    public void testJoin() throws Exception {

        peer.join(peer_reference);
        Assert.assertFalse(peer_state.getReferences().contains(peer_reference));
    }

    @Test
    public void testPush() throws Exception {

        peer.push(peer_reference);
        Assert.assertFalse(peer_state.getReferences().contains(peer_reference));
    }

    @Test
    public void testPull() throws Exception {

        final PeerReference[] reference = peer.pull(Self.getInstance(), 1);
        Assert.assertEquals(peer_reference, reference[0]);
    }

    @Test
    public void testLookup() throws Exception {

        final PeerReference reference = peer.lookup(peer_key);
        Assert.assertEquals(peer_reference, reference);
    }

    @Test
    public void testGetAddress() throws Exception {

        Assert.assertEquals(peer.getAddress(), peer_reference.getAddress());
    }

    @Test
    public void testGetPeerState() throws Exception {

        Assert.assertNotNull(peer_state);
        Assert.assertNull(peer_state.first());
        Assert.assertNull(peer_state.last());
        Assert.assertTrue(peer_state.getReferences().size() == 0);
        peer_state.add(peer_reference);
        Assert.assertNull(peer_state.first());
        Assert.assertNull(peer_state.last());
        Assert.assertTrue(peer_state.getReferences().size() == 0);
    }

    @Test
    public void testGetRemote() throws Exception {

        Assert.assertEquals(peer, peer.getRemote(peer_reference));
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

        Assert.assertEquals(1, exposed_notification_count.get());
        Assert.assertEquals(1, unexposed_notification_count.get());
    }

    @Test
    public void testIsExposed() throws Exception {

        Assert.assertTrue(peer.isExposed());
        peer.unexpose();
        Assert.assertFalse(peer.isExposed());
    }
}
