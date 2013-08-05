package uk.ac.standrews.cs.trombone.evaluation;

import uk.ac.standrews.cs.shabdiz.util.HashCodeUtil;
import uk.ac.standrews.cs.trombone.key.Key;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class LookupEvent extends ExperimentEvent {

    private static final long serialVersionUID = 8171231149854930079L;
    private final Key target;
    private final Participant expected_result;

    public LookupEvent(final Participant source, final Key target, final Participant expected_result, final Long time_nanos) {

        super(source, time_nanos);
        this.target = target;
        this.expected_result = expected_result;
    }

    public Key getTarget() {

        return target;
    }

    public Participant getExpectedResult() {

        return expected_result;
    }

    @Override
    public int hashCode() {

        return HashCodeUtil.generate(super.hashCode(), target.hashCode(), expected_result.hashCode());
    }

    @Override
    public boolean equals(final Object other) {

        if (this == other) { return true; }
        if (!(other instanceof AvailabilityChangeEvent)) { return false; }
        final LookupEvent that = (LookupEvent) other;
        return super.equals(other) && target.equals(that.target) && expected_result.equals(that.expected_result);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LookupEvent{");
        sb.append("time=").append(getTimeInNanos());
        sb.append(", peer=").append(getSource().getKey());
        sb.append(", target=").append(target);
        sb.append(", expected_result=").append(expected_result);
        sb.append('}');
        return sb.toString();
    }
}
