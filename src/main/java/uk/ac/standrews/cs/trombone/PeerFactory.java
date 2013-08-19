package uk.ac.standrews.cs.trombone;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import org.mashti.jetson.exception.RPCException;
import org.mashti.jetson.lean.LeanClientFactory;
import uk.ac.standrews.cs.trombone.codec.PeerCodecs;
import uk.ac.standrews.cs.trombone.key.Key;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public final class PeerFactory {

    static final LeanClientFactory<PeerRemote> CLIENT_FACTORY = new LeanClientFactory<PeerRemote>(PeerRemote.class, PeerCodecs.INSTANCE);

    public static PeerRemote bind(PeerReference reference) {

        return CLIENT_FACTORY.get(reference.getAddress());
    }

    public static PeerReference bind(final InetSocketAddress address) throws RPCException {

        final PeerRemote remote = CLIENT_FACTORY.get(address);
        final Key key = remote.getKey();
        return new PeerReference(key, address);
    }

    public static Peer createPeer(PeerReference reference, final PeerConfigurator configurator) {

        final Key key = reference.getKey();
        final InetSocketAddress address = reference.getAddress();
        return createPeer(address, key, configurator);
    }

    public static Peer createPeer(final Key key) throws UnknownHostException {

        return new Peer(key);
    }

    public static Peer createPeer(final InetSocketAddress address, final Key key, final PeerConfigurator configurator) {

        final Peer peer = new Peer(address, key);
        if (configurator != null) {
            configurator.configure(peer);
        }
        return peer;
    }
}
