package uk.ac.standrews.cs.trombone.core;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import org.mashti.jetson.ChannelPool;
import org.mashti.jetson.Client;
import org.mashti.jetson.ClientFactory;
import org.mashti.jetson.FutureResponse;
import org.mashti.jetson.exception.RPCException;
import org.mashti.jetson.exception.TransportException;
import org.mashti.jetson.lean.LeanClientChannelInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.trombone.core.rpc.codec.PeerCodecs;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
class PeerClientFactory extends ClientFactory<PeerRemote> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PeerClientFactory.class);
    private static final Bootstrap BOOTSTRAP = new Bootstrap();
    private static final ChannelPool CHANNEL_POOL = new ChannelPool(BOOTSTRAP);

    static {
        //        BOOTSTRAP.group(new NioEventLoopGroup(100));
        BOOTSTRAP.group(PeerServerFactory.child_event_loop);
        BOOTSTRAP.channel(NioSocketChannel.class);
        BOOTSTRAP.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5_000);
        BOOTSTRAP.option(ChannelOption.TCP_NODELAY, true);
        BOOTSTRAP.handler(new LeanClientChannelInitializer(PeerRemote.class, PeerCodecs.INSTANCE));

        CHANNEL_POOL.setTestOnBorrow(true);
        CHANNEL_POOL.setTestOnReturn(true);
        CHANNEL_POOL.setMaxTotalPerKey(400);
        CHANNEL_POOL.setBlockWhenExhausted(false);
    }

    private final Peer peer;
    private final SyntheticDelay synthetic_delay;
    private final PeerState peer_state;
    private final PeerMetric peer_metric;
    private final Maintenance peer_maintenance;
    private final InetAddress peer_address;

    PeerClientFactory(final Peer peer, final SyntheticDelay synthetic_delay) {

        super(PeerRemote.class, BOOTSTRAP, CHANNEL_POOL);
        this.peer = peer;
        this.synthetic_delay = synthetic_delay;
        peer_state = peer.getPeerState();
        peer_metric = peer.getPeerMetric();
        peer_maintenance = peer.getMaintenance();
        peer_address = peer.getAddress().getAddress();
    }

    PeerRemote get(final PeerReference reference) {

        final PeerRemote remote = get(reference.getAddress());
        final PeerClient handler = (PeerClient) Proxy.getInvocationHandler(remote);
        handler.reference = peer_state.getInternalReference(reference);
        return remote;
    }

    @Override
    protected Client createClient(final InetSocketAddress address) {

        return new PeerClient(address, CHANNEL_POOL);
    }

    public class PeerClient extends Client {

        private final InetAddress client_address;
        volatile InternalPeerReference reference;

        protected PeerClient(final InetSocketAddress address, final ChannelPool pool) {

            super(address, dispatch, pool);
            setWrittenByteCountListener(peer_metric);
            client_address = address.getAddress();
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] params) throws Throwable {

            if (!peer.isExposed()) {
                LOGGER.debug("remote procedure {} was invoked while the peer is unexposed", method);
                throw new RPCException("peer is unexposed; cannot invoke remote procedure");
            }

            synthetic_delay.apply(peer_address, client_address);
            return super.invoke(proxy, method, params);
        }

        @Override
        public FutureResponse newFutureResponse(final Method method, final Object[] arguments) {

            final FutureResponse future_response = super.newFutureResponse(method, arguments);

            Futures.addCallback(future_response, new FutureCallback<Object>() {

                @Override
                public void onSuccess(final Object result) {

                    reference.seen();

                    if (result instanceof PeerReference) {
                        peer.push((PeerReference) result);
                    }
                    if (result instanceof PeerReference[]) {
                        peer.push((PeerReference[]) result);
                    }
                }

                @Override
                public void onFailure(final Throwable t) {

                    LOGGER.debug("failure occurred on future", t);
                    if (t instanceof TransportException) {
                        reference.setReachable(false);
                    }
                }
            });
            return future_response;
        }

        @Override
        protected void beforeFlush(final Channel channel, final FutureResponse future_response) throws RPCException {

            final DisseminationStrategy strategy = peer_maintenance.getDisseminationStrategy();
            if (strategy != null) {
                for (DisseminationStrategy.Action action : strategy) {
                    if (action.isOpportunistic() && action.recipientsContain(peer, reference)) {
                        final FutureResponse future_dissemination = newFutureResponse(action.getMethod(), action.getArguments(peer));
                        channel.write(future_dissemination);
                    }
                }
            }
            super.beforeFlush(channel, future_response);
        }
    }
}
