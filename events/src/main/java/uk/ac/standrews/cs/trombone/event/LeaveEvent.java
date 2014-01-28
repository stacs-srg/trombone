package uk.ac.standrews.cs.trombone.event;

import org.apache.commons.lang.builder.HashCodeBuilder;
import uk.ac.standrews.cs.trombone.core.PeerReference;

/**
 * Presents the change of a peer's availability at {@code t} nanoseconds through an experiment.
 *
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class LeaveEvent extends Event {

    static final int LEAVE_EVENT_CODE = 0;
    private static final long serialVersionUID = 7322230770762762864L;
    private static final String NONE = "";

    /**
     * Constructs an availability change event.
     *
     * @param source The peer on which the Event initially occurred
     * @param time_nanos the time through the experiment in nanoseconds
     * @throws IllegalArgumentException if source is {@code null}
     */
    LeaveEvent(final Participant source, long time_nanos) {

        super(source, time_nanos);
    }

    LeaveEvent(final PeerReference source, Integer source_id, long time_nanos) {

        super(source, source_id, time_nanos);
    }

    @Override
    public int hashCode() {

        return new HashCodeBuilder().append(time_nanos).appendSuper(super.hashCode()).toHashCode();
    }

    @Override
    public boolean equals(final Object other) {

        if (this == other) { return true; }
        if (!(other instanceof LeaveEvent)) { return false; }
        return super.equals(other);
    }

    @Override
    public String toString() {

        final StringBuilder sb = new StringBuilder("LeaveEvent{");
        sb.append("time=").append(getTimeInNanos());
        sb.append(", peer=").append(getSource());
        sb.append('}');
        return sb.toString();
    }

    @Override
    int getCode() {

        return LEAVE_EVENT_CODE;
    }

    @Override
    String getParameters() {

        return NONE;
    }
}
