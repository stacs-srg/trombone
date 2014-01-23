package uk.ac.standrews.cs.trombone.event;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicLong;
import uk.ac.standrews.cs.trombone.event.churn.Churn;
import uk.ac.standrews.cs.trombone.core.key.Key;
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
    private volatile ChurnEvent last_availability;

    public ParticipantEventIterator(Participant participant, long experiment_duration_nanos) {

        this.participant = participant;
        this.experiment_duration_nanos = experiment_duration_nanos;
        churn = participant.getChurn();
        workload = participant.getWorkload();
    }

    @Override
    public synchronized boolean hasNext() {

        return !isTimeUp();
    }

    @Override
    public synchronized Event next() {

        final long event_time = current_time_nanos.get();
        if (isTimeUp()) {
            if (!last_availability.isAvailable()) {
                throw new NoSuchElementException();
            }
            else {
                last_availability = getChurnEventAt(event_time);
                current_time_nanos.set(experiment_duration_nanos);
                return last_availability;
            }
        }

        if (last_availability == null || !last_availability.isAvailable() || !last_availability.isWithin(event_time)) {
            last_availability = getChurnEventAt(event_time);

            if (!last_availability.isAvailable()) {
                current_time_nanos.addAndGet(last_availability.getDurationInNanos());
            }
            return last_availability;
        }

        final Workload.Lookup lookup = workload.getLookupAt(event_time);
        final Key target = lookup.getTarget();
        final long interval = lookup.getIntervalInNanos();
        final long lookup_time = event_time + interval;

        if (last_availability.isWithin(lookup_time)) {
            current_time_nanos.addAndGet(interval);
            return new LookupEvent(participant, lookup_time, target);
        }
        else {
            current_time_nanos.set(last_availability.getEndTimeNanos());
        }

        return next();
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

        return true;
    }

    @Override
    public int hashCode() {

        return 31 * participant.hashCode();
    }

    @Override
    public String toString() {

        return "generator " + participant.getId();
    }

    private ChurnEvent getChurnEventAt(final long time) {

        if (time >= experiment_duration_nanos) {
            return new ChurnEvent(participant, experiment_duration_nanos, false);
        }

        Churn.Availability availability = churn.getAvailabilityAt(time);
        final long duration_nanos = Math.min(availability.getDurationInNanos(), experiment_duration_nanos - time);
        final boolean available = availability.isAvailable();
        final ChurnEvent current_availability = new ChurnEvent(participant, time, available);
        current_availability.setDurationInNanos(duration_nanos);
        return current_availability;
    }

    private boolean isTimeUp() {

        return experiment_duration_nanos <= current_time_nanos.get();
    }
}
