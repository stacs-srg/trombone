package uk.ac.standrews.cs.trombone.trombone;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.mashti.jetson.Server;
import org.mashti.jetson.ServerFactory;
import org.mashti.jetson.exception.RPCException;
import org.mashti.jetson.lean.LeanServerFactory;
import uk.ac.standrews.cs.trombone.trombone.codec.PeerCodecs;
import uk.ac.standrews.cs.trombone.trombone.gossip.OpportunisticGossip;
import uk.ac.standrews.cs.trombone.trombone.key.Key;
import uk.ac.standrews.cs.trombone.trombone.selector.Selector;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class Peer implements PeerRemote {

    private static final String EXPOSURE_PROPERTY_NAME = "exposure";
    private static final String JOIN_PROPERTY_NAME = "arrival";
    private static final ServerFactory<PeerRemote> SERVER_FACTORY = new LeanServerFactory<PeerRemote>(PeerRemote.class, PeerCodecs.INSTANCE);
    private final PeerState state;
    private final Key key;
    private final PeerRemoteFactory remote_factory;
    private final Server server;
    private final PropertyChangeSupport property_change_support;
    private final AtomicBoolean joined = new AtomicBoolean();
    private final PeerMetric metric;
    private volatile PeerReference self;
    private volatile List<OpportunisticGossip> opportunistic_gossip;
    private volatile Maintenance maintenance;

    public Peer(final InetSocketAddress address, final Key key) {

        this.key = key;
        state = new PeerState(key);

        metric = new PeerMetric(null);
        remote_factory = new PeerRemoteFactory(this, PeerFactory.CLIENT_FACTORY);
        server = SERVER_FACTORY.createServer(this);
        server.setBindAddress(address);
        server.setWrittenByteCountListenner(metric);
        refreshSelfReference();
        property_change_support = new PropertyChangeSupport(this);
    }

    public void setMaintenance(final Maintenance maintenance) {

        this.maintenance = maintenance;
    }

    public synchronized boolean expose() throws IOException {

        final boolean exposed = server.expose();
        if (exposed) {
            property_change_support.firePropertyChange(EXPOSURE_PROPERTY_NAME, false, true);
            refreshSelfReference();
        }
        return exposed;
    }

    public synchronized boolean unexpose() throws IOException {

        final boolean unexposed = server.unexpose();
        if (unexposed) {
            property_change_support.firePropertyChange(EXPOSURE_PROPERTY_NAME, true, false);
            setJoined(false);
        }
        return unexposed;
    }

    @Override
    public Key getKey() {

        return key;
    }

    @Override
    public synchronized void join(final PeerReference member) throws RPCException {

        if (!hasJoined()) {
            final PeerRemote remote_member = getRemote(member);
            final PeerReference successor = remote_member.lookup(key);
            push(member);
            remote_member.push(self);
            getRemote(successor).push(self);
            push(successor);
            setJoined(true);
        }
    }

    @Override
    public void push(final PeerReference reference) {

        state.add(reference);
    }

    @Override
    public void push(final PeerReference[] references) {

        for (PeerReference reference : references) {
            push(reference);
        }
    }

    @Override
    public PeerReference[] pull(final Selector selector, final int size) throws RPCException {

        return selector.select(this, size);
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
            PeerReference ceiling = state.ceiling(target);
            while (ceiling != null && !ceiling.isReachable()) {
                ceiling = state.lower(ceiling.getKey());
            }
            next_hop = ceiling != null ? ceiling : self;
        }

        return next_hop;
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

    public void addExposureChangeListener(final PropertyChangeListener listener) {

        property_change_support.addPropertyChangeListener(EXPOSURE_PROPERTY_NAME, listener);
    }

    public void addMembershipChangeListener(final PropertyChangeListener listener) {

        property_change_support.addPropertyChangeListener(JOIN_PROPERTY_NAME, listener);
    }

    public boolean isExposed() {

        return server.isExposed();
    }

    public PeerReference lookup(final Key target, int retry_count) throws RPCException {

        final PeerMetric.LookupMeasurement measurement = metric.newLookupMeasurement(retry_count);
        do {
            measurement.incrementRetryCount();
            try {
                final PeerReference result = lookup(target, measurement);
                measurement.stop(result);
            }
            catch (final RPCException e) {
                if (measurement.hasRetryThresholdReached()) {
                    measurement.stop(e);
                }
            }
        }
        while (!Thread.currentThread().isInterrupted() && !measurement.isDone());

        if (measurement.isDoneInError()) { throw measurement.getError(); }
        return measurement.getResult();
    }

    public PeerMetric getPeerMetric() {

        return metric;
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
                current_hop_remote.push(next_hop); // Notify current hop of the broken next hop
                throw error;
            }
            finally {
                if (measurement != null) {
                    measurement.incrementHopCount();
                }
            }
            current_hop = next_hop;
            current_hop_remote = next_hop_remote;
        }
        return next_hop;
    }

    private void setJoined(boolean join) {

        if (joined.compareAndSet(!join, join)) {
            property_change_support.firePropertyChange(JOIN_PROPERTY_NAME, !join, join);
        }
    }

    private boolean hasJoined() {

        return joined.get();
    }

    private void refreshSelfReference() {

        self = new PeerReference(key, getAddress());
    }

    List<OpportunisticGossip> getOpportunisticGossip() {

        return opportunistic_gossip;
    }

    void setOpportunisticGossip(List<OpportunisticGossip> opportunistic_gossip) {

        this.opportunistic_gossip = opportunistic_gossip;
    }
}
