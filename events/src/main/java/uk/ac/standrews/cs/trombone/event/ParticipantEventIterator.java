package uk.ac.standrews.cs.trombone.event;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.mashti.gauge.util.LongAdder;
import uk.ac.standrews.cs.trombone.core.key.Key;
import uk.ac.standrews.cs.trombone.event.churn.Churn;
import uk.ac.standrews.cs.trombone.event.churn.Workload;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class ParticipantEventIterator implements Iterator<Event>, Comparable<ParticipantEventIterator> {

    private final LongAdder current_time_nanos = new LongAdder();
    private final Participant participant;
    private final long experiment_duration_nanos;
    private final Churn churn;
    private final Workload workload;
    private final int hashcode;
    private final Random random;
    private boolean available;
    private long session_end_time;

    public ParticipantEventIterator(Participant participant, long experiment_duration_nanos, final Random random) {

        this.participant = participant;
        this.experiment_duration_nanos = experiment_duration_nanos;
        this.random = random;
        churn = participant.getChurn();
        workload = participant.getWorkload();
        hashcode = new HashCodeBuilder(23, 91).append(participant).append(experiment_duration_nanos).toHashCode();
    }

    @Override
    public synchronized boolean hasNext() {

        return !(isTimeUp() && !available);
    }

    @Override
    public synchronized Event next() {

        final Event next_event;
        if (isTimeUp()) {
            next_event = lastEvent();
        }
        else if (!available || getCurrentTime() == session_end_time) {
            next_event = nextJoinLeaveEvent();
        }
        else {
            next_event = nextLookupEvent();
        }

        return next_event;
    }

    public Long getCurrentTime() {

        return current_time_nanos.longValue();
    }

    @Override
    public void remove() {

        throw new UnsupportedOperationException();
    }

    @Override
    public int compareTo(final ParticipantEventIterator other) {

        final int time_comparison = getCurrentTime().compareTo(other.getCurrentTime());
        return time_comparison != 0 ? time_comparison : participant.compareTo(other.participant);
    }

    @Override
    public boolean equals(final Object other) {

        if (this == other) { return true; }
        if (!(other instanceof ParticipantEventIterator)) { return false; }

        final ParticipantEventIterator that = (ParticipantEventIterator) other;
        if (!participant.equals(that.participant)) { return false; }
        return getCurrentTime().equals(that.getCurrentTime());
    }

    @Override
    public int hashCode() {

        return hashcode;
    }

    private synchronized Event nextLookupEvent() {

        assert available;
        final long current_time = current_time_nanos.longValue();
        final Key target = workload.getTargetKeyAt(current_time);
        final long interval = workload.getIntervalAt(current_time);
        final long occurrence_time = Math.abs(current_time + interval);

        final Event next_event;
        if (session_end_time > occurrence_time) {
            current_time_nanos.add(interval);
            next_event = new LookupEvent(participant, occurrence_time, target);
        }
        else {
            current_time_nanos.add(Math.max(0, session_end_time - current_time));
            next_event = next();
        }
        return next_event;
    }

    private Event lastEvent() {

        assert isTimeUp();

        if (available) {
            available = false;
            return new LeaveEvent(participant, experiment_duration_nanos);
        }

        throw new NoSuchElementException();
    }

    private synchronized Event nextJoinLeaveEvent() {

        assert !isTimeUp();
        final Long current_time = getCurrentTime();

        available = isFirstEvent(current_time) ? churn.getAvailabilityAt(0).nextEvent(random) : !available;

        if (available) {
            final long session_length = normalize(churn.getSessionLengthAt(current_time));
            session_end_time = current_time + session_length;
            return new JoinEvent(participant, current_time, session_length);
        }
        else {
            final long downtime = normalize(churn.getDowntimeAt(current_time));
            current_time_nanos.add(downtime);
            return new LeaveEvent(participant, current_time);
        }
    }

    private static boolean isFirstEvent(final Long current_time) {

        return current_time == 0;
    }

    private long normalize(long duration_nanos) {

        return Math.min(duration_nanos, getRemainingExperimentTime());
    }

    private long getRemainingExperimentTime() {

        return experiment_duration_nanos - getCurrentTime();
    }

    private boolean isTimeUp() {

        return getCurrentTime() >= experiment_duration_nanos;
    }
}
