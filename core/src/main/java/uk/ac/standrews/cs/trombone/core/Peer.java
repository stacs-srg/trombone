package uk.ac.standrews.cs.trombone.core;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentSkipListSet;
import org.mashti.jetson.Server;
import org.mashti.jetson.ServerFactory;
import org.mashti.jetson.exception.RPCException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.trombone.core.key.Key;
import uk.ac.standrews.cs.trombone.core.selector.Selector;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class Peer implements PeerRemote {

    private static final Logger LOGGER = LoggerFactory.getLogger(Peer.class);
    private static final String EXPOSURE_PROPERTY_NAME = "exposure";
    private static final ServerFactory<PeerRemote> SERVER_FACTORY = new PeerServerFactory();
    private final PeerState state;
    private final PeerClientFactory remote_factory;
    private final Key key;
    private final Server server;
    private final PropertyChangeSupport property_change_support;
    private final PeerMetric metric;
    private final Maintenance.PeerMaintainer maintainer;
    private volatile PeerReference self;

    public static final ConcurrentSkipListSet<Integer> EXPOSED_PORTS = new ConcurrentSkipListSet<>();

    Peer(final Key key) {

        this(new InetSocketAddress(0), key);
    }

    Peer(final InetSocketAddress address, final Key key) {

        this(address, key, PeerFactory.DEFAULT_PEER_CONFIGURATION);
    }

    Peer(final InetSocketAddress address, final Key key, PeerConfiguration configuration) {

        this.key = key;
        property_change_support = new PropertyChangeSupport(this);

        state = new PeerState(key);
        metric = new PeerMetric(this);
        maintainer = configuration.getMaintenance().maintain(this);
        server = SERVER_FACTORY.createServer(this);
        server.setBindAddress(address);
        server.setWrittenByteCountListener(metric);
        remote_factory = new PeerClientFactory(this, configuration.getSyntheticDelay());
        refreshSelfReference();
    }

    public synchronized boolean expose() throws IOException {

        final boolean exposed = server.expose();
        if (exposed) {
            EXPOSED_PORTS.add(getAddress().getPort());
            refreshSelfReference();
            property_change_support.firePropertyChange(EXPOSURE_PROPERTY_NAME, false, true);
            LOGGER.trace("exposed {} on {}", key, getAddress());

        }
        return exposed;
    }

    public synchronized boolean unexpose() throws IOException {

        final boolean unexposed = server.unexpose();
        if (unexposed) {
            EXPOSED_PORTS.remove(getAddress().getPort());
            property_change_support.firePropertyChange(EXPOSURE_PROPERTY_NAME, true, false);
        }
        return unexposed;
    }

    @Override
    public Key getKey() {

        return key;
    }

    @Override
    public synchronized void join(final PeerReference member) throws RPCException {

        if (isExposed() && member != null) {

            push(member);
            getRemote(member).push(self);
        }
    }

    @Override
    public void push(final PeerReference... references) {

        if (references != null) {
            for (PeerReference reference : references) {
                state.add(reference);
            }
        }
    }

    @Override
    public PeerReference[] pull(final Selector selector) throws RPCException {

        return selector.select(this);
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

    public DisseminationStrategy getDisseminationStrategy() {

        return maintainer.getDisseminationStrategy();
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

    private void refreshSelfReference() {

        self = new PeerReference(key, getAddress());
    }
}
