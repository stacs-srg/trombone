package uk.ac.standrews.cs.trombone.core;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomAdaptor;
import org.mashti.jetson.Server;
import org.mashti.jetson.ServerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.trombone.core.maintenance.Maintenance;
import uk.ac.standrews.cs.trombone.core.selector.Selector;
import uk.ac.standrews.cs.trombone.core.state.PeerState;
import uk.ac.standrews.cs.trombone.core.strategy.JoinStrategy;
import uk.ac.standrews.cs.trombone.core.strategy.LookupStrategy;
import uk.ac.standrews.cs.trombone.core.strategy.NextHopStrategy;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class Peer implements AsynchronousPeerRemote {

    private static final Logger LOGGER = LoggerFactory.getLogger(Peer.class);
    private static final String EXPOSURE_PROPERTY_NAME = "exposure";
    private static final ServerFactory<AsynchronousPeerRemote> SERVER_FACTORY = new PeerServerFactory();

    private final PeerState state;
    private final PeerClientFactory remote_factory;
    private final Key key;
    private final Server server;
    private final PropertyChangeSupport property_change_support;
    private final PeerMetric metric;
    private final Maintenance maintenance;
    private final Random random;
    private final ScheduledExecutorService executor;
    private volatile PeerReference self;
    private final JoinStrategy join_strategy;
    private final LookupStrategy lookup_strategy;
    private final NextHopStrategy next_hop_strategy;

    Peer(final InetSocketAddress address, final Key key, PeerConfiguration configuration) {

        this.key = key;
        random = new RandomAdaptor(new MersenneTwister(key.longValue()));
        property_change_support = new PropertyChangeSupport(this);
        metric = new PeerMetric(configuration.isApplicationFeedbackEnabled());

        server = SERVER_FACTORY.createServer(this);
        server.setBindAddress(address);
        server.setWrittenByteCountListener(metric);
        remote_factory = new PeerClientFactory(this, configuration.getSyntheticDelay());
        refreshSelfReference();

        executor = configuration.getExecutor();
        state = configuration.getPeerState().apply(this);
        maintenance = configuration.getMaintenance().apply(this);
        join_strategy = configuration.getJoinStrategy();
        lookup_strategy = configuration.getLookupStrategy();
        next_hop_strategy = configuration.getNextHopStrategy();

    }

    public Random getRandom() {

        return random;
    }

    public synchronized boolean expose() throws IOException {

        final boolean exposed = server.expose();
        if (exposed) {
            refreshSelfReference();
            property_change_support.firePropertyChange(EXPOSURE_PROPERTY_NAME, false, true);
            LOGGER.debug("exposed {} on {}", key, getAddress());

        }
        return exposed;
    }

    public synchronized boolean unexpose() throws IOException {

        final boolean unexposed = server.unexpose();
        if (unexposed) {
            property_change_support.firePropertyChange(EXPOSURE_PROPERTY_NAME, true, false);
            LOGGER.debug("unexposed {} from {}", key, getAddress());
        }
        return unexposed;
    }

    @Override
    public CompletableFuture<Key> getKey() {

        return CompletableFuture.completedFuture(key);
    }

    public Key key() {

        return key;
    }

    @Override
    public CompletableFuture<Void> join(final PeerReference member) {

        return join_strategy.join(this, member);
    }

    @Override
    public CompletableFuture<Void> push(final PeerReference reference) {

        return CompletableFuture.runAsync(() -> state.add(reference), executor);
    }

    @Override
    public CompletableFuture<Void> push(final List<PeerReference> references) {

        return CompletableFuture.runAsync(() -> {
            if (references != null) {
                references.forEach(state:: add);
            }
        }, executor);
    }

    @Override
    public CompletableFuture<List<PeerReference>> pull(final Selector selector) {

        return CompletableFuture.supplyAsync(() -> selector.select(this), executor);
    }

    @Override
    public CompletableFuture<PeerReference> lookup(final Key target) {

        return lookup(target, Optional.empty());
    }

    @Override
    public CompletableFuture<NextHopReference> nextHop(final Key target) {

        return next_hop_strategy.nextHop(this, target);
    }

    @Override
    public boolean equals(final Object other) {

        if (this == other) { return true; }
        if (!(other instanceof Peer)) { return false; }

        final Peer that = (Peer) other;
        return key.equals(that.key);
    }

    @Override
    public int hashCode() {

        return key.hashCode();
    }

    public InetSocketAddress getAddress() {

        return server.getLocalSocketAddress();
    }

    public PeerState getPeerState() {

        return state;
    }

    public PeerReference getSelfReference() {

        return self;
    }

    public AsynchronousPeerRemote getAsynchronousRemote(final PeerReference reference) {

        return !self.equals(reference) ? remote_factory.get(reference) : this;
    }

    public PeerMetric getPeerMetric() {

        return metric;
    }

    public void addExposureChangeListener(final PropertyChangeListener listener) {

        property_change_support.addPropertyChangeListener(EXPOSURE_PROPERTY_NAME, listener);
    }

    public boolean isExposed() {

        return server.isExposed();
    }

    public CompletableFuture<PeerMetric.LookupMeasurement> lookupWithRetry(final Key target, int retry_count) {

        return lookupWithRetry(target, retry_count, null);
    }

    public CompletableFuture<PeerMetric.LookupMeasurement> lookupWithRetry(final Key target, int retry_count, final PeerReference expected_result) {

        final PeerMetric.LookupMeasurement measurement = metric.newLookupMeasurement(retry_count, expected_result);
        final CompletableFuture<PeerMetric.LookupMeasurement> future_lookup_measurement = new CompletableFuture<>();
        lookupWithRetryRecursively(future_lookup_measurement, target, measurement);

        return future_lookup_measurement;
    }

    public ScheduledExecutorService getExecutor() {

        return executor;
    }

    private void lookupWithRetryRecursively(final CompletableFuture<PeerMetric.LookupMeasurement> future_lookup_measurement, final Key target, final PeerMetric.LookupMeasurement measurement) {

        final CompletableFuture<PeerReference> lookup_try = lookup(target, Optional.of(measurement));
        lookup_try.whenComplete((result, error) -> {

            measurement.incrementRetryCount();

            if (lookup_try.isCompletedExceptionally()) {
                if (measurement.hasRetryThresholdReached()) {
                    measurement.stop(error);
                    future_lookup_measurement.complete(measurement);
                }
                else {
                    lookupWithRetryRecursively(future_lookup_measurement, target, measurement);
                }
            }
            else {
                future_lookup_measurement.complete(measurement);
                measurement.stop(result);
            }
        });
    }

    public Maintenance getMaintenance() {

        return maintenance;
    }

    private CompletableFuture<PeerReference> lookup(final Key target, final Optional<PeerMetric.LookupMeasurement> optional_measurement) {

        return lookup_strategy.lookup(this, target, optional_measurement);
    }

    private void refreshSelfReference() {

        self = new PeerReference(key, getAddress());
    }
}
