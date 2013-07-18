package uk.ac.standrews.cs.trombone.trombone;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;
import uk.ac.standrews.cs.trombone.trombone.key.Key;
import uk.ac.standrews.cs.shabdiz.util.HashCodeUtil;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class PeerReference implements Comparable<PeerReference> {

    public static final boolean DEFAULT_REACHABILITY = true;
    private final Key key;
    private final InetSocketAddress address;
    private final AtomicBoolean reachable = new AtomicBoolean();

    public PeerReference(final Key key, final InetSocketAddress address) {

        this(key, address, DEFAULT_REACHABILITY);
    }

    public PeerReference(final Key key, final InetSocketAddress address, final Boolean reachable) {

        this.key = key;
        this.address = address;
        this.reachable.set(reachable);
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

        return HashCodeUtil.generate(key.hashCode(), address.hashCode());
    }

    @Override
    public boolean equals(final Object other) {

        if (this == other) { return true; }
        if (!(other instanceof PeerReference)) { return false; }

        final PeerReference that = (PeerReference) other;
        return address.equals(that.address) && key.equals(that.key);
    }

    @Override
    public String toString() {

        final StringBuilder sb = new StringBuilder("PeerReference{");
        sb.append("key=").append(key);
        sb.append(", address=").append(address);
        sb.append(", reachable=").append(reachable.get());
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int compareTo(final PeerReference other) {

        return key.compareTo(other.key);
    }

    protected void setReachable(final boolean reachable) {

        this.reachable.set(reachable);
    }
}
