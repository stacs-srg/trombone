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
    static final PeerConfiguration DEFAULT_PEER_CONFIGURATION = new DefaultPeerConfiguration();
    public static final SyntheticDelay NO_SYNTHETIC_DELAY = new SyntheticDelay() {

        @Override
        public void apply(final InetAddress from, final InetAddress to) throws InterruptedException {
            // do nothing
        }
    };

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

    private static class DefaultPeerConfiguration implements PeerConfiguration {

        private static final long serialVersionUID = -6578481188286774616L;

        @Override
        public Maintenance getMaintenance(final Peer peer) {

            return new Maintenance(peer);
        }

        @Override
        public SyntheticDelay getSyntheticDelay() {

            return NO_SYNTHETIC_DELAY;
        }
    }
}
