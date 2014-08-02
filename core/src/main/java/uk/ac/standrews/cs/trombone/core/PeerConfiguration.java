package uk.ac.standrews.cs.trombone.core;

import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;
import java.util.function.Supplier;
import uk.ac.standrews.cs.trombone.core.maintenance.Maintenance;
import uk.ac.standrews.cs.trombone.core.state.PeerState;
import uk.ac.standrews.cs.trombone.core.strategy.JoinStrategy;
import uk.ac.standrews.cs.trombone.core.strategy.LookupStrategy;
import uk.ac.standrews.cs.trombone.core.strategy.NextHopStrategy;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public interface PeerConfiguration {

    String PEER_KEY_LENGTH_SYSTEM_PROPERTY = "peer.key.length";
    int KEY_LENGTH = Integer.parseInt(System.getProperty(PEER_KEY_LENGTH_SYSTEM_PROPERTY, String.valueOf(16)));     // Default 32 bit key

    default SyntheticDelay getSyntheticDelay() {

        return SyntheticDelay.ZERO;
    }

    default boolean isApplicationFeedbackEnabled() {

        return false;
    }

    Maintenance getMaintenance(Peer peer);

    PeerState getPeerState(Peer peer);

    JoinStrategy getJoinStrategy(Peer peer);

    LookupStrategy getLookupStrategy(Peer peer);

    NextHopStrategy getNextHopStrategy(Peer peer);

    ScheduledExecutorService getExecutor();

    // TODO include self reference in inter-peer communications flag
    // TODO lean from inter-peer communications  flag
    // TODO piggyback on serve flag and implementation?

    class Builder {

        private SyntheticDelay synthetic_delay = SyntheticDelay.ZERO;
        private Function<Peer, Maintenance> maintenance_factory;
        private boolean application_feedback_enabled;
        private Function<Peer, PeerState> peer_state_factory;
        private Function<Peer, JoinStrategy> join_strategy_factory;
        private Function<Peer, NextHopStrategy> next_hop_strategy_factory;
        private Function<Peer, LookupStrategy> lookup_strategy_factory;
        private Supplier<ScheduledExecutorService> executor_supplier;

        /** Constructs a new {@link Builder builder} */
        public Builder() {

        }

        /**
         * Constructs a copy of the given {@code builder}.
         *
         * @param builder the builder from which to copy
         */
        public Builder(Builder builder) {

            synthetic_delay = builder.synthetic_delay;
            maintenance_factory = builder.maintenance_factory;
            application_feedback_enabled = builder.application_feedback_enabled;
            peer_state_factory = builder.peer_state_factory;
            join_strategy_factory = builder.join_strategy_factory;
            next_hop_strategy_factory = builder.next_hop_strategy_factory;
            lookup_strategy_factory = builder.lookup_strategy_factory;
            executor_supplier = builder.executor_supplier;
        }

        public Builder syntheticDelay(SyntheticDelay synthetic_delay) {

            this.synthetic_delay = synthetic_delay;

            return this;
        }

        public Builder maintenance(Function<Peer, Maintenance> maintenance_factory) {

            this.maintenance_factory = maintenance_factory;
            return this;
        }

        public Builder enableApplicationFeedback(boolean application_feedback_enabled) {

            this.application_feedback_enabled = application_feedback_enabled;
            return this;
        }

        public Builder peerState(Function<Peer, PeerState> peer_state_factory) {

            this.peer_state_factory = peer_state_factory;
            return this;
        }

        public Builder lookupStrategy(Function<Peer, LookupStrategy> lookup_strategy_factory) {

            this.lookup_strategy_factory = lookup_strategy_factory;
            return this;
        }

        public Builder joinStrategy(Function<Peer, JoinStrategy> join_strategy_factory) {

            this.join_strategy_factory = join_strategy_factory;
            return this;
        }

        public Builder nextHopStrategy(Function<Peer, NextHopStrategy> next_hop_strategy_factory) {

            this.next_hop_strategy_factory = next_hop_strategy_factory;
            return this;
        }

        public Builder executor(Supplier<ScheduledExecutorService> executor_supplier) {

            this.executor_supplier = executor_supplier;
            return this;
        }

        public PeerConfiguration build() {

            return new PeerConfiguration() {

                @Override
                public SyntheticDelay getSyntheticDelay() {

                    return synthetic_delay;
                }

                @Override
                public boolean isApplicationFeedbackEnabled() {

                    return application_feedback_enabled;
                }

                @Override
                public Maintenance getMaintenance(final Peer peer) {

                    return maintenance_factory.apply(peer);
                }

                @Override
                public PeerState getPeerState(final Peer peer) {

                    return peer_state_factory.apply(peer);
                }

                @Override
                public JoinStrategy getJoinStrategy(final Peer peer) {

                    return join_strategy_factory.apply(peer);
                }

                @Override
                public LookupStrategy getLookupStrategy(final Peer peer) {

                    return lookup_strategy_factory.apply(peer);
                }

                @Override
                public NextHopStrategy getNextHopStrategy(final Peer peer) {

                    return next_hop_strategy_factory.apply(peer);
                }

                @Override
                public ScheduledExecutorService getExecutor() {

                    return executor_supplier.get();
                }
            };
        }

    }
}
