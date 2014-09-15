package uk.ac.standrews.cs.trombone.core;

import java.net.InetSocketAddress;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class PeerReference implements Comparable<PeerReference> {

    private final Key key;
    private final InetSocketAddress address;

    public PeerReference(final Key key, final InetSocketAddress address) {

        this.key = key;
        this.address = address;
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
        return key.equals(that.key) && address.equals(that.address);
    }

    @Override
    public String toString() {

        return "PeerReference{" + "key=" + getKey() + ", address=" + getAddress() + '}';
    }

    @Override
    public int compareTo(final PeerReference other) {

        return key.compareTo(other.key);
    }
}
