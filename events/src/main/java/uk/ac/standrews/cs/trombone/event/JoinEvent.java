package uk.ac.standrews.cs.trombone.event;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang.builder.HashCodeBuilder;
import uk.ac.standrews.cs.trombone.core.PeerReference;

/**
 * Presents the change of a peer's availability at {@code t} nanoseconds through an experiment.
 *
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class JoinEvent extends Event {

    private static final long serialVersionUID = 82641656657049852L;
    private Long duration_nanos;
    private Set<PeerReference> known_peer_references;

    JoinEvent(final Participant source, long time_nanos) {

        super(source, time_nanos);
    }

    JoinEvent(final Participant source, long time_nanos, long session_duration) {

        super(source, time_nanos);
        setDurationInNanos(session_duration);
    }

    @Override
    public int hashCode() {

        return new HashCodeBuilder().append(duration_nanos).appendSuper(super.hashCode()).toHashCode();
    }

    @Override
    public boolean equals(final Object other) {

        if (this == other) { return true; }
        if (!(other instanceof JoinEvent)) { return false; }
        final JoinEvent that = (JoinEvent) other;

        return duration_nanos == that.duration_nanos && super.equals(other);
    }

    @Override
    public String toString() {

        final StringBuilder sb = new StringBuilder("JoinEvent{");
        sb.append("time=").append(getTimeInNanos());
        sb.append(", peer=").append(getSource());
        sb.append(", known_peer_references=").append(Arrays.toString(known_peer_references.toArray()));
        sb.append('}');
        return sb.toString();
    }

    public boolean isWithin(final long time_nanos) {

        return getTimeInNanos() <= time_nanos && time_nanos < getEndTimeNanos();

    }

    public long getEndTimeNanos() {

        return getTimeInNanos() + getDurationInNanos();
    }

    public void setKnownPeers(final Set<Participant> known_peers) {

        Set<PeerReference> references = new HashSet<>();
        for (Participant known_peer : known_peers) {

            references.add(known_peer.getReference());
        }
        setKnownPeerReferences(references);
    }

    public void setKnownPeerReferences(final Set<PeerReference> known_peer_references) {

        this.known_peer_references = known_peer_references;
    }

    public Set<PeerReference> getKnownPeerReferences() {

        return known_peer_references;
    }

    void setDurationInNanos(Long duration_nanos) {

        this.duration_nanos = duration_nanos;
    }

    Long getDurationInNanos() {

        return duration_nanos;
    }
}
