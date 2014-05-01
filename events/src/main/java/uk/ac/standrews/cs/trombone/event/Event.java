package uk.ac.standrews.cs.trombone.event;

import java.util.EventObject;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.lang.builder.HashCodeBuilder;
import uk.ac.standrews.cs.trombone.core.PeerReference;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public abstract class Event extends EventObject implements Comparable<Event> {

    private static final long serialVersionUID = -3008406138733512647L;
    private static final AtomicLong NEXT_ID = new AtomicLong();
    protected final Long time_nanos;
    private final Long id;
    private transient Participant participant;

    protected Event(final Participant source, Long time_nanos) {

        this(source.getReference(), time_nanos);
        participant = source;
    }

    protected Event(final PeerReference source, Long time_nanos) {

        super(source);
        this.time_nanos = time_nanos;
        id = NEXT_ID.getAndIncrement();

    }

    @Override
    public PeerReference getSource() {

        return (PeerReference) super.getSource();
    }

    public Long getTimeInNanos() {

        return time_nanos;
    }

    @Override
    public int compareTo(final Event other) {

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
}
