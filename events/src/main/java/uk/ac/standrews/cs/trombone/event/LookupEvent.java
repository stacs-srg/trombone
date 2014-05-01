package uk.ac.standrews.cs.trombone.event;

import uk.ac.standrews.cs.shabdiz.util.HashCodeUtil;
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

    public void setExpectedResult(final Participant expected_result) {

        this.expected_result = expected_result.getReference();
    }

    public void setExpectedResult(final PeerReference expected_result) {

        this.expected_result = expected_result;
    }

    @Override
    public int hashCode() {

        return HashCodeUtil.generate(super.hashCode(), target.hashCode(), expected_result.hashCode());
    }

    @Override
    public boolean equals(final Object other) {

        if (this == other) { return true; }
        if (!(other instanceof LookupEvent)) { return false; }
        final LookupEvent that = (LookupEvent) other;
        return super.equals(other) && target.equals(that.target) && expected_result.equals(that.expected_result);
    }

    @Override
    public String toString() {

        final StringBuilder sb = new StringBuilder("LookupEvent{");
        sb.append("time=").append(getTimeInNanos());
        sb.append(", peer=").append(getSource());
        sb.append(", target=").append(target);
        sb.append(", expected_result=").append(expected_result);
        sb.append('}');
        return sb.toString();
    }
}
