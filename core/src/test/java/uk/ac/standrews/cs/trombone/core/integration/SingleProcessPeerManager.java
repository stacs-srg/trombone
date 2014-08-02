package uk.ac.standrews.cs.trombone.core.integration;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.shabdiz.ApplicationDescriptor;
import uk.ac.standrews.cs.shabdiz.ApplicationManager;
import uk.ac.standrews.cs.shabdiz.ApplicationState;
import uk.ac.standrews.cs.shabdiz.util.AttributeKey;
import uk.ac.standrews.cs.trombone.core.AsynchronousPeerRemote;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerConfiguration;
import uk.ac.standrews.cs.trombone.core.PeerFactory;
import uk.ac.standrews.cs.trombone.core.PeerReference;
import uk.ac.standrews.cs.trombone.core.key.Key;
import uk.ac.standrews.cs.trombone.core.key.KeySupplier;
import uk.ac.standrews.cs.trombone.core.maintenance.ChordMaintenance;
import uk.ac.standrews.cs.trombone.core.maintenance.DisseminationStrategy;
import uk.ac.standrews.cs.trombone.core.maintenance.Maintenance;
import uk.ac.standrews.cs.trombone.core.selector.First;
import uk.ac.standrews.cs.trombone.core.selector.Last;
import uk.ac.standrews.cs.trombone.core.selector.Selector;
import uk.ac.standrews.cs.trombone.core.selector.Self;
import uk.ac.standrews.cs.trombone.core.state.ChordPeerState;
import uk.ac.standrews.cs.trombone.core.state.PeerState;
import uk.ac.standrews.cs.trombone.core.strategy.ChordJoinStrategy;
import uk.ac.standrews.cs.trombone.core.strategy.ChordLookupStrategy;
import uk.ac.standrews.cs.trombone.core.strategy.ChordNextHopStrategy;
import uk.ac.standrews.cs.trombone.core.strategy.JoinStrategy;
import uk.ac.standrews.cs.trombone.core.strategy.LookupStrategy;
import uk.ac.standrews.cs.trombone.core.strategy.NextHopStrategy;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class SingleProcessPeerManager implements ApplicationManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(SingleProcessPeerManager.class);
    private static final KeySupplier KEY_PROVIDER = new KeySupplier(894156);
    private static final AttributeKey<Peer> PEER_KEY = new AttributeKey<>();
    private static final Random RANDOM = new Random(545454);
    private final Set<PeerReference> joined_peers = new ConcurrentSkipListSet<>();
    public static final DisseminationStrategy STRATEGY = new DisseminationStrategy();

    static {
        final First successor = new First(1, Selector.ReachabilityCriteria.REACHABLE);
        final Last predecessor = new Last(1, Selector.ReachabilityCriteria.REACHABLE);
        final Self self = Self.INSTANCE;

        STRATEGY.addAction(new DisseminationStrategy.Action(false, false, predecessor, successor));
        STRATEGY.addAction(new DisseminationStrategy.Action(false, false, successor, predecessor));
        STRATEGY.addAction(new DisseminationStrategy.Action(false, true, self, successor));
        STRATEGY.addAction(new DisseminationStrategy.Action(false, true, self, predecessor));
    }

    public static final ScheduledThreadPoolExecutor EXECUTOR_SERVICE = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(100);

    public static final PeerConfiguration CONFIGURATION = new PeerConfiguration() {

        @Override
        public Maintenance getMaintenance(final Peer peer) {

            return new ChordMaintenance(peer, 3, TimeUnit.SECONDS);
        }

        @Override
        public PeerState getPeerState(final Peer peer) {

            return new ChordPeerState(peer, 8, Key.TWO, 10);
        }

        @Override
        public JoinStrategy getJoinStrategy(final Peer peer) {

            return new ChordJoinStrategy(peer);
        }

        @Override
        public LookupStrategy getLookupStrategy(final Peer peer) {

            return new ChordLookupStrategy(peer);
        }

        @Override
        public NextHopStrategy getNextHopStrategy(final Peer peer) {

            return new ChordNextHopStrategy(peer);
        }

        @Override
        public ScheduledExecutorService getExecutor() {

            return EXECUTOR_SERVICE;
        }
    };

    Peer getPeer(ApplicationDescriptor descriptor) {

        return descriptor.getAttribute(PEER_KEY);
    }

    @Override
    public ApplicationState probeState(final ApplicationDescriptor descriptor) {

        final Peer peer = descriptor.getAttribute(PEER_KEY);
        return peer != null ? peer.isExposed() ? ApplicationState.RUNNING : ApplicationState.UNREACHABLE : ApplicationState.AUTH;
    }

    @Override
    public synchronized Object deploy(final ApplicationDescriptor descriptor) throws Exception {

        final InetAddress host_address = descriptor.getHost()
                .getAddress();
        final InetSocketAddress peer_address = new InetSocketAddress(host_address, 0);
        final Peer peer = PeerFactory.createPeer(peer_address, KEY_PROVIDER.get(), CONFIGURATION);

        peer.expose();
        descriptor.setAttribute(PEER_KEY, peer);
        final PeerReference peer_reference = peer.getSelfReference();
        if (joined_peers.isEmpty()) {
            join(peer_reference).get();
        }
        else {
            join(peer_reference);

        }
        return peer_reference;
    }

    @Override
    public synchronized void kill(final ApplicationDescriptor descriptor) throws Exception {

        final Peer peer = descriptor.getAttribute(PEER_KEY);
        if (peer != null) {
            peer.unexpose();
            depart(peer.getSelfReference());
        }
    }

    private synchronized void depart(final PeerReference peer_reference) {

        joined_peers.remove(peer_reference);
    }

    private CompletableFuture<Void> join(final PeerReference peer_reference) {

        final AsynchronousPeerRemote remote = PeerFactory.bind(peer_reference);
        final PeerReference known_peer = randomlySelectJoinedPeer(peer_reference);
        return remote.join(known_peer)
                .thenRun(() -> {
                    LOGGER.info("{} joined successfully", peer_reference);
                    joined_peers.add(peer_reference);
                });
    }

    private synchronized PeerReference randomlySelectJoinedPeer(final PeerReference peer_reference) {

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
