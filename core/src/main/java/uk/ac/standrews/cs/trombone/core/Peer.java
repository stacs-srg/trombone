package uk.ac.standrews.cs.trombone.core;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.codec.digest.DigestUtils;
import org.mashti.jetson.Server;
import org.mashti.jetson.ServerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncommons.maths.random.MersenneTwisterRNG;
import uk.ac.standrews.cs.trombone.core.key.Key;
import uk.ac.standrews.cs.trombone.core.selector.Selector;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class Peer implements AsynchronousPeerRemote {

    private static final Logger LOGGER = LoggerFactory.getLogger(Peer.class);
    private static final String EXPOSURE_PROPERTY_NAME = "exposure";
    private static final ServerFactory<AsynchronousPeerRemote> SERVER_FACTORY = new PeerServerFactory();
    private final PeerState state;
    private final PeerClientFactory remote_factory;
    private final AsynchronousPeerClientFactory asynchronous_remote_factory;
    private final Key key;
    private final Server server;
    private final PropertyChangeSupport property_change_support;
    private final PeerMetric metric;
    private final PeerMaintainer maintainer;

    private final MersenneTwisterRNG random;
    private volatile PeerReference self;

    Peer(final Key key) {

        this(new InetSocketAddress(0), key);
    }

    Peer(final InetSocketAddress address, final Key key) {

        this(address, key, PeerFactory.DEFAULT_PEER_CONFIGURATION);
    }

    Peer(final InetSocketAddress address, final Key key, PeerConfiguration configuration) {

        this.key = key;
        random = new MersenneTwisterRNG(DigestUtils.md5(key.toByteArray()));
        property_change_support = new PropertyChangeSupport(this);
        metric = new PeerMetric(configuration.isApplicationFeedbackEnabled());
        state = new PeerState(key, metric);
        maintainer = configuration.getMaintenanceFactory().maintain(this);
        server = SERVER_FACTORY.createServer(this);
        server.setBindAddress(address);
        server.setWrittenByteCountListener(metric);
        remote_factory = new PeerClientFactory(this, configuration.getSyntheticDelay());
        asynchronous_remote_factory = new AsynchronousPeerClientFactory(this, configuration.getSyntheticDelay());
        refreshSelfReference();
    }

    public MersenneTwisterRNG getRandom() {

        return random;
    }

    public synchronized boolean expose() throws IOException {

        final boolean exposed = server.expose();
        if (exposed) {
            refreshSelfReference();
            property_change_support.firePropertyChange(EXPOSURE_PROPERTY_NAME, false, true);
            LOGGER.trace("exposed {} on {}", key, getAddress());

        }
        return exposed;
    }

    public synchronized boolean unexpose() throws IOException {

        final boolean unexposed = server.unexpose();
        if (unexposed) {
            property_change_support.firePropertyChange(EXPOSURE_PROPERTY_NAME, true, false);
        }
        return unexposed;
    }

    @Override
    public CompletableFuture<Key> getKey() {

        return CompletableFuture.completedFuture(key);
    }

    @Override
    public CompletableFuture<Void> join(final PeerReference member) {

        return CompletableFuture.runAsync(() -> {
            if (isExposed() && member != null && !self.equals(member)) {
                push(member);
            }
        });
    }

    @Override
    public CompletableFuture<Void> push(final PeerReference reference) {

        return CompletableFuture.runAsync(() -> { state.add(reference); });
    }

    @Override
    public CompletableFuture<Void> push(final List<PeerReference> references) {

        return CompletableFuture.runAsync(() -> {
            
            
            if (references != null) {
                references.forEach(reference -> {state.add(reference);});
            }
        });
    }

    @Override
    public CompletableFuture<List<PeerReference>> pull(final Selector selector) {

        return CompletableFuture.supplyAsync(() -> {
            return (List<PeerReference>) selector.select(this);
        });
    }

    @Override
    public CompletableFuture<PeerReference> lookup(final Key target) {

        return lookup(target, Optional.empty());
    }

    @Override
    public CompletableFuture<PeerReference> nextHop(final Key target) {

        return CompletableFuture.supplyAsync(() -> {
            final PeerReference next_hop;
            if (state.inLocalKeyRange(target)) {
                next_hop = self;
            }
            else {
                final PeerReference ceiling = state.ceilingReachable(target);
                next_hop = ceiling != null ? ceiling : state.firstReachable();
            }
            return next_hop;
        });
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

    private void lookupWithRetryRecursively(final CompletableFuture<PeerMetric.LookupMeasurement> future_lookup_measurement, final Key target, final PeerMetric.LookupMeasurement measurement) {

        final CompletableFuture<PeerReference> lookup_try = lookup(target, Optional.of(measurement));
        lookup_try.whenCompleteAsync((result, error) -> {

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
                measurement.stop(result);
                future_lookup_measurement.complete(measurement);
            }
        });
    }

    public DisseminationStrategy getDisseminationStrategy() {

        return maintainer.getDisseminationStrategy();
    }

    public PeerMaintainer getPeerMaintainer() {

        return maintainer;
    }

    private CompletableFuture<PeerReference> lookup(final Key target, final Optional<PeerMetric.LookupMeasurement> optional_measurement) {

        final CompletableFuture<PeerReference> future_lookup = new CompletableFuture<>();
        lookupHelper(future_lookup, target, self, nextHop(target), optional_measurement);
        return future_lookup;
    }

    void lookupHelper(CompletableFuture<PeerReference> future_lookup, Key target, PeerReference current, CompletableFuture<PeerReference> next, Optional<PeerMetric.LookupMeasurement> measurement) {

        next.whenCompleteAsync((next_hop, error) -> {
            
            if (next.isCompletedExceptionally()) {
                future_lookup.completeExceptionally(error);
                current.setReachable(false);
            }
            else {

                if (!current.equals(self) && measurement.isPresent()) {
                    final PeerMetric.LookupMeasurement lookupMeasurement = measurement.get();
                    lookupMeasurement.incrementHopCount();
                    if(lookupMeasurement.getHopCount() > 20){
                        System.out.println("HOP " + lookupMeasurement.getHopCount());
                        future_lookup.complete(next_hop);
                        return;
                    }
                }

                if (current.equals(next_hop)) {
                    future_lookup.complete(next_hop);
                }
                else {

                    final AsynchronousPeerRemote next_hop_remote = getAsynchronousRemote(next_hop);
                    next_hop_remote.push(self);
                    lookupHelper(future_lookup, target, next_hop, next_hop_remote.nextHop(target), measurement);
                }
            }
            
            push(current);
        }, MaintenanceFactory.SCHEDULER);
    }

    private void refreshSelfReference() {

        self = new PeerReference(key, getAddress());
    }
}
