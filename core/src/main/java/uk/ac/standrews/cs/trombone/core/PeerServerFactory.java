package uk.ac.standrews.cs.trombone.core;

import com.google.common.util.concurrent.ListeningExecutorService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import org.mashti.jetson.FutureResponse;
import org.mashti.jetson.Server;
import org.mashti.jetson.lean.LeanServerFactory;
import uk.ac.standrews.cs.trombone.core.rpc.codec.PeerCodecs;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
class PeerServerFactory extends LeanServerFactory<PeerRemote> {

    public PeerServerFactory() {

        super(PeerRemote.class, PeerCodecs.INSTANCE);
    }

    @Override
    public Server createServer(final PeerRemote service) {

        return new MyServer(server_bootstrap, service, request_executor) {

        };
    }

    static class MyServer extends Server {

        private final Peer peer;

        MyServer(final ServerBootstrap server_bootstrap, final Object service, final ListeningExecutorService executor) {

            super(server_bootstrap, service, executor);

            peer = (Peer) service;
        }

        @Override
        protected void handle(final ChannelHandlerContext context, final FutureResponse future_response) {
            //                peer.getPeerMetric().notifyMethod(future_response.getMethod()); // record frequency of called methods
            super.handle(context, future_response);
        }
    }
}
