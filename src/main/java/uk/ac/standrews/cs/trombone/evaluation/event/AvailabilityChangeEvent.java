package uk.ac.standrews.cs.trombone.evaluation.event;

import uk.ac.standrews.cs.shabdiz.util.HashCodeUtil;
import uk.ac.standrews.cs.trombone.PeerReference;

/**
 * Peresents the change of a peer's availability at {@code t} nanoseconds through an experiment.
 *
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class AvailabilityChangeEvent extends ExperimentEvent {

    private static final long serialVersionUID = -9157210573159544121L;
    private final boolean available;
    private final Long availability_duration_in_nanos;

    /**
     * Constructs an availability change event.
     *
     * @param source The peer on which the Event initially occurred
     * @param time_in_nanos the time through the experiment in nanoseconds
     * @param available whether the source is reachable
     * @throws IllegalArgumentException if source is {@code null}
     */
    public AvailabilityChangeEvent(final PeerReference source, long time_in_nanos, boolean available, Long availability_duration_in_nanos) {

        super(source, time_in_nanos);
        this.available = available;
        this.availability_duration_in_nanos = availability_duration_in_nanos;
    }

    /**
     * Whether the source of this event is reachable.
     *
     * @return the the source of this event is reachable
     */
    public boolean isAvailable() {

        return available;
    }

    public long getAvailabilityDurationInNanos() {

        return availability_duration_in_nanos;
    }

    @Override
    public int hashCode() {

        return HashCodeUtil.generate(super.hashCode(), available ? 1 : 0);
    }

    @Override
    public boolean equals(final Object other) {

        if (this == other) { return true; }
        if (!(other instanceof AvailabilityChangeEvent)) { return false; }
        final AvailabilityChangeEvent that = (AvailabilityChangeEvent) other;
        return super.equals(other) && available == that.available && availability_duration_in_nanos.equals(that.availability_duration_in_nanos);
    }
}
