package uk.ac.standrews.cs.trombone.core;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.lang.builder.HashCodeBuilder;
import uk.ac.standrews.cs.trombone.core.key.Key;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class InternalPeerReference extends PeerReference {

    private final AtomicLong first_seen = new AtomicLong();
    private final AtomicLong last_seen_reachable = new AtomicLong();
    private final int hashcode;

    InternalPeerReference(final Key key, final InetSocketAddress address) {

        super(key, address);
        hashcode = calculateHashcode();
    }

    InternalPeerReference(final PeerReference reference) {

        super(reference.getKey(), reference.getAddress(), reference.isReachable());
        hashcode = calculateHashcode();
    }

    public boolean isContactedBefore() {

        return getFirstSeen() != 0;
    }

    public long getAge() {

        return getLastSeen() - getFirstSeen();
    }

    private long getFirstSeen() {

        return first_seen.get();
    }

    private long getLastSeen() {

        return last_seen_reachable.get();
    }

    @Override
    public boolean equals(final Object other) {

        if (this == other) { return true; }
        return super.equals(other);
    }

    @Override
    public int hashCode() {

        return hashcode;
    }

    void seen(boolean reachable) {

        last_seen_reachable.set(System.currentTimeMillis());
        if (!isContactedBefore()) {
            first_seen.set(getLastSeen());
        }
        setReachable(reachable);
    }

    @Override
    protected void setReachable(final boolean reachable) {

        if(reachable){
            
        }
        
        super.setReachable(reachable);
    }

    private int calculateHashcode() {

        return new HashCodeBuilder(19, 93).appendSuper(super.hashCode()).toHashCode();
    }
}
