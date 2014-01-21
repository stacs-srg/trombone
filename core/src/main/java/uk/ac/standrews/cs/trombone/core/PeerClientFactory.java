package uk.ac.standrews.cs.trombone.core;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import io.netty.channel.Channel;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import org.mashti.jetson.ChannelPool;
import org.mashti.jetson.Client;
import org.mashti.jetson.FutureResponse;
import org.mashti.jetson.exception.RPCException;
import org.mashti.jetson.exception.TransportException;
import org.mashti.jetson.lean.LeanClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.trombone.core.rpc.codec.PeerCodecs;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
class PeerClientFactory extends LeanClientFactory<PeerRemote> {

    private static final ConcurrentHashMap<InetSocketAddress, ChannelPool> channel_pool_map = new ConcurrentHashMap<InetSocketAddress, ChannelPool>();
    private static final Logger LOGGER = LoggerFactory.getLogger(PeerClientFactory.class);
    private final Peer peer;
    private final PeerState peer_state;
    private final PeerMetric peer_metric;
    private final Maintenance peer_maintenance;

    PeerClientFactory(final Peer peer) {

        super(PeerRemote.class, PeerCodecs.INSTANCE);
        this.peer = peer;
        peer_state = peer.getPeerState();
        peer_metric = peer.getPeerMetric();
        peer_maintenance = peer.getMaintenance();
    }

    PeerRemote get(final PeerReference reference) {

        final PeerRemote remote = get(reference.getAddress());
        final PeerClient handler = (PeerClient) Proxy.getInvocationHandler(remote);
        handler.reference = peer_state.getInternalReference(reference);
        return remote;
    }

    @Override
    protected Client createClient(final InetSocketAddress address) {

        return new PeerClient(address, getChannelPool(address));
    }

    public class PeerClient extends Client {

        volatile InternalPeerReference reference;

        protected PeerClient(final InetSocketAddress address, final ChannelPool pool) {

            super(address, dispatch, pool);
            setWrittenByteCountListener(peer_metric);
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] params) throws Throwable {

            if (!peer.isExposed()) {
                LOGGER.warn("remote procedure {} was invoked while the peer is unexposed", method);
                throw new RPCException("peer is unexposed; cannot invoke remote procedure");
            }
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

            for (DisseminationStrategy strategy : peer_maintenance.getDisseminationStrategies()) {
                if (strategy.isOpportunistic() && strategy.recipientsContain(peer, reference)) {
                    final FutureResponse future_dissemination = newFutureResponse(strategy.getMethod(), strategy.getArguments(peer));
                    channel.write(future_dissemination);
                }
            }
            super.beforeFlush(channel, future_response);
        }
    }
}
