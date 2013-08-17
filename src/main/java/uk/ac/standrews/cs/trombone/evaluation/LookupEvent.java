package uk.ac.standrews.cs.trombone.evaluation;

import uk.ac.standrews.cs.shabdiz.util.HashCodeUtil;
import uk.ac.standrews.cs.trombone.PeerReference;
import uk.ac.standrews.cs.trombone.key.Key;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class LookupEvent extends Event {

    static final int LOOKUP_EVENT_CODE = 2;
    private static final long serialVersionUID = 8171231149854930079L;
    private final Key target;
    private PeerReference expected_result;
    private Integer expected_result_id;
    private Integer target_id;

    LookupEvent(final Participant source, final Long time_nanos, final Key target) {

        super(source, time_nanos);
        this.target = target;
    }

    LookupEvent(final PeerReference source, Integer source_id, final Long time_nanos, final Key target) {

        super(source, source_id, time_nanos);
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
        expected_result_id = expected_result.getId();
    }

    public void setExpectedResult(final PeerReference expected_result, Integer expected_result_id) {
        this.expected_result = expected_result;
        this.expected_result_id = expected_result_id;
    }

    @Override
    public int hashCode() {

        return HashCodeUtil.generate(super.hashCode(), target.hashCode(), expected_result.hashCode());
    }

    @Override
    public boolean equals(final Object other) {

        if (this == other) { return true; }
        if (!(other instanceof ChurnEvent)) { return false; }
        final LookupEvent that = (LookupEvent) other;
        return super.equals(other) && target.equals(that.target) && expected_result.equals(that.expected_result);
    }

    @Override
    int getCode() {

        return LOOKUP_EVENT_CODE;
    }

    @Override
    String getParameters() {
        if (target_id == null) { throw new NullPointerException("target id is null"); }
        return target_id + ":" + expected_result_id;
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

    public void setTargetId(Integer target_id) {

        this.target_id = target_id;
    }
}
