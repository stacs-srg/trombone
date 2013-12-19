package uk.ac.standrews.cs.trombone.core;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.lang.builder.HashCodeBuilder;
import uk.ac.standrews.cs.trombone.core.key.Key;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class InternalPeerReference extends PeerReference {

    private final AtomicLong first_seen = new AtomicLong();
    private final AtomicLong last_seen = new AtomicLong();

    InternalPeerReference(final Key key, final InetSocketAddress address) {

        super(key, address);
    }

    InternalPeerReference(final PeerReference reference) {

        super(reference.getKey(), reference.getAddress(), reference.isReachable());
    }

    public boolean isContactedBefore() {

        return first_seen.get() != 0;
    }

    @Override
    public boolean equals(final Object other) {

        if (this == other) { return true; }
        return super.equals(other);
    }

    @Override
    public int hashCode() {

        return new HashCodeBuilder(19, 93).appendSuper(super.hashCode()).toHashCode();
    }

    void seen() {

        last_seen.set(System.currentTimeMillis());
        if (!isContactedBefore()) {
            first_seen.set(last_seen.get());
        }
        setReachable(true);
    }
}
