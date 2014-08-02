package uk.ac.standrews.cs.trombone.core;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Stream;
import uk.ac.standrews.cs.trombone.core.key.Key;
import uk.ac.standrews.cs.trombone.core.maintenance.Maintenance;
import uk.ac.standrews.cs.trombone.core.state.PeerState;
import uk.ac.standrews.cs.trombone.core.strategy.JoinStrategy;
import uk.ac.standrews.cs.trombone.core.strategy.LookupStrategy;
import uk.ac.standrews.cs.trombone.core.strategy.NextHopStrategy;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class TestPeerConfiguration implements PeerConfiguration {

    volatile List<PeerReference> peer_state_references;
    volatile CompletableFuture<PeerReference> lookup_result;
    volatile ScheduledExecutorService executor;
    volatile Peer peer_state_peer;
    volatile Peer join_strategy_peer;
    volatile Peer lookup_strategy_peer;
    volatile Peer next_hop_strategy_peer;
    volatile Optional<PeerMetric.LookupMeasurement> lookup_strategy_measurement;
    volatile Key lookup_strategy_target;
    volatile CompletableFuture<NextHopReference> next_hop_result;
    volatile Key next_hop_strategy_target;
    volatile CompletableFuture<Void> join_strategy_result;
    volatile PeerReference join_strategy_member;
    volatile PeerReference peer_state_added;
    volatile Maintenance maintenance;
    volatile Peer maintenance_peer;
    volatile int lookup_strategy_lookup_call;

    @Override
    public Maintenance getMaintenance(final Peer peer) {

        maintenance_peer = peer;
        return maintenance;
    }

    @Override
    public PeerState getPeerState(final Peer peer) {

        peer_state_peer = peer;
        return new PeerState() {

            @Override
            public boolean add(final PeerReference reference) {

                peer_state_added = reference;
                return true;
            }

            @Override
            public PeerReference remove(final PeerReference reference) {

                return null;
            }

            @Override
            public PeerReference closest(final Key target) {

                return null;
            }

            @Override
            public PeerReference first() {

                return peer_state_references != null && !peer_state_references.isEmpty() ? peer_state_references.get(0) : null;
            }

            @Override
            public PeerReference last() {

                return peer_state_references != null && !peer_state_references.isEmpty() ? peer_state_references.get(peer_state_references.size() - 1) : null;
            }

            @Override
            public Stream<PeerReference> stream() {

                return peer_state_references != null ? peer_state_references.stream() : null;
            }

            @Override
            public boolean inLocalKeyRange(final Key target) {

                return false;
            }

            @Override
            public int size() {

                return 0;
            }

            @Override
            public List<PeerReference> getReferences() {

                return peer_state_references;
            }
        };
    }

    @Override
    public JoinStrategy getJoinStrategy(final Peer peer) {

        join_strategy_peer = peer;

        return new JoinStrategy() {

            @Override
            public CompletableFuture<Void> apply(final PeerReference member) {

                join_strategy_member = member;

                return join_strategy_result;
            }
        };
    }

    @Override
    public LookupStrategy getLookupStrategy(final Peer peer) {

        lookup_strategy_peer = peer;

        return new LookupStrategy() {

            @Override
            public CompletableFuture<PeerReference> apply(final Key target, final Optional<PeerMetric.LookupMeasurement> measurement) {

                lookup_strategy_target = target;
                lookup_strategy_measurement = measurement;
                lookup_strategy_lookup_call++;
                System.out.println(lookup_strategy_lookup_call);
                return lookup_result;
            }
        };
    }

    @Override
    public NextHopStrategy getNextHopStrategy(final Peer peer) {

        next_hop_strategy_peer = peer;

        return new NextHopStrategy() {

            @Override
            public CompletableFuture<NextHopReference> apply(final Key target) {

                next_hop_strategy_target = target;
                return next_hop_result;
            }
        };
    }

    @Override
    public ScheduledExecutorService getExecutor() {

        return executor;
    }
}
