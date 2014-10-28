package uk.ac.standrews.cs.trombone.core;

import io.netty.bootstrap.Bootstrap;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.mashti.jetson.ChannelFuturePool;
import org.mashti.jetson.Client;
import org.mashti.jetson.ClientFactory;
import org.mashti.jetson.FutureResponse;
import org.mashti.jetson.exception.RPCException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.trombone.core.rpc.FuturePeerResponse;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class PeerClientWithoutPiggybackFactory extends ClientFactory<AsynchronousPeerRemote> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PeerClientWithoutPiggybackFactory.class);

    private final Peer peer;
    private final SyntheticDelay synthetic_delay;
    private final PeerMetric peer_metric;
    private final InetAddress peer_address;

    PeerClientWithoutPiggybackFactory(final Peer peer, final SyntheticDelay synthetic_delay) {

        super(AsynchronousPeerRemote.class, PeerClientFactory.DISPATCH, PeerClientFactory.BOOTSTRAP);
        this.peer = peer;
        this.synthetic_delay = synthetic_delay;
        peer_metric = peer.getPeerMetric();
        peer_address = peer.getAddress()
                .getAddress();

    }

    @Override
    protected ChannelFuturePool constructChannelPool(final Bootstrap bootstrap) {

        return PeerClientFactory.CHANNEL_POOL;
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

            super(address, dispatch, PeerClientFactory.CHANNEL_POOL);
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

            final FuturePeerResponse<?> future_response = new FuturePeerResponse(peer.getSelfReference(), method, arguments);
            future_response.setWrittenByteCountListener(written_byte_count_listener);
            if (peer.getConfiguration()
                    .isLearnFromCommunicationsEnabled()) {
                future_response.whenComplete((Object result, Throwable error) -> {
                    if (!future_response.isCompletedExceptionally()) {

                        peer.getPeerState()
                                .add(reference);

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
                        peer.getPeerState()
                                .remove(reference);
                        peer_metric.notifyRPCError(reference, error);
                        LOGGER.debug("failure occurred on future", error);
                    }

                });
            }

            return future_response;
        }
    }
}
