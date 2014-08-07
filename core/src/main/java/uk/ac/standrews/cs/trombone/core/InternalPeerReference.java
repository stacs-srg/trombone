package uk.ac.standrews.cs.trombone.core;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicLong;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class InternalPeerReference extends PeerReference {

    public static final long NEVER_SEEN = 0;
    private final AtomicLong first_seen = new AtomicLong();
    private final AtomicLong last_seen = new AtomicLong();

    InternalPeerReference(final Key key, final InetSocketAddress address) {

        super(key, address);
    }

    public InternalPeerReference(final PeerReference reference) {

        super(reference.getKey(), reference.getAddress(), reference.isReachable());
    }

    public boolean isContactedBefore() {

        return getFirstSeen() != NEVER_SEEN;
    }

    public long getElapsedMillisSinceLastSeen() {

        return System.currentTimeMillis() - getLastSeen();
    }

    public long getFirstSeen() {

        return first_seen.get();
    }

    public long getLastSeen() {

        return last_seen.get();
    }

    void seen(boolean reachable) {

        final long now = System.currentTimeMillis();
        last_seen.set(now);
        if (!isContactedBefore()) {
            first_seen.compareAndSet(NEVER_SEEN, now);
        }
        setReachable(reachable);
    }

}
