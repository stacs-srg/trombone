package uk.ac.standrews.cs.trombone.evaluation;

import uk.ac.standrews.cs.shabdiz.util.HashCodeUtil;
import uk.ac.standrews.cs.trombone.PeerReference;

/**
 * Peresents the change of a peer's availability at {@code t} nanoseconds through an experiment.
 *
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class ChurnEvent extends Event {

    static final int UNAVAILABLE_CODE = 0;
    static final int AVAILABLE_CODE = 1;
    private static final long serialVersionUID = -9157210573159544121L;
    private static final String EMPTY_STRING = "";
    private final boolean available;
    private Long duration_nanos;

    /**
     * Constructs an availability change event.
     *
     * @param source The peer on which the Event initially occurred
     * @param time_nanos the time through the experiment in nanoseconds
     * @param available whether the source is reachable
     * @throws IllegalArgumentException if source is {@code null}
     */
    ChurnEvent(final Participant source, long time_nanos, boolean available) {

        super(source, time_nanos);
        this.available = available;
    }

    ChurnEvent(final PeerReference source, Integer source_id, long time_nanos, boolean available) {

        super(source, source_id, time_nanos);
        this.available = available;
    }

    /**
     * Whether the source of this event is reachable.
     *
     * @return the the source of this event is reachable
     */
    public boolean isAvailable() {

        return available;
    }

    @Override
    public int hashCode() {

        return HashCodeUtil.generate(super.hashCode(), available ? 1 : 2);
    }

    @Override
    public boolean equals(final Object other) {

        if (this == other) { return true; }
        if (!(other instanceof ChurnEvent)) { return false; }
        final ChurnEvent that = (ChurnEvent) other;
        return super.equals(other) && available == that.available;
    }

    @Override
    int getCode() {
        return !isAvailable() ? UNAVAILABLE_CODE : AVAILABLE_CODE;
    }

    @Override
    String getParameters() {

        final String parameters;
        if (isAvailable()) {
            if (duration_nanos == null) { throw new IllegalArgumentException("duration must be specified when peer is avaiable"); }
            parameters = String.valueOf(duration_nanos);
        }
        else {
            parameters = EMPTY_STRING;
        }
        return parameters;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ChurnEvent{");
        sb.append("time=").append(getTimeInNanos());
        sb.append(", peer=").append(getSource());
        sb.append(", available=").append(available);
        sb.append('}');
        return sb.toString();
    }

    void setDurationInNanos(Long duration_nanos) {

        this.duration_nanos = duration_nanos;
    }

    Long getDurationInNanos() {

        return duration_nanos;
    }
}
