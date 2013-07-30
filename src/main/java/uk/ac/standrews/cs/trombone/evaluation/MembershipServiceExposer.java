package uk.ac.standrews.cs.trombone.evaluation;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import org.mashti.jetson.ClientFactory;
import org.mashti.jetson.Server;
import org.mashti.jetson.ServerFactory;
import org.mashti.jetson.lean.LeanClientFactory;
import org.mashti.jetson.lean.LeanServerFactory;
import uk.ac.standrews.cs.shabdiz.util.AttributeKey;
import uk.ac.standrews.cs.shabdiz.util.NetworkUtil;
import uk.ac.standrews.cs.trombone.codec.PeerCodecs;
import uk.ac.standrews.cs.trombone.evaluation.membership.MembershipService;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class MembershipServiceExposer {

    static final AttributeKey<MembershipService> MEMBERSHIP_SERVICE_ATTRIBUTE_KEY = new AttributeKey<MembershipService>();
    private static final ServerFactory<MembershipService> SERVER_FACTORY = new LeanServerFactory<MembershipService>(MembershipService.class, PeerCodecs.INSTANCE);
    private static final ClientFactory<MembershipService> CLIENT_FACTORY = new LeanClientFactory<MembershipService>(MembershipService.class, PeerCodecs.INSTANCE);
    private final MembershipService service;
    private final Server server;

    MembershipServiceExposer(final MembershipService service) throws UnknownHostException {

        this(NetworkUtil.getLocalIPv4InetSocketAddress(0), service);
    }

    MembershipServiceExposer(InetSocketAddress endpoint, MembershipService service) {

        this.service = service;
        server = SERVER_FACTORY.createServer(service);
        server.setBindAddress(endpoint);
    }

    public static MembershipService bind(InetSocketAddress address) {

        return CLIENT_FACTORY.get(address);
    }

    boolean expose() throws IOException {

        return server.expose();
    }

    boolean unexpose() throws IOException {

        return server.unexpose();
    }

    InetSocketAddress getAddress() {

        return server.getLocalSocketAddress();
    }

    MembershipService getMembershipService() {

        return service;
    }
}
