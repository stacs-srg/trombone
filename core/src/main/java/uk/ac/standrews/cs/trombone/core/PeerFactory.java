package uk.ac.standrews.cs.trombone.core;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import org.mashti.jetson.lean.LeanClientFactory;
import uk.ac.standrews.cs.trombone.core.key.Key;
import uk.ac.standrews.cs.trombone.core.rpc.codec.PeerCodecs;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public final class PeerFactory {

    static final LeanClientFactory<AsynchronousPeerRemote> CLIENT_FACTORY = new LeanClientFactory<>(AsynchronousPeerRemote.class, PeerCodecs.getInstance());

    private PeerFactory() {

    }

    public static AsynchronousPeerRemote bind(PeerReference reference) {

        return CLIENT_FACTORY.get(reference.getAddress());
    }

    public static CompletableFuture<PeerReference> bind(final InetSocketAddress address) {

        return CLIENT_FACTORY.get(address).getKey().thenCompose(key -> CompletableFuture.completedFuture(new PeerReference(key, address)));
    }

    public static Peer createPeer(PeerReference reference, final PeerConfiguration configuration) {

        final Key key = reference.getKey();
        final InetSocketAddress address = reference.getAddress();
        return createPeer(address, key, configuration);
    }

    public static Peer createPeer(final InetSocketAddress address, final Key key, final PeerConfiguration configuration) {

        return new Peer(address, key, configuration);
    }
}
