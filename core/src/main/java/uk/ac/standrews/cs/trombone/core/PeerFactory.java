package uk.ac.standrews.cs.trombone.core;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import org.mashti.jetson.exception.RPCException;
import org.mashti.jetson.lean.LeanClientFactory;
import uk.ac.standrews.cs.trombone.core.key.Key;
import uk.ac.standrews.cs.trombone.core.rpc.codec.PeerCodecs;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public final class PeerFactory {

    static final LeanClientFactory<PeerRemote> CLIENT_FACTORY = new LeanClientFactory<PeerRemote>(PeerRemote.class, PeerCodecs.INSTANCE);
    public static final SyntheticDelay NO_SYNTHETIC_DELAY = new SyntheticDelay() {

        private static final long serialVersionUID = 8318814582661151942L;

        @Override
        public void apply(final InetAddress from, final InetAddress to) {
            // do nothing
        }

        @Override
        public String getName() {

            return "NoSyntheticDelay";
        }
    };
    static final PeerConfiguration DEFAULT_PEER_CONFIGURATION = new PeerConfiguration(new Maintenance(), NO_SYNTHETIC_DELAY);

    public static PeerRemote bind(PeerReference reference) {

        return CLIENT_FACTORY.get(reference.getAddress());
    }

    public static PeerReference bind(final InetSocketAddress address) throws RPCException {

        final PeerRemote remote = CLIENT_FACTORY.get(address);
        final Key key = remote.getKey();
        return new PeerReference(key, address);
    }

    public static Peer createPeer(PeerReference reference, final PeerConfiguration configurator) {

        final Key key = reference.getKey();
        final InetSocketAddress address = reference.getAddress();
        return createPeer(address, key, configurator);
    }

    public static Peer createPeer(final Key key) {

        return new Peer(key);
    }

    public static Peer createPeer(final InetSocketAddress address, final Key key, final PeerConfiguration configuration) {

        return new Peer(address, key, configuration);
    }
}
