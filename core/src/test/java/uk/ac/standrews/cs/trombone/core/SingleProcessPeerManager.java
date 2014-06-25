package uk.ac.standrews.cs.trombone.core;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.apache.commons.codec.digest.DigestUtils;
import org.mashti.jetson.exception.RPCException;
import uk.ac.standrews.cs.shabdiz.ApplicationDescriptor;
import uk.ac.standrews.cs.shabdiz.ApplicationManager;
import uk.ac.standrews.cs.shabdiz.ApplicationState;
import uk.ac.standrews.cs.shabdiz.util.AttributeKey;
import uk.ac.standrews.cs.trombone.core.key.KeyProvider;
import uk.ac.standrews.cs.trombone.core.selector.First;
import uk.ac.standrews.cs.trombone.core.selector.Last;
import uk.ac.standrews.cs.trombone.core.selector.Selector;
import uk.ac.standrews.cs.trombone.core.selector.Self;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class SingleProcessPeerManager implements ApplicationManager {

    private static final KeyProvider KEY_PROVIDER = new KeyProvider(32, DigestUtils.md5("sss"));
    private static final AttributeKey<Peer> PEER_KEY = new AttributeKey<Peer>();
    private static final Random RANDOM = new Random(545454);
    private static final MaintenanceFactory MAINTENANCE = new MaintenanceFactory();
    private final Set<PeerReference> joined_peers = new HashSet<PeerReference>();

    static {
        final DisseminationStrategy strategy = MAINTENANCE.getStrategy();
        final First successor = new First(1, Selector.ReachabilityCriteria.REACHABLE);
        final Last predecessor = new Last(1, Selector.ReachabilityCriteria.REACHABLE);
        final Self self = Self.INSTANCE;

        strategy.addAction(new DisseminationStrategy.Action(false, false, predecessor, successor));
        strategy.addAction(new DisseminationStrategy.Action(false, false, successor, predecessor));
        strategy.addAction(new DisseminationStrategy.Action(false, true, self, successor));
        strategy.addAction(new DisseminationStrategy.Action(false, true, self, predecessor));
    }

    private static final PeerConfiguration CONFIGURATION = new PeerConfiguration(MAINTENANCE, PeerFactory.NO_SYNTHETIC_DELAY);

    @Override
    public ApplicationState probeState(final ApplicationDescriptor descriptor) {

        final Peer peer = descriptor.getAttribute(PEER_KEY);
        return peer != null ? peer.isExposed() ? ApplicationState.RUNNING : ApplicationState.UNREACHABLE : ApplicationState.AUTH;
    }

    @Override
    public synchronized Object deploy(final ApplicationDescriptor descriptor) throws Exception {

        final InetAddress host_address = descriptor.getHost().getAddress();
        final InetSocketAddress peer_address = new InetSocketAddress(host_address, 0);
        final Peer peer = PeerFactory.createPeer(peer_address, KEY_PROVIDER.get(), CONFIGURATION);

        peer.expose();
        descriptor.setAttribute(PEER_KEY, peer);
        final PeerReference peer_reference = peer.getSelfReference();
        join(peer_reference);
        return peer_reference;
    }

    @Override
    public void kill(final ApplicationDescriptor descriptor) throws Exception {

        final Peer peer = descriptor.getAttribute(PEER_KEY);
        if (peer != null) {
            peer.unexpose();
            depart(peer.getSelfReference());
        }
    }

    private synchronized void depart(final PeerReference peer_reference) {

        joined_peers.remove(peer_reference);
    }

    private synchronized void join(final PeerReference peer_reference) throws RPCException {

        final PeerRemote remote = PeerFactory.bind(peer_reference);
        final PeerReference known_peer = randomlySelectJoinedPeer(peer_reference);
        remote.join(known_peer);
        joined_peers.add(peer_reference);
    }

    private synchronized PeerReference randomlySelectJoinedPeer(final PeerReference peer_reference) throws RPCException {

        if (!joined_peers.isEmpty()) {

            final int joined_peers_count = joined_peers.size();
            final int candidate_index = RANDOM.nextInt(joined_peers_count);
            int index = 0;
            for (PeerReference reference : joined_peers) {
                if (index == candidate_index) { return reference; }
                index++;
            }
        }
        return peer_reference;
    }
}
