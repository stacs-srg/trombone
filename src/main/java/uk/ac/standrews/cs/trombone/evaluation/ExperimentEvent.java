package uk.ac.standrews.cs.trombone.evaluation;

import java.util.EventObject;
import java.util.concurrent.atomic.AtomicLong;
import uk.ac.standrews.cs.shabdiz.util.HashCodeUtil;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public abstract class ExperimentEvent extends EventObject implements Comparable<ExperimentEvent> {

    private static final long serialVersionUID = -5689824070545613983L;
    private static final AtomicLong NEXT_ID = new AtomicLong();
    private final Long time_nanos;
    private final Long id; // used to break ties when comparing two events

    protected ExperimentEvent(final Participant source, Long time_nanos) {

        super(source);
        this.time_nanos = time_nanos;
        id = NEXT_ID.getAndIncrement();
    }

    @Override
    public Participant getSource() {

        return (Participant) super.getSource();
    }

    public long getTimeInNanos() {

        return time_nanos;
    }

    @Override
    public int compareTo(final ExperimentEvent other) {

        final int time_comparison = time_nanos.compareTo(other.time_nanos);
        return time_comparison != 0 ? time_comparison : id.compareTo(other.id);
    }

    @Override
    public int hashCode() {

        return HashCodeUtil.generate(getSource().hashCode(), time_nanos.hashCode(), id.hashCode());
    }

    @Override
    public boolean equals(final Object other) {

        if (this == other) { return true; }
        if (!(other instanceof ExperimentEvent)) { return false; }
        final ExperimentEvent that = (ExperimentEvent) other;
        return id.equals(that.id) && time_nanos.equals(that.time_nanos) && getSource().equals(that.getSource());
    }
}
