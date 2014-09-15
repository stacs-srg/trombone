package uk.ac.standrews.cs.trombone.core.selector;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerReference;
import uk.ac.standrews.cs.trombone.core.util.Copyable;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public abstract class Selector implements Serializable, Copyable {

    private static final long serialVersionUID = -1994233167230411201L;

    protected int size;

    protected Selector(int size) {

        this.size = size;
    }

    public abstract CompletableFuture<List<PeerReference>> select(Peer peer);

    @Override
    public abstract Selector copy();

    public boolean isSingleton() {

        return false;
    }

    public int getSelectionSize() {

        return size;
    }

    @Override
    public boolean equals(final Object other) {

        if (this == other) { return true; }
        if (!getClass().isInstance(other)) { return false; }

        final Selector selector = (Selector) other;
        return size == selector.size;
    }

    @Override
    public int hashCode() {

        return 31 * size;
    }
}
