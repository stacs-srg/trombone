package uk.ac.standrews.cs.trombone.core;

import com.google.common.util.concurrent.Futures;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.GenericFutureListener;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.reflect.MethodUtils;
import org.mashti.jetson.ChannelFuturePool;
import org.mashti.jetson.Client;
import org.mashti.jetson.ClientFactory;
import org.mashti.jetson.FutureResponse;
import org.mashti.jetson.exception.RPCException;
import org.mashti.jetson.util.ReflectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class AsynchronousPeerClientFactory extends ClientFactory<PeerRemote> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsynchronousPeerClientFactory.class);

    public static final Method[] SORTED_ASYNC_METHODS = ReflectionUtil.sort(AsynchronousPeerRemote.class.getMethods());
    private final ConcurrentHashMap<InetSocketAddress, AsynchronousPeerRemote> asynchronous_cached_proxy_map = new ConcurrentHashMap<InetSocketAddress, AsynchronousPeerRemote>();

    static {

        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(new Runnable() {

            @Override
            public void run() {

                for (final Map.Entry<InetSocketAddress, ChannelFuture> next : PeerClientFactory.CHANNEL_POOL.getPooledEntries()) {
                    try {
                        next.getValue().channel().flush();
                    }
                    catch (Exception e) {
                        LOGGER.error("FAILED TO FLUSH {} due to {}", next.getKey(), e);
                    }
                }
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    private final Peer peer;
    private final SyntheticDelay synthetic_delay;
    private final PeerState peer_state;
    private final PeerMetric peer_metric;
    private final InetAddress peer_address;
    private final ClassLoader class_loader;
    private final Class<?>[] interfaces;
    private final Method[] asynch_dispatch;

    public static void shutdownPeerClientFactory() {

        try {
            PeerClientFactory.BOOTSTRAP.group().shutdownGracefully().sync();
        }
        catch (InterruptedException e) {
            LOGGER.warn("interrupted while shutting down peer client factory", e);
        }

        PeerClientFactory.CHANNEL_POOL.clear();
    }

    AsynchronousPeerClientFactory(final Peer peer, final SyntheticDelay synthetic_delay) {

        super(PeerRemote.class, PeerClientFactory.DISPATCH, PeerClientFactory.BOOTSTRAP);
        this.peer = peer;
        this.synthetic_delay = synthetic_delay;
        peer_state = peer.getPeerState();
        peer_metric = peer.getPeerMetric();
        peer_address = peer.getAddress().getAddress();
        class_loader = ClassLoader.getSystemClassLoader();
        interfaces = new Class<?>[] {AsynchronousPeerRemote.class};
        asynch_dispatch = SORTED_ASYNC_METHODS;
    }

    @Override
    protected ChannelFuturePool constructChannelPool(final Bootstrap bootstrap) {

        return PeerClientFactory.CHANNEL_POOL;
    }

    AsynchronousPeerRemote get(final PeerReference reference) {

        final InetSocketAddress address = reference.getAddress();
        final AsynchronousPeerRemote remote = getAsynchronous(address);
        final AsynchronousPeerClient handler = (AsynchronousPeerClient) Proxy.getInvocationHandler(remote);
        handler.reference = peer_state.getInternalReference(reference);

        return remote;
    }

    AsynchronousPeerRemote getAsynchronous(InetSocketAddress address) {

        if (asynchronous_cached_proxy_map.containsKey(address)) { return asynchronous_cached_proxy_map.get(address); }
        final Client handler = createClient(address);
        final AsynchronousPeerRemote new_proxy = createAsynchronousProxy(handler);
        final AsynchronousPeerRemote existing_proxy = asynchronous_cached_proxy_map.putIfAbsent(address, new_proxy);
        return existing_proxy != null ? existing_proxy : new_proxy;
    }

    AsynchronousPeerRemote createAsynchronousProxy(final Client handler) {

        return (AsynchronousPeerRemote) Proxy.newProxyInstance(class_loader, interfaces, handler);
    }

    @Override
    protected Client createClient(final InetSocketAddress address) {

        return new AsynchronousPeerClient(address);
    }

    public class AsynchronousPeerClient extends Client {

        private final InetAddress client_address;
        volatile InternalPeerReference reference;

        protected AsynchronousPeerClient(final InetSocketAddress address) {

            super(address, dispatch, PeerClientFactory.CHANNEL_POOL);
            setWrittenByteCountListener(peer_metric);
            client_address = address.getAddress();
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] params) throws Throwable {

            if (dispatchContains(method)) {

                if (!peer.isExposed()) {
                    LOGGER.debug("remote procedure {} was invoked while the peer is unexposed", method);
                    return Futures.immediateFailedFuture(new RPCException("peer is unexposed; cannot invoke remote procedure"));
                }

                final Method matching_Method = MethodUtils.getMatchingAccessibleMethod(PeerRemote.class, method.getName(), method.getParameterTypes());
                if (matching_Method == null) {
                    LOGGER.error("NO MATCHING METHOD");
                }

                return writeRequest(newFutureResponse(matching_Method, params));
            }
            else {
                LOGGER.error("method {} was not found in dispatch; executing method on proxy object", method);
                return method.invoke(this, params);
            }
        }

        protected boolean dispatchContains(final Method target) {

            for (final Method method : asynch_dispatch) {
                if (method.equals(target)) { return true; }
            }
            return false;
        }

        protected FutureResponse writeRequest(final FutureResponse future_response) {

            final ChannelFuture channel_future = channel_pool.get(address);
            Maintenance.SCHEDULER.schedule(new RequestWriter(future_response, channel_future), synthetic_delay.get(peer_address, client_address), TimeUnit.NANOSECONDS);
            return future_response;
        }

        @Override
        public FutureResponse newFutureResponse(final Method method, final Object[] arguments) {

            final FutureResponse future_response = super.newFutureResponse(method, arguments);

            Futures.addCallback(future_response, new FutureCallback(), Maintenance.SCHEDULER);
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
                    }
                }
            }
            super.beforeFlush(channel, future_response);
        }

        private class FutureCallback implements com.google.common.util.concurrent.FutureCallback<Object> {

            @Override
            public void onSuccess(final Object result) {

                if (reference != null) {
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
            }

            @Override
            public void onFailure(final Throwable t) {

                if (reference != null) {
                    peer_metric.notifyRPCError(t);
                    LOGGER.debug("failure occurred on future ", t);
                    reference.seen(false);
                }
            }
        }

        private class RequestWriter implements Runnable {

            private final FutureResponse future_response;
            private final ChannelFuture channel_future;

            public RequestWriter(final FutureResponse future_response, final ChannelFuture channel_future) {

                this.future_response = future_response;
                this.channel_future = channel_future;
            }

            @Override
            public void run() {

                final GenericFutureListener<ChannelFuture> listener = new AsynchWriteRequestListener();
                channel_future.addListener(listener);
            }

            private class AsynchWriteRequestListener implements GenericFutureListener<ChannelFuture> {

                @Override
                public void operationComplete(final ChannelFuture future) throws Exception {

                    if (!future_response.isDone()) {

                        if (future.isSuccess()) {

                            final Channel channel = channel_future.channel();
                            final ChannelFuture write = channel.write(future_response);
                            write.addListener(new ExceptionListener(future_response));
                            beforeFlush(channel, future_response);
                        }
                        else {
                            setException(future.cause(), future_response);
                        }
                    }
                }
            }
        }
    }
}
