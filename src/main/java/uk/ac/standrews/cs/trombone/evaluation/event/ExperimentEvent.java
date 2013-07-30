package uk.ac.standrews.cs.trombone.evaluation.event;

import java.util.EventObject;
import uk.ac.standrews.cs.shabdiz.util.HashCodeUtil;
import uk.ac.standrews.cs.trombone.PeerReference;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public abstract class ExperimentEvent extends EventObject implements Comparable<ExperimentEvent> {

    private static final long serialVersionUID = -5689824070545613983L;
    private final Long time_in_nanos;

    protected ExperimentEvent(final PeerReference source, Long time_in_nanos) {

        super(source);
        this.time_in_nanos = time_in_nanos;
    }

    @Override
    public PeerReference getSource() {

        return (PeerReference) super.getSource();
    }

    public long getTimeInNanos() {

        return time_in_nanos;
    }

    @Override
    public int compareTo(final ExperimentEvent other) {

        return time_in_nanos.compareTo(other.time_in_nanos);
    }

    @Override
    public int hashCode() {

        return HashCodeUtil.generate(getSource().hashCode(), time_in_nanos.hashCode());
    }

    @Override
    public boolean equals(final Object other) {

        if (this == other) { return true; }
        if (!(other instanceof ExperimentEvent)) { return false; }
        final ExperimentEvent that = (ExperimentEvent) other;
        if (time_in_nanos != null ? !time_in_nanos.equals(that.time_in_nanos) : that.time_in_nanos != null) { return false; }
        return getSource().equals(that.getSource()) && time_in_nanos.equals(that.time_in_nanos);
    }
}
