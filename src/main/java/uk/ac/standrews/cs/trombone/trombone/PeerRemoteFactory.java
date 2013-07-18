package uk.ac.standrews.cs.trombone.trombone;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import org.mashti.jetson.ChannelPool;
import org.mashti.jetson.Client;
import org.mashti.jetson.FutureResponse;
import org.mashti.jetson.exception.RPCException;
import org.mashti.jetson.lean.LeanClientFactory;
import uk.ac.standrews.cs.trombone.trombone.codec.PeerCodecs;
import uk.ac.standrews.cs.trombone.trombone.gossip.OpportunisticGossip;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class PeerRemoteFactory extends LeanClientFactory<PeerRemote> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PeerRemoteFactory.class);
    private final Peer peer;
    private final LeanClientFactory<PeerRemote> parent;
    private final PeerState peer_state;
    private final PeerMetric peer_metric;

    PeerRemoteFactory(final Peer peer, LeanClientFactory<PeerRemote> parent) {

        super(PeerRemote.class, PeerCodecs.INSTANCE);
        this.peer = peer;
        this.parent = parent;
        peer_state = peer.getPeerState();
        peer_metric = peer.getPeerMetric();
    }

    PeerRemote get(final PeerReference reference) {

        final PeerRemote remote = get(reference.getAddress());
        final PeerClient invocationHandler = (PeerClient) Proxy.getInvocationHandler(remote);
        invocationHandler.reference = peer_state.getInternalReference(reference);
        return remote;
    }

    @Override
    protected Client createClient(final InetSocketAddress address) {

        return new PeerClient(address, parent.getChannelPool(address));
    }

    public class PeerClient extends Client {

        private volatile InternalPeerReference reference;

        protected PeerClient(final InetSocketAddress address, final ChannelPool pool) {

            super(address, dispatch, pool);
            setWrittenByteCountListenner(peer_metric);
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

                    //TODO the filure may be due to internal error; the reference might not be unreachable.
                    LOGGER.info("failure occured on future", t);
                    reference.setReachable(false);
                }
            });
            return future_response;
        }

        @Override
        protected List<FutureResponse> getExtraRequrests() throws RPCException {

            final List<FutureResponse> responses = new ArrayList<FutureResponse>();
            final List<OpportunisticGossip> gossips = peer.getOpportunisticGossip();
            if (gossips != null) {
                for (OpportunisticGossip gossip : gossips) {
                    final FutureResponse future_gossip = gossip.get(this);
                    responses.add(future_gossip);
                }
            }
            return responses;
        }

        @Override
        public FutureResponse writeRequest(final FutureResponse future_response) throws RPCException {

            return super.writeRequest(future_response);
        }
    }
}
