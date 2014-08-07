package uk.ac.standrews.cs.trombone.core.integration;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import uk.ac.standrews.cs.trombone.core.Key;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerConfiguration;
import uk.ac.standrews.cs.trombone.core.PeerFactory;
import uk.ac.standrews.cs.trombone.core.PeerReference;
import uk.ac.standrews.cs.trombone.core.maintenance.MaintenanceFactory;
import uk.ac.standrews.cs.trombone.core.state.PeerStateFactory;
import uk.ac.standrews.cs.trombone.core.state.TrombonePeerStateFactory;
import uk.ac.standrews.cs.trombone.core.strategy.ChordLookupStrategy;
import uk.ac.standrews.cs.trombone.core.strategy.JoinStrategy;
import uk.ac.standrews.cs.trombone.core.strategy.LookupStrategy;
import uk.ac.standrews.cs.trombone.core.strategy.MinimalJoinStrategy;
import uk.ac.standrews.cs.trombone.core.strategy.NextHopStrategy;
import uk.ac.standrews.cs.trombone.core.strategy.TromboneNextHopStrategy;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LookupCorrectnessTest {

    private static final Random RANDOM = new Random(4152);
    public static final Supplier<Key> KEY_PROVIDER = () -> Key.valueOf(RANDOM.nextLong());
    private static final ScheduledExecutorService EXECUTOR_SERVICE = Executors.newScheduledThreadPool(100);
    public static final PeerConfiguration TROMBONE_CONFIGURATION = new PeerConfiguration() {

        @Override
        public MaintenanceFactory getMaintenance() {

            return peer -> null;
        }

        @Override
        public PeerStateFactory getPeerState() {

            return new TrombonePeerStateFactory();
        }

        @Override
        public JoinStrategy getJoinStrategy() {

            return new MinimalJoinStrategy();
        }

        @Override
        public LookupStrategy getLookupStrategy() {

            return new ChordLookupStrategy();
        }

        @Override
        public NextHopStrategy getNextHopStrategy() {

            return new TromboneNextHopStrategy();
        }

        @Override
        public ScheduledExecutorService getExecutor() {

            return EXECUTOR_SERVICE;
        }
    };
    private final NavigableMap<Key, Peer> network = new TreeMap<>();
    private static final int NETWORK_SIZE = 100;

    @Before
    public void setUp() throws Exception {

        List<PeerReference> references = new ArrayList<>();
        for (int i = 0; i < NETWORK_SIZE; i++) {
            final Key key = KEY_PROVIDER.get();
            final Peer peer = PeerFactory.createPeer(new InetSocketAddress(InetAddress.getLocalHost(), 0), key, TROMBONE_CONFIGURATION);
            network.put(key, peer);
            peer.expose();

            references.add(peer.getSelfReference());
        }

        for (Peer peer : network.values()) {
            final PeerReference successor = getSuccessor(peer.key()
                    .next());
            peer.join(successor)
                    .get();
            peer.push(getPredecessor(peer.key()
                    .previous()))
                    .get();
        }
    }

    @Test
    public void test1RingStabilisation() throws Exception {

        for (Map.Entry<Key, Peer> entry : network.entrySet()) {
            Key key = entry.getKey();
            Peer peer = entry.getValue();

            Assert.assertEquals(getSuccessor(key.next()), peer.getPeerState()
                    .first());
        }
    }

    @Test
    public void test2LookupCorrectness() throws Exception {

        List<CompletableFuture<?>> futures = new ArrayList<>();

        for (Map.Entry<Key, Peer> entry : network.entrySet()) {

            final Peer peer = entry.getValue();
            for (int i = 0; i < 50; i++) {

                final Key target = KEY_PROVIDER.get();
                final CompletableFuture<Void> future_test = peer.lookup(target)
                        .thenAccept(actual -> {

                            final PeerReference expected = getSuccessor(target);
                            Assert.assertEquals(expected, actual);
                        });

                futures.add(future_test);
            }
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]))
                .get();
    }

    private PeerReference getSuccessor(Key target) {

        final Map.Entry<Key, Peer> ceilingEntry = network.ceilingEntry(target);
        return (ceilingEntry == null ? network.firstEntry() : ceilingEntry).getValue()
                .getSelfReference();
    }

    private PeerReference getPredecessor(final Key target) {

        final Map.Entry<Key, Peer> floorEntry = network.floorEntry(target);
        return (floorEntry == null ? network.lastEntry() : floorEntry).getValue()
                .getSelfReference();
    }
}
