package uk.ac.standrews.cs.trombone.event;

/**
 * Presents the change of a peer's availability at {@code t} nanoseconds through an experiment.
 *
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class LeaveEvent extends Event {

    private static final long serialVersionUID = 7322230770762762864L;

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

    @Override
    public String toString() {

        return "LeaveEvent{" + "time=" + getTimeInNanos() + ", peer=" + getSource() + '}';
    }
}
