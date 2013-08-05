package uk.ac.standrews.cs.trombone.evaluation;

import uk.ac.standrews.cs.shabdiz.util.HashCodeUtil;

/**
 * Peresents the change of a peer's availability at {@code t} nanoseconds through an experiment.
 *
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class AvailabilityChangeEvent extends ExperimentEvent {

    private static final long serialVersionUID = -9157210573159544121L;
    private final boolean available;
    private final Long duration_nanos;

    /**
     * Constructs an availability change event.
     *
     * @param source The peer on which the Event initially occurred
     * @param time_nanos the time through the experiment in nanoseconds
     * @param available whether the source is reachable
     * @throws IllegalArgumentException if source is {@code null}
     */
    AvailabilityChangeEvent(final Participant source, long time_nanos, boolean available, Long duration_nanos) {

        super(source, time_nanos);
        this.available = available;
        this.duration_nanos = duration_nanos;
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

        return duration_nanos;
    }

    @Override
    public int hashCode() {

        return HashCodeUtil.generate(super.hashCode(), available ? 1 : 2, duration_nanos.hashCode());
    }

    @Override
    public boolean equals(final Object other) {

        if (this == other) { return true; }
        if (!(other instanceof AvailabilityChangeEvent)) { return false; }
        final AvailabilityChangeEvent that = (AvailabilityChangeEvent) other;
        return super.equals(other) && available == that.available && duration_nanos.equals(that.duration_nanos);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AvailabilityChangeEvent{");
        sb.append("time=").append(getTimeInNanos());
        sb.append(", peer=").append(getSource().getKey());
        sb.append(", available=").append(available);
        sb.append(", duration_nanos=").append(duration_nanos);
        sb.append('}');
        return sb.toString();
    }
}
