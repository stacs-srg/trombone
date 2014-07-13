package uk.ac.standrews.cs.trombone.event;

import java.util.EventObject;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Presents an event that occurs on a {@link Participant participant}.
 *
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public abstract class Event extends EventObject implements Comparable<Event> {

    private static final long serialVersionUID = -3008406138733512647L;
    private static final AtomicLong NEXT_ID = new AtomicLong();

    protected final Participant source;
    protected final Long time_nanos;
    protected final Long id;

    /**
     * Constructs a new event.
     *
     * @param source the participant on which this event occurred
     * @param time_nanos the time at which this event occurred
     */
    protected Event(final Participant source, Long time_nanos) {

        super(source);
        this.source = source;
        this.time_nanos = time_nanos;
        id = NEXT_ID.getAndIncrement();
    }

    /**
     * Gets the unique identifier of this event.
     * The identifier is unique between all the events that have been generated in the current JVM.
     *
     * @return the unique identifier of this event
     */
    public Long getId() {

        return id;
    }

    @Override
    public Participant getSource() {

        return source;
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

        int result = time_nanos.hashCode();
        result = 31 * result + id.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object other) {

        if (this == other) { return true; }
        if (!(other instanceof Event)) { return false; }
        final Event that = (Event) other;
        return id.equals(that.id) && time_nanos.equals(that.time_nanos) && source.equals(that.source);
    }
}
