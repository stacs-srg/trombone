package uk.ac.standrews.cs.trombone.core;

import com.google.common.util.concurrent.ListeningExecutorService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;
import org.mashti.jetson.FutureResponse;
import org.mashti.jetson.Server;
import org.mashti.jetson.ServerFactory;
import org.mashti.jetson.exception.RPCException;
import org.mashti.jetson.lean.LeanServerFactory;
import uk.ac.standrews.cs.trombone.core.key.Key;
import uk.ac.standrews.cs.trombone.core.rpc.codec.PeerCodecs;
import uk.ac.standrews.cs.trombone.core.selector.Selector;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class Peer implements PeerRemote {

    private static final String EXPOSURE_PROPERTY_NAME = "exposure";
    private static final String JOINED_PROPERTY_NAME = "joined";
    private static final ServerFactory<PeerRemote> SERVER_FACTORY = new LeanServerFactory<PeerRemote>(PeerRemote.class, PeerCodecs.INSTANCE) {

        @Override
        public Server createServer(final PeerRemote service) {

            return new MyServer(server_bootstrap, service, request_executor) {

            };
        }

        class MyServer extends Server {

            private final Peer peer;
            MyServer(final ServerBootstrap server_bootstrap, final Object service, final ListeningExecutorService executor) {

                super(server_bootstrap, service, executor);
                
                peer = (Peer) service;
            }

            @Override
            protected void handle(final ChannelHandlerContext context, final FutureResponse future_response) {
                  // peer.getPeerMetric().notifyMethod(future_response.getMethod()); // record frequency of called methods
                super.handle(context, future_response);
            }
        }
    };
    private final PeerState state;
    private final Key key;
    private final PeerRemoteFactory remote_factory;
    private final Server server;
    private final PropertyChangeSupport property_change_support;
    private final AtomicBoolean joined = new AtomicBoolean();
    private final PeerMetric metric;
    private final Maintenance maintenance;
    private volatile PeerReference self;

    Peer(final Key key) {

        this(new InetSocketAddress(0), key);
    }

    Peer(final InetSocketAddress address, final Key key) {

        this.key = key;
        state = new PeerState(key);
        maintenance = new Maintenance(this);
        property_change_support = new PropertyChangeSupport(this);
        metric = new PeerMetric(this);
        remote_factory = new PeerRemoteFactory(this);
        server = SERVER_FACTORY.createServer(this);
        server.setBindAddress(address);
        server.setWrittenByteCountListener(metric);
        refreshSelfReference();
    }

    public synchronized boolean expose() throws IOException {

        final boolean exposed = server.expose();
        if (exposed) {
            refreshSelfReference();
            maintenance.start();
            property_change_support.firePropertyChange(EXPOSURE_PROPERTY_NAME, false, true);
        }
        return exposed;
    }

    public synchronized boolean unexpose() throws IOException {

        final boolean unexposed = server.unexpose();
        if (unexposed) {
            property_change_support.firePropertyChange(EXPOSURE_PROPERTY_NAME, true, false);
            setJoined(false);
            maintenance.stop();
        }
        return unexposed;
    }

    @Override
    public Key getKey() {

        return key;
    }

    @Override
    public synchronized void join(final PeerReference member) throws RPCException {

        if (isExposed() && !hasJoined() && member != null) {

            final PeerRemote member_remote = getRemote(member);
            final PeerReference successor = member_remote.lookup(key);
            final PeerRemote successor_remote = getRemote(successor);

            // upon getting the remote proxy the member and successor will be added to state.

            member_remote.push(self);
            successor_remote.push(self);
            setJoined(true);
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

    public void addMembershipChangeListener(final PropertyChangeListener listener) {

        property_change_support.addPropertyChangeListener(JOINED_PROPERTY_NAME, listener);
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

    public Maintenance getMaintenance() {

        return maintenance;
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
            property_change_support.firePropertyChange(JOINED_PROPERTY_NAME, !join, join);
        }
    }

    private boolean hasJoined() {

        return joined.get();
    }

    private void refreshSelfReference() {

        self = new PeerReference(key, getAddress());
    }
}
