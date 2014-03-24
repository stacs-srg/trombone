package uk.ac.standrews.cs.trombone.core;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.mashti.gauge.Rate;
import org.mashti.jetson.ChannelFuturePool;
import org.mashti.jetson.Client;
import org.mashti.jetson.ClientFactory;
import org.mashti.jetson.FutureResponse;
import org.mashti.jetson.exception.RPCException;
import org.mashti.jetson.lean.LeanClientChannelInitializer;
import org.mashti.jetson.util.NamedThreadFactory;
import org.mashti.jetson.util.ReflectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.trombone.core.rpc.codec.PeerCodecs;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class PeerClientFactory extends ClientFactory<PeerRemote> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PeerClientFactory.class);
    static final Bootstrap BOOTSTRAP = new Bootstrap();
    static final ChannelFuturePool CHANNEL_POOL = new ChannelFuturePool(BOOTSTRAP);
    private static final Rate rate = new Rate();
    private static final Rate error_rate = new Rate();
    private static final Rate succ_rate = new Rate();

    static {
        final NioEventLoopGroup child_event_loop = new NioEventLoopGroup(50, new NamedThreadFactory("client_event_loop_"));
        BOOTSTRAP.group(child_event_loop);
        BOOTSTRAP.channel(NioSocketChannel.class);
        BOOTSTRAP.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30_000);
        BOOTSTRAP.option(ChannelOption.TCP_NODELAY, true);
        BOOTSTRAP.handler(new LeanClientChannelInitializer(PeerRemote.class, PeerCodecs.INSTANCE));
        BOOTSTRAP.option(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, 32 * 1024);
        BOOTSTRAP.option(ChannelOption.WRITE_BUFFER_LOW_WATER_MARK, 8 * 1024);
        BOOTSTRAP.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

        CHANNEL_POOL.setMaxPooledObjectAgeInMillis(2_000);

        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(new Runnable() {

            @Override
            public void run() {

                LOGGER.info("call rate: {} ", rate.getRate());
                LOGGER.info("error rate: {}", error_rate.getRate());
                LOGGER.info("succ rate: {}", succ_rate.getRate());
            }
        }, 20, 10, TimeUnit.SECONDS);
    }

    static final Method[] DISPATCH = ReflectionUtil.sort(PeerRemote.class.getMethods());

    private final Peer peer;
    private final SyntheticDelay synthetic_delay;
    private final PeerState peer_state;
    private final PeerMetric peer_metric;
    private final InetAddress peer_address;

    public static void shutdownPeerClientFactory() {

        try {
            BOOTSTRAP.group().shutdownGracefully().sync();
        }
        catch (InterruptedException e) {
            LOGGER.warn("interrupted while shutting down peer client factory", e);
        }

        CHANNEL_POOL.clear();
    }

    PeerClientFactory(final Peer peer, final SyntheticDelay synthetic_delay) {

        super(PeerRemote.class, DISPATCH, BOOTSTRAP);
        this.peer = peer;
        this.synthetic_delay = synthetic_delay;
        peer_state = peer.getPeerState();
        peer_metric = peer.getPeerMetric();
        peer_address = peer.getAddress().getAddress();
    }

    @Override
    protected ChannelFuturePool constructChannelPool(final Bootstrap bootstrap) {

        return CHANNEL_POOL;
    }

    PeerRemote get(final PeerReference reference) {

        final InetSocketAddress address = reference.getAddress();
        final PeerRemote remote = get(address);
        final PeerClient handler = (PeerClient) Proxy.getInvocationHandler(remote);
        handler.reference = peer_state.getInternalReference(reference);

        return remote;
    }

    @Override
    protected Client createClient(final InetSocketAddress address) {

        return new PeerClient(address);
    }

    public class PeerClient extends Client {

        private final InetAddress client_address;
        volatile InternalPeerReference reference;

        protected PeerClient(final InetSocketAddress address) {

            super(address, dispatch, CHANNEL_POOL);
            setWrittenByteCountListener(peer_metric);
            client_address = address.getAddress();
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] params) throws Throwable {

            if (!peer.isExposed()) {
                LOGGER.debug("remote procedure {} was invoked while the peer is unexposed", method);
                throw new RPCException("peer is unexposed; cannot invoke remote procedure");
            }

            try {
                TimeUnit.NANOSECONDS.sleep(synthetic_delay.get(peer_address, client_address));
            }
            catch (InterruptedException e) {
                throw new RPCException("interrupted while waiting for synthetic delay", e);
            }
            rate.mark();
            return super.invoke(proxy, method, params);
        }

        @Override
        public FutureResponse newFutureResponse(final Method method, final Object[] arguments) {

            final FutureResponse future_response = super.newFutureResponse(method, arguments);
            Futures.addCallback(future_response, new DefaultCallback(), Maintenance.SCHEDULER);
            return future_response;
        }

        @Override
        protected void beforeFlush(final Channel channel, final FutureResponse future_response) throws RPCException {

            final DisseminationStrategy strategy = peer.getDisseminationStrategy();
            if (strategy != null) {
                for (DisseminationStrategy.Action action : strategy) {
                    if (action.isOpportunistic() && action.recipientsContain(peer, reference)) {
                        final FutureResponse future_dissemination = newFutureResponse(action.getMethod(), action.getArguments(peer));
                        channel.write(future_dissemination);
                        rate.mark();
                    }
                }
            }
            super.beforeFlush(channel, future_response);
        }

        private class DefaultCallback implements FutureCallback<Object> {

            @Override
            public void onSuccess(final Object result) {

                succ_rate.mark();
                reference.seen(true);

                if (result instanceof PeerReference) {
                    peer.push((PeerReference) result);
                }

                if (result instanceof List) {
                    List list = (List) result;
                    for (Object element : list) {
                        if (element instanceof PeerReference) {
                            PeerReference peerReference = (PeerReference) element;
                            peer.push(peerReference);
                        }
                    }
                }
            }

            @Override
            public void onFailure(final Throwable t) {

                if (Peer.EXPOSED_PORTS.contains(getAddress().getPort())) {
                    error_rate.mark();
                    LOGGER.debug("failure occurred on future", t);
                }
                reference.seen(false);
            }
        }
    }
}
