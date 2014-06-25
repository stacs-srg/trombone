package uk.ac.standrews.cs.trombone.core;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.apache.commons.codec.digest.DigestUtils;
import org.mashti.jetson.Server;
import org.mashti.jetson.ServerFactory;
import org.mashti.jetson.exception.RPCException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncommons.maths.random.MersenneTwisterRNG;
import uk.ac.standrews.cs.trombone.core.key.Key;
import uk.ac.standrews.cs.trombone.core.selector.Selector;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class Peer implements PeerRemote {

    private static final Logger LOGGER = LoggerFactory.getLogger(Peer.class);
    private static final String EXPOSURE_PROPERTY_NAME = "exposure";
    private static final ServerFactory<PeerRemote> SERVER_FACTORY = new PeerServerFactory();
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
    public Key getKey() {

        return key;
    }

    @Override
    public synchronized void join(final PeerReference member) {

        if (isExposed() && member != null && !self.equals(member)) {
            push(member);
        }
    }

    @Override
    public void push(final PeerReference reference) {

        state.add(reference);

    }

    @Override
    public void push(final List<PeerReference> references) {

        if (references != null) {
            references.forEach(this :: push);
        }
    }

    @Override
    public List<PeerReference> pull(final Selector selector) {

        return (List<PeerReference>) selector.select(this);
    }

    @Override
    public PeerReference lookup(final Key target) throws RPCException {

        return lookup(target, null);
    }

    @Override
    public PeerReference nextHop(final Key target) {

        final PeerReference next_hop;
        if (state.inLocalKeyRange(target)) {
            next_hop = self;
        }
        else {
            final PeerReference ceiling = state.ceilingReachable(target);
            next_hop = ceiling != null ? ceiling : self;
        }
        return next_hop;
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

    public PeerRemote getRemote(final PeerReference reference) {

        return !self.equals(reference) ? remote_factory.get(reference) : this;
    }

    public AsynchronousPeerRemote getAsynchronousRemote(final PeerReference reference) {

        if (self.equals(reference)) {
            return new AsynchronousPeerRemote() {

                @Override
                public CompletableFuture<Key> getKey() {

                    return CompletableFuture.supplyAsync(() -> Peer.this.getKey());
                }

                @Override
                public CompletableFuture<Void> join(final PeerReference member) {

                    return CompletableFuture.runAsync(() -> Peer.this.join(member));
                }

                @Override
                public CompletableFuture<Void> push(final List<PeerReference> references) {

                    return CompletableFuture.runAsync(() -> Peer.this.push(references));
                }

                @Override
                public CompletableFuture<Void> push(final PeerReference reference) {

                    return CompletableFuture.runAsync(() -> Peer.this.push(reference));
                }

                @Override
                public CompletableFuture<List<PeerReference>> pull(final Selector selector) {

                    return CompletableFuture.supplyAsync(() -> Peer.this.pull(selector));
                }

                @Override
                public CompletableFuture<PeerReference> lookup(final Key target) {

                    final CompletableFuture<PeerReference> lookup_future = new CompletableFuture<>();

                    CompletableFuture.runAsync(() -> {
                        try {
                            lookup_future.complete(Peer.this.lookup(target));
                        }
                        catch (RPCException e) {
                            lookup_future.completeExceptionally(e);
                        }
                    });

                    return lookup_future;
                }

                @Override
                public CompletableFuture<PeerReference> nextHop(final Key target) {

                    return CompletableFuture.supplyAsync(() -> Peer.this.nextHop(target));
                }
            };
        }
        else {
            return asynchronous_remote_factory.get(reference);
        }
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

    public PeerMetric.LookupMeasurement lookup(final Key target, int retry_count) {

        final PeerMetric.LookupMeasurement measurement = metric.newLookupMeasurement(retry_count);
        do {
            try {
                final PeerReference result = lookup(target, measurement);
                measurement.stop(result);
            }
            catch (final RPCException e) {
                if (measurement.hasRetryThresholdReached()) {
                    measurement.stop(e);
                }
            }
            finally {
                measurement.incrementRetryCount();
            }
        } while (!Thread.currentThread().isInterrupted() && !measurement.isDone());
        return measurement;
    }

    public CompletionStage<PeerMetric.LookupMeasurement> lookupAsync(final Key target, int retry_count, final PeerReference expectedResult) {

        final PeerMetric.LookupMeasurement measurement = metric.newLookupMeasurement(retry_count, expectedResult);
        final CompletableFuture<PeerMetric.LookupMeasurement> future = new CompletableFuture<>();
        lookupAsync(future, measurement, target);
        return future;
    }

    private void lookupAsync(final CompletableFuture<PeerMetric.LookupMeasurement> future, final PeerMetric.LookupMeasurement measurement, final Key target) {

        if (measurement.isDone()) {
            future.complete(measurement);
        }
        else {
            final CompletableFuture<PeerReference> ff = lookupAsynchronous(target, measurement);
            ff.whenCompleteAsync((PeerReference result, Throwable error) -> {
                if (!ff.isCompletedExceptionally()) {
                    measurement.stop(result);
                    measurement.incrementRetryCount();
                    lookupAsync(future, measurement, target);
                }
                else {
                    if (measurement.hasRetryThresholdReached()) {
                        measurement.stop(new RPCException(error));
                    }
                    measurement.incrementRetryCount();
                    lookupAsync(future, measurement, target);
                }
            });
        }
    }

    public DisseminationStrategy getDisseminationStrategy() {

        return maintainer.getDisseminationStrategy();
    }

    public PeerMaintainer getPeerMaintainer() {

        return maintainer;
    }

    private PeerReference lookup(final Key target, final PeerMetric.LookupMeasurement measurement) throws RPCException {

        PeerReference current_hop = self;
        PeerRemote current_hop_remote = this;
        PeerReference next_hop = current_hop_remote.nextHop(target);
        while (!current_hop.equals(next_hop)) {

            final PeerRemote next_hop_remote = getRemote(next_hop);
            try {
                next_hop = next_hop_remote.nextHop(target);
            }
            catch (final RPCException error) {
                next_hop.setReachable(false);
                current_hop_remote.push(next_hop); // Notify current hop of the broken next hop
                throw error;
            }
            finally {
                if (measurement != null) {
                    measurement.incrementHopCount();
                }
                push(next_hop);
            }
            current_hop = next_hop;
            current_hop_remote = next_hop_remote;
        }

        return next_hop;
    }

    private CompletableFuture<PeerReference> lookupAsynchronous(final Key target, final PeerMetric.LookupMeasurement measurement) {

        final CompletableFuture<PeerReference> async_lookup = new CompletableFuture<>();

        nextHopAsync(async_lookup, target, nextHop(target), self, measurement);
        return async_lookup;
    }

    private void nextHopAsync(final CompletableFuture<PeerReference> async_lookup, final Key target, final PeerReference next_hop, final PeerReference current_hop, final PeerMetric.LookupMeasurement measurement) {

        if (current_hop.equals(next_hop)) {
            async_lookup.complete(next_hop);
        }
        else {

            if (measurement != null && measurement.getHopCount() > 100) {
                async_lookup.complete(nextHop(target));
            }
            else if (!next_hop.equals(self)) {
                final AsynchronousPeerRemote next_hop_asynch_remote = asynchronous_remote_factory.get(next_hop);
                next_hop_asynch_remote.push(self);

                final CompletionStage<PeerReference> future_next_hop = next_hop_asynch_remote.nextHop(target);
                if (measurement != null) {
                    measurement.incrementHopCount();
                }

                future_next_hop.whenComplete((PeerReference result, Throwable error) -> {

                    final boolean successful = error == null;

                    if (successful) {
                        push(next_hop);
                        nextHopAsync(async_lookup, target, result, next_hop, measurement);
                    }
                    else {
                        async_lookup.completeExceptionally(error);
                        next_hop.setReachable(false);
                        push(next_hop);
                        if (!current_hop.equals(self)) {
                            asynchronous_remote_factory.get(current_hop).push(next_hop);
                        }
                    }
                });

            }
            else {
                if (measurement != null) {
                    LOGGER.debug("traversed to itself after {} hops", measurement.getHopCount());
                }
                async_lookup.complete(nextHop(target));
            }
        }
    }

    private void refreshSelfReference() {

        self = new PeerReference(key, getAddress());
    }
}
