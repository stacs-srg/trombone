package uk.ac.standrews.cs.trombone.event;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.lang.builder.HashCodeBuilder;
import uk.ac.standrews.cs.trombone.core.key.Key;
import uk.ac.standrews.cs.trombone.event.churn.Churn;
import uk.ac.standrews.cs.trombone.event.workload.Workload;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class ParticipantEventIterator implements Iterator<Event>, Comparable<ParticipantEventIterator> {

    private final AtomicLong current_time_nanos = new AtomicLong(0);
    private final Participant participant;
    private final long experiment_duration_nanos;
    private final Churn churn;
    private final Workload workload;
    private final int hashcode;
    private boolean available;
    private long session_end_time;

    public ParticipantEventIterator(Participant participant, long experiment_duration_nanos) {

        this.participant = participant;
        this.experiment_duration_nanos = experiment_duration_nanos;
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

        return current_time_nanos.get();
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

    private Event nextLookupEvent() {

        assert available;
        final long current_time = current_time_nanos.get();
        final Workload.Lookup lookup = workload.getLookupAt(current_time);
        final Key target = lookup.getTarget();
        final long interval = lookup.getIntervalInNanos();
        final long occurrence_time = current_time + interval;

        final Event next_event;
        if (session_end_time > occurrence_time) {
            current_time_nanos.addAndGet(interval);
            next_event = new LookupEvent(participant, occurrence_time, target);
        }
        else {
            current_time_nanos.set(session_end_time);
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
        Churn.Availability availability = churn.getAvailabilityAt(current_time);
        final long duration_nanos = Math.min(availability.getDurationInNanos(), experiment_duration_nanos - current_time);
        available = availability.isAvailable();

        if (available) {
            session_end_time = current_time + duration_nanos;
            return new JoinEvent(participant, current_time, duration_nanos);
        }
        else {
            current_time_nanos.addAndGet(duration_nanos);
            return new LeaveEvent(participant, current_time);
        }
    }

    private boolean isTimeUp() {

        return getCurrentTime() >= experiment_duration_nanos;
    }
}
