package uk.ac.standrews.cs.trombone.core;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.mashti.gauge.Rate;
import org.mashti.jetson.FutureResponse;
import org.mashti.jetson.Server;
import org.mashti.jetson.ServerFactory;
import org.mashti.jetson.util.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.trombone.core.rpc.FuturePeerResponse;
import uk.ac.standrews.cs.trombone.core.rpc.LeanPeerServerChannelInitializer;
import uk.ac.standrews.cs.trombone.core.rpc.codec.PeerCodecs;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class PeerServerFactory extends ServerFactory<AsynchronousPeerRemote> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PeerServerFactory.class);
    private static final ServerBootstrap SERVER_BOOTSTRAP = new ServerBootstrap();

    static final NioEventLoopGroup parent_event_loop;
    static final NioEventLoopGroup child_event_loop;
    static final Rate handling_rate = new Rate();
    static final Rate handled_rate = new Rate();

    static {

        parent_event_loop = new NioEventLoopGroup(50, new NamedThreadFactory("server_parent_event_loop_"));
        child_event_loop = new NioEventLoopGroup(50, new NamedThreadFactory("server_child_event_loop_"));

        SERVER_BOOTSTRAP.group(parent_event_loop, child_event_loop);
        SERVER_BOOTSTRAP.channel(NioServerSocketChannel.class);
        SERVER_BOOTSTRAP.option(ChannelOption.TCP_NODELAY, true);
        SERVER_BOOTSTRAP.childOption(ChannelOption.TCP_NODELAY, true);
        SERVER_BOOTSTRAP.childOption(ChannelOption.SO_KEEPALIVE, true);
        SERVER_BOOTSTRAP.childOption(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, 32 * 1024);
        SERVER_BOOTSTRAP.childOption(ChannelOption.WRITE_BUFFER_LOW_WATER_MARK, 8 * 1024);
        SERVER_BOOTSTRAP.childHandler(new LeanPeerServerChannelInitializer<AsynchronousPeerRemote>(AsynchronousPeerRemote.class, PeerCodecs.getInstance()));
        SERVER_BOOTSTRAP.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
    }

    public static void shutdownPeerServerFactory() {

        try {
            SERVER_BOOTSTRAP.childGroup()
                    .shutdownGracefully()
                    .sync();
            SERVER_BOOTSTRAP.group()
                    .shutdownGracefully()
                    .sync();
        }
        catch (InterruptedException e) {
            LOGGER.warn("interrupted while shutting down peer server factory", e);
        }
    }

    public PeerServerFactory() {

        super(SERVER_BOOTSTRAP);
    }

    @Override
    public Server createServer(final AsynchronousPeerRemote service) {

        return new MyServer(server_bootstrap, service);
    }

    static class MyServer extends Server {

        private final Peer peer;

        MyServer(final ServerBootstrap server_bootstrap, final Object service) {

            super(server_bootstrap, service);
            peer = (Peer) service;
        }

        @Override
        protected void handle(final ChannelHandlerContext context, final FutureResponse<Object> future_response) {

            handling_rate.mark();
            peer.getPeerMetric()
                    .notifyServe(future_response.getMethod()); // record frequency of called methods

            if (future_response instanceof FuturePeerResponse) {
                final FuturePeerResponse<?> future_peer_response = (FuturePeerResponse<?>) future_response;
                final PeerReference correspondent = future_peer_response.getCorrespondent();
                if (correspondent != null) {
                    peer.getPeerState()
                            .add(correspondent);
                }
            }

            MyServer.super.handle(context, future_response);
            future_response.thenRun(() -> {
                handled_rate.mark();
            });
        }
    }
}
