package uk.ac.standrews.cs.trombone.core;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class PeerReference implements Comparable<PeerReference> {

    private static final boolean DEFAULT_REACHABILITY = true;
    private final Key key;
    private final InetSocketAddress address;
    private final AtomicBoolean reachable;

    public PeerReference(final Key key, final InetSocketAddress address) {

        this(key, address, DEFAULT_REACHABILITY);
    }

    public PeerReference(final Key key, final InetSocketAddress address, final Boolean reachable) {

        this.key = key;
        this.address = address;
        this.reachable = new AtomicBoolean(reachable);
    }

    public boolean isReachable() {

        return reachable.get();
    }

    public Key getKey() {

        return key;
    }

    public InetSocketAddress getAddress() {

        return address;
    }

    @Override
    public int hashCode() {

        return key.hashCode();
    }

    @Override
    public boolean equals(final Object other) {

        if (this == other) { return true; }
        if (!(other instanceof PeerReference)) { return false; }

        final PeerReference that = (PeerReference) other;
        return key.equals(that.key) && address.equals(that.address) && isReachable() == that.isReachable();
    }

    @Override
    public String toString() {

        return "PeerReference{" + "key=" + getKey() + ", address=" + getAddress() + ", reachable=" + isReachable() + '}';
    }

    @Override
    public int compareTo(final PeerReference other) {

        return key.compareTo(other.key);
    }

    public void setReachable(final boolean reachable) {

        this.reachable.set(reachable);
    }
}
