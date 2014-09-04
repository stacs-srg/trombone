package uk.ac.standrews.cs.trombone.core;

import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;
import uk.ac.standrews.cs.trombone.core.maintenance.MaintenanceFactory;
import uk.ac.standrews.cs.trombone.core.state.PeerStateFactory;
import uk.ac.standrews.cs.trombone.core.strategy.JoinStrategy;
import uk.ac.standrews.cs.trombone.core.strategy.LookupStrategy;
import uk.ac.standrews.cs.trombone.core.strategy.NextHopStrategy;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class PeerConfiguration {

    public static final String PEER_KEY_LENGTH_SYSTEM_PROPERTY = "peer.key.length";
    public static final int KEY_LENGTH = Integer.parseInt(System.getProperty(PEER_KEY_LENGTH_SYSTEM_PROPERTY, String.valueOf(16)));     // Default 32 bit key
    private final Builder builder;

    public static class Builder {

        private SyntheticDelay synthetic_delay = SyntheticDelay.ZERO;
        private MaintenanceFactory maintenance_factory;
        private boolean application_feedback_enabled;
        private PeerStateFactory peer_state_factory;
        private JoinStrategy join_strategy;
        private NextHopStrategy next_hop_strategy;
        private LookupStrategy lookup_strategy;
        private Supplier<ScheduledExecutorService> executor_supplier;

        private Builder() {

        }

        private Builder(Builder builder) {

            synthetic_delay = builder.synthetic_delay;
            maintenance_factory = builder.maintenance_factory;
            application_feedback_enabled = builder.application_feedback_enabled;
            peer_state_factory = builder.peer_state_factory;
            join_strategy = builder.join_strategy;
            next_hop_strategy = builder.next_hop_strategy;
            lookup_strategy = builder.lookup_strategy;
            executor_supplier = builder.executor_supplier;
        }

        public Builder syntheticDelay(SyntheticDelay synthetic_delay) {

            this.synthetic_delay = synthetic_delay;

            return this;
        }

        public Builder maintenance(MaintenanceFactory maintenance_factory) {

            this.maintenance_factory = maintenance_factory;
            return this;
        }

        public Builder enableApplicationFeedback(boolean application_feedback_enabled) {

            this.application_feedback_enabled = application_feedback_enabled;
            return this;
        }

        public Builder peerState(PeerStateFactory peer_state_factory) {

            this.peer_state_factory = peer_state_factory;
            return this;
        }

        public Builder lookupStrategy(LookupStrategy lookup_strategy) {

            this.lookup_strategy = lookup_strategy;
            return this;
        }

        public Builder joinStrategy(JoinStrategy join_strategy) {

            this.join_strategy = join_strategy;
            return this;
        }

        public Builder nextHopStrategy(NextHopStrategy next_hop_strategy) {

            this.next_hop_strategy = next_hop_strategy;
            return this;
        }

        public Builder executor(Supplier<ScheduledExecutorService> executor_supplier) {

            this.executor_supplier = executor_supplier;
            return this;
        }

        public PeerConfiguration build() {

            return new PeerConfiguration(this);
        }

    }

    public static Builder builder() {

        return new Builder();
    }

    public static Builder builder(Builder base) {

        return new Builder(base);
    }

    protected PeerConfiguration(Builder builder) {

        this.builder = builder;
    }

    public SyntheticDelay getSyntheticDelay() {

        return SyntheticDelay.ZERO;
    }

    public boolean isApplicationFeedbackEnabled() {

        return builder.application_feedback_enabled;
    }

    public MaintenanceFactory getMaintenance() {

        return builder.maintenance_factory;
    }

    public PeerStateFactory getPeerState() {

        return builder.peer_state_factory;
    }

    public JoinStrategy getJoinStrategy() {

        return builder.join_strategy;
    }

    public LookupStrategy getLookupStrategy() {

        return builder.lookup_strategy;
    }

    public NextHopStrategy getNextHopStrategy() {

        return builder.next_hop_strategy;
    }

    public ScheduledExecutorService getExecutor() {

        return builder.executor_supplier.get();
    }

    // TODO include self reference in inter-peer communications flag
    // TODO lean from inter-peer communications  flag
    // TODO piggyback on serve flag and implementation?

}
