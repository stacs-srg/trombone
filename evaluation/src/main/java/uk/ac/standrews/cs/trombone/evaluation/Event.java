package uk.ac.standrews.cs.trombone.evaluation;

import java.util.EventObject;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.lang.builder.HashCodeBuilder;
import uk.ac.standrews.cs.trombone.core.PeerReference;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public abstract class Event extends EventObject implements Comparable<Event> {

    private static final AtomicLong NEXT_ID = new AtomicLong();
    private static final long serialVersionUID = -3008406138733512647L;
    private final Integer source_id;
    protected final Long time_nanos;
    private final Long id;
    private transient Participant participant;

    protected Event(final Participant source, Long time_nanos) {

        this(source.getReference(), source.getId(), time_nanos);
        participant = source;
    }

    /**
     * @param source the peer on which this event occurs
     * @param source_id
     * @param time_nanos the moment in time at which this event occurs
     */
    protected Event(final PeerReference source, Integer source_id, Long time_nanos) {

        super(source);
        this.source_id = source_id;
        this.time_nanos = time_nanos;
        id = NEXT_ID.getAndIncrement();
    }

    @Override
    public PeerReference getSource() {

        return (PeerReference) super.getSource();
    }

    public Integer getSourceId() {

        return source_id;
    }

    public Long getTimeInNanos() {

        return time_nanos;
    }

    @Override
    public int compareTo(final Event other) {

        //TODO give higher priority to churn event if times are the same
        final int time_comparison = time_nanos.compareTo(other.time_nanos);
        return time_comparison != 0 ? time_comparison : id.compareTo(other.id);
    }

    @Override
    public int hashCode() {

        return new HashCodeBuilder().append(id).append(time_nanos).append(source).toHashCode();
    }

    @Override
    public boolean equals(final Object other) {

        if (this == other) { return true; }
        if (!(other instanceof Event)) { return false; }
        final Event that = (Event) other;
        return id.equals(that.id) && time_nanos.equals(that.time_nanos) && getSource().equals(that.getSource());
    }

    public Participant getParticipant() {

        return participant;
    }

    abstract int getCode();

    abstract String getParameters();
}
