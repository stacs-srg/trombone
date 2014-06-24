package uk.ac.standrews.cs.trombone.core.selector;

import java.io.Serializable;
import java.util.List;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerReference;
import uk.ac.standrews.cs.trombone.core.util.Copyable;
import uk.ac.standrews.cs.trombone.core.util.Named;
import uk.ac.standrews.cs.trombone.core.util.NamingUtils;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public abstract class Selector implements Serializable, Copyable, Named {

    private static final long serialVersionUID = -1994233167230411201L;

    public enum ReachabilityCriteria {
        REACHABLE,
        UNREACHABLE,
        REACHABLE_OR_UNREACHABLE
    }

    protected int size;
    protected ReachabilityCriteria reachability_criteria;

    protected Selector(int size, ReachabilityCriteria reachability_criteria) {

        this.size = size;
        this.reachability_criteria = reachability_criteria;
    }

    public abstract List<? extends PeerReference> select(Peer peer);

    @Override
    public abstract Selector copy();

    @Override
    public String getName() {

        return NamingUtils.name(this);
    }

    public boolean isSingleton() {

        return false;
    }

    public int getSelectionSize() {

        return size;
    }

    public ReachabilityCriteria getReachabilityCriteria() {

        return reachability_criteria;
    }

    @Override
    public boolean equals(final Object other) {

        if (this == other) { return true; }
        if (!getClass().isInstance(other)) { return false; }

        final Selector selector = (Selector) other;
        return size == selector.size && reachability_criteria == selector.reachability_criteria;
    }

    @Override
    public int hashCode() {

        int result = size;
        return  31 * result + reachability_criteria.hashCode();
    }
}
