package uk.ac.standrews.cs.trombone.core;

import java.net.InetSocketAddress;
import uk.ac.standrews.cs.trombone.core.key.Key;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class InternalPeerReference extends PeerReference {

    private volatile long first_seen;
    private volatile long last_seen;

    InternalPeerReference(final Key key, final InetSocketAddress address) {

        super(key, address);
    }

    InternalPeerReference(final PeerReference reference) {

        super(reference.getKey(), reference.getAddress(), reference.isReachable());
    }

    public boolean isContactedBefore() {

        return first_seen != 0;
    }

    synchronized void seen() {

        last_seen = System.currentTimeMillis();
        if (!isContactedBefore()) {
            first_seen = last_seen;
        }
        setReachable(true);
    }
}
