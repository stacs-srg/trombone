package uk.ac.standrews.cs.trombone.core;

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
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.reflect.MethodUtils;
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
import uk.ac.standrews.cs.trombone.core.maintenance.DisseminationStrategy;
import uk.ac.standrews.cs.trombone.core.maintenance.Maintenance;
import uk.ac.standrews.cs.trombone.core.maintenance.StrategicMaintenance;
import uk.ac.standrews.cs.trombone.core.rpc.FuturePeerResponse;
import uk.ac.standrews.cs.trombone.core.rpc.codec.PeerCodecs;
import uk.ac.standrews.cs.trombone.core.selector.Selector;
import uk.ac.standrews.cs.trombone.core.state.PeerState;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class PeerClientFactory extends ClientFactory<AsynchronousPeerRemote> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PeerClientFactory.class);

    static final Method PUSH_SINGLE_METHOD = MethodUtils.getAccessibleMethod(AsynchronousPeerRemote.class, "push", PeerReference.class);
    static final Method PUSH_METHOD = MethodUtils.getAccessibleMethod(AsynchronousPeerRemote.class, "push", List.class);
    static final Method PULL_METHOD = MethodUtils.getAccessibleMethod(AsynchronousPeerRemote.class, "pull", Selector.class);
    static final Bootstrap BOOTSTRAP = new Bootstrap();
    static final ChannelFuturePool CHANNEL_POOL = new ChannelFuturePool(BOOTSTRAP);

    static {
        final NioEventLoopGroup child_event_loop = new NioEventLoopGroup(50, new NamedThreadFactory("client_event_loop_"));
        BOOTSTRAP.group(child_event_loop);
        BOOTSTRAP.channel(NioSocketChannel.class);
        BOOTSTRAP.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30_000);
        BOOTSTRAP.option(ChannelOption.TCP_NODELAY, true);
        BOOTSTRAP.handler(new LeanClientChannelInitializer(AsynchronousPeerRemote.class, PeerCodecs.getInstance()));
        BOOTSTRAP.option(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, 32 * 1024);
        BOOTSTRAP.option(ChannelOption.WRITE_BUFFER_LOW_WATER_MARK, 8 * 1024);
        BOOTSTRAP.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

        CHANNEL_POOL.setMaxPooledObjectAgeInMillis(2_000);
    }

    static final Method[] DISPATCH = ReflectionUtil.checkAndSort(AsynchronousPeerRemote.class.getMethods());

    private final Peer peer;
    private final SyntheticDelay synthetic_delay;
    private final PeerState peer_state;
    private final PeerMetric peer_metric;
    private final InetAddress peer_address;

    public static void shutdownPeerClientFactory() {

        try {
            BOOTSTRAP.group()
                    .shutdownGracefully()
                    .sync();
        }
        catch (InterruptedException e) {
            LOGGER.warn("interrupted while shutting down peer client factory", e);
        }

        CHANNEL_POOL.clear();
    }

    PeerClientFactory(final Peer peer, final SyntheticDelay synthetic_delay) {

        super(AsynchronousPeerRemote.class, DISPATCH, BOOTSTRAP);
        this.peer = peer;
        this.synthetic_delay = synthetic_delay;
        peer_state = peer.getPeerState();
        peer_metric = peer.getPeerMetric();
        peer_address = peer.getAddress()
                .getAddress();
    }

    @Override
    protected ChannelFuturePool constructChannelPool(final Bootstrap bootstrap) {

        return CHANNEL_POOL;
    }

    AsynchronousPeerRemote get(final PeerReference reference) {

        final InetSocketAddress address = reference.getAddress();
        final AsynchronousPeerRemote remote = get(address);
        final PeerClient handler = (PeerClient) Proxy.getInvocationHandler(remote);
        handler.reference = reference;

        return remote;
    }

    @Override
    protected Client createClient(final InetSocketAddress address) {

        return new PeerClient(address);
    }

    public class PeerClient extends Client {

        private final InetAddress client_address;
        volatile PeerReference reference;

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

            // FIXME use schedule instead of thread sleep
            try {
                TimeUnit.NANOSECONDS.sleep(synthetic_delay.get(peer_address, client_address, peer.getRandom()));
            }
            catch (InterruptedException e) {
                throw new RPCException("interrupted while waiting for synthetic delay", e);
            }
            return super.invoke(proxy, method, params);
        }

        @Override
        public FutureResponse<?> newFutureResponse(final Method method, final Object[] arguments) {

            final FuturePeerResponse<?> future_response = new FuturePeerResponse(reference, method, arguments);
            future_response.setWrittenByteCountListener(written_byte_count_listener);
            future_response.whenCompleteAsync((Object result, Throwable error) -> {
                if (!future_response.isCompletedExceptionally()) {

                    reference.setReachable(true);

                    if (result instanceof PeerReference) {
                        peer.push((PeerReference) result);
                    }

                    if (result instanceof List) {
                        List<?> list = (List<?>) result;
                        list.stream()
                                .filter(element -> element instanceof PeerReference)
                                .forEach(element -> {
                                    PeerReference peerReference = (PeerReference) element;
                                    peer.push(peerReference);
                                });
                    }
                }
                else {
                    reference.setReachable(false);
                    peer_metric.notifyRPCError(error);
                    LOGGER.debug("failure occurred on future", error);
                }

                peer.push(reference);

            }, peer.getExecutor());

            return future_response;
        }

        @Override
        protected void beforeFlush(final Channel channel, final FutureResponse<?> future_response) throws RPCException {

            final Maintenance maintenance = peer.getMaintenance();
            if (maintenance instanceof StrategicMaintenance) {
                StrategicMaintenance strategicMaintenance = (StrategicMaintenance) maintenance;
                final DisseminationStrategy strategy = strategicMaintenance.getDisseminationStrategy();

                if (strategy != null) {

                    strategy.getActions()
                            .stream()
                            .filter(action -> action.isOpportunistic() && action.recipientsContain(peer, reference))
                            .forEach(action -> {

                                final FutureResponse<?> future_dissemination = newFutureResponse(getMethod(action), action.getArguments(peer));
                                writeToChannel(channel, future_dissemination);
                            });

                }

            }
            super.beforeFlush(channel, future_response);
        }

        public Method getMethod(DisseminationStrategy.Action action) {

            return action.isPush() ? PUSH_METHOD : PULL_METHOD;
        }
    }
}
