package uk.ac.standrews.cs.trombone.core;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.mashti.jetson.FutureResponse;
import org.mashti.jetson.Server;
import org.mashti.jetson.ServerFactory;
import org.mashti.jetson.lean.LeanServerChannelInitializer;
import org.mashti.jetson.util.NamedThreadFactory;
import uk.ac.standrews.cs.trombone.core.rpc.codec.PeerCodecs;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
class PeerServerFactory extends ServerFactory<PeerRemote> {

    static final NioEventLoopGroup child_event_loop = new NioEventLoopGroup(100, new NamedThreadFactory("server_child_event_loop_"));
    private static final ServerBootstrap SERVER_BOOTSTRAP = new ServerBootstrap();
    private static final ThreadPoolExecutor SERVER_REQUEST_EXECUTOR = new ThreadPoolExecutor(5, 100, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    static {

        final NioEventLoopGroup parent_event_loop = new NioEventLoopGroup(100, new NamedThreadFactory("server_parent_event_loop_"));
        SERVER_BOOTSTRAP.group(parent_event_loop, child_event_loop);
        SERVER_BOOTSTRAP.channel(NioServerSocketChannel.class);
        SERVER_BOOTSTRAP.option(ChannelOption.TCP_NODELAY, true);
        SERVER_BOOTSTRAP.childOption(ChannelOption.TCP_NODELAY, true);
        SERVER_BOOTSTRAP.childOption(ChannelOption.SO_KEEPALIVE, true);
        SERVER_BOOTSTRAP.childHandler(new LeanServerChannelInitializer<PeerRemote>(PeerRemote.class, PeerCodecs.INSTANCE));

        SERVER_REQUEST_EXECUTOR.prestartAllCoreThreads();
    }

    public PeerServerFactory() {

        super(SERVER_BOOTSTRAP, SERVER_REQUEST_EXECUTOR);
    }

    @Override
    public Server createServer(final PeerRemote service) {

        return new MyServer(server_bootstrap, service, SERVER_REQUEST_EXECUTOR) {

        };
    }

    static class MyServer extends Server {

        private final Peer peer;

        MyServer(final ServerBootstrap server_bootstrap, final Object service, final ExecutorService executor) {

            super(server_bootstrap, service, executor);

            peer = (Peer) service;
        }

        @Override
        protected void handle(final ChannelHandlerContext context, final FutureResponse future_response) {

            peer.getPeerMetric().notifyServe(future_response.getMethod()); // record frequency of called methods
            super.handle(context, future_response);
        }
    }
}
