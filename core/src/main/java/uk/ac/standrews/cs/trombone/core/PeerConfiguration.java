package uk.ac.standrews.cs.trombone.core;

import java.io.Serializable;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import uk.ac.standrews.cs.trombone.core.key.Key;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public abstract class PeerConfiguration implements Serializable {

    private static final long serialVersionUID = -6034624232106255574L;
    private final MaintenanceFactory maintenance;
    private final SyntheticDelay synthetic_delay;
    private boolean application_feedback_enabled;

    protected PeerConfiguration(MaintenanceFactory maintenance) {

        this(maintenance, SyntheticDelay.ZERO);
    }

     protected PeerConfiguration(MaintenanceFactory maintenance, SyntheticDelay synthetic_delay) {

        this.maintenance = maintenance;
        this.synthetic_delay = synthetic_delay;
    }

    public MaintenanceFactory getMaintenanceFactory() {

        return maintenance;
    }

    public SyntheticDelay getSyntheticDelay() {

        return synthetic_delay;
    }

    public boolean isApplicationFeedbackEnabled() {

        return application_feedback_enabled;
    }

    public void setApplicationFeedbackEnabled(final boolean application_feedback_enabled) {

        this.application_feedback_enabled = application_feedback_enabled;
    }

    public abstract RoutingState getPeerState(Peer peer);

    public abstract Function<PeerReference, CompletableFuture<Void>> getJoinStrategy(Peer peer);

    public abstract BiFunction<Key, Optional<PeerMetric.LookupMeasurement>, CompletableFuture<PeerReference>> getLookupStrategy(Peer peer);

    public abstract Function<Key, CompletableFuture<PeerReference>> getNextHopStrategy(Peer peer);
}
