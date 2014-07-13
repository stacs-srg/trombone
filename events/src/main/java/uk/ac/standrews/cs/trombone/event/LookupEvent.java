package uk.ac.standrews.cs.trombone.event;

import uk.ac.standrews.cs.trombone.core.PeerReference;
import uk.ac.standrews.cs.trombone.core.key.Key;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class LookupEvent extends Event {

    private static final long serialVersionUID = 8171231149854930079L;
    private final Key target;
    private transient PeerReference expected_result;

    LookupEvent(final Participant source, final Long time_nanos, final Key target) {

        super(source, time_nanos);
        this.target = target;
    }

    public Key getTarget() {

        return target;
    }

    public PeerReference getExpectedResult() {

        return expected_result;
    }

    public void setExpectedResult(final PeerReference expected_result) {

        this.expected_result = expected_result;
    }

    @Override
    public boolean equals(final Object other) {

        if (this == other) { return true; }
        if (!(other instanceof LookupEvent)) { return false; }
        if (!super.equals(other)) { return false; }

        final LookupEvent that = (LookupEvent) other;

        if (expected_result != null ? !expected_result.equals(that.expected_result) : that.expected_result != null) {
            return false;
        }
        if (!target.equals(that.target)) { return false; }

        return true;
    }

    @Override
    public int hashCode() {

        int result = super.hashCode();
        result = 31 * result + target.hashCode();
        result = 31 * result + (expected_result != null ? expected_result.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {

        return "LookupEvent{" + "time=" + getTimeInNanos() + ", peer=" + getSource() + ", target=" + target + ", expected_result=" + expected_result + '}';
    }
}
