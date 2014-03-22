package uk.ac.standrews.cs.trombone.core;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.mashti.gauge.Rate;
import org.mashti.jetson.FutureResponse;
import org.mashti.jetson.Server;
import org.mashti.jetson.ServerFactory;
import org.mashti.jetson.lean.LeanServerChannelInitializer;
import org.mashti.jetson.util.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.trombone.core.rpc.codec.PeerCodecs;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class PeerServerFactory extends ServerFactory<PeerRemote> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PeerServerFactory.class);
    private static final ServerBootstrap SERVER_BOOTSTRAP = new ServerBootstrap();
    private static ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);

    static final NioEventLoopGroup parent_event_loop;
    static final NioEventLoopGroup child_event_loop;
    static final Rate handling_rate = new Rate();
    static final Rate handled_rate = new Rate();

    static {
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(new Runnable() {

            @Override
            public void run() {

                LOGGER.info("server handling rate: {}", handling_rate.getRate());
                LOGGER.info("server handled rate: {}", handled_rate.getRate());
            }
        }, 10, 10, TimeUnit.SECONDS);
        parent_event_loop = new NioEventLoopGroup(0, new NamedThreadFactory("server_parent_event_loop_"));
        child_event_loop = new NioEventLoopGroup(0, new NamedThreadFactory("server_child_event_loop_"));
        SERVER_BOOTSTRAP.group(parent_event_loop, child_event_loop);
        SERVER_BOOTSTRAP.channel(NioServerSocketChannel.class);
        SERVER_BOOTSTRAP.option(ChannelOption.TCP_NODELAY, true);
        SERVER_BOOTSTRAP.childOption(ChannelOption.TCP_NODELAY, true);
        SERVER_BOOTSTRAP.childOption(ChannelOption.SO_KEEPALIVE, true);
        SERVER_BOOTSTRAP.childHandler(new LeanServerChannelInitializer<PeerRemote>(PeerRemote.class, PeerCodecs.INSTANCE));
        executor.prestartAllCoreThreads();
    }

    public static void shutdownPeerServerFactory() {

        try {
            SERVER_BOOTSTRAP.childGroup().shutdownGracefully().sync();
            SERVER_BOOTSTRAP.group().shutdownGracefully().sync();
        }
        catch (InterruptedException e) {
            LOGGER.warn("interrupted while shutting down peer server factory", e);
        }
    }

    public PeerServerFactory() {

        super(SERVER_BOOTSTRAP);
    }

    @Override
    public Server createServer(final PeerRemote service) {

        return new MyServer(server_bootstrap, service);
    }

    static class MyServer extends Server {

        private final Peer peer;

        MyServer(final ServerBootstrap server_bootstrap, final Object service) {

            super(server_bootstrap, service);
            peer = (Peer) service;
        }

        @Override
        protected void handle(final ChannelHandlerContext context, final FutureResponse future_response) {

            handling_rate.mark();
            executor.execute(new Runnable() {

                @Override
                public void run() {

                    peer.getPeerMetric().notifyServe(future_response.getMethod()); // record frequency of called methods
                    MyServer.super.handle(context, future_response);
                    handled_rate.mark();
                }
            });
        }
    }
}
