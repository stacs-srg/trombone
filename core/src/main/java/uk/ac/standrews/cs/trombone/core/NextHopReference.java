package uk.ac.standrews.cs.trombone.core;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class NextHopReference extends PeerReference {

    private final boolean final_hop;

    public NextHopReference(PeerReference reference, boolean final_hop) {

        super(reference.getKey(), reference.getAddress());
        this.final_hop = final_hop;
    }

    public boolean isFinalHop() {

        return final_hop;
    }

    @Override
    public boolean equals(final Object other) {

        if (this == other) { return true; }
        if (!(other instanceof NextHopReference)) { return false; }
        if (!super.equals(other)) { return false; }

        final NextHopReference that = (NextHopReference) other;
        return final_hop == that.final_hop;
    }

    @Override
    public int hashCode() {

        return 31 * super.hashCode() + (final_hop ? 1 : 0);
    }
}
