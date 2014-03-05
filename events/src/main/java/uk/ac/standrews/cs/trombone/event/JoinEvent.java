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

    public static final String PARAMETER_DELIMITER = " ";
    static final int JOIN_EVENT_CODE = 1;
    private static final long serialVersionUID = 82641656657049852L;
    private Long duration_nanos;
    private Set<Participant> known_peers;
    private Set<PeerReference> known_peer_references;

    JoinEvent(final Participant source, long time_nanos) {

        super(source, time_nanos);
    }

    JoinEvent(final Participant source, long time_nanos, long session_duration) {

        super(source, time_nanos);
        setDurationInNanos(session_duration);
    }

    JoinEvent(final PeerReference source, Integer source_id, long occurrence_time_nanos) {

        super(source, source_id, occurrence_time_nanos);
    }

    @Override
    public int hashCode() {

        return new HashCodeBuilder().append(getCode()).appendSuper(super.hashCode()).toHashCode();
    }

    @Override
    public boolean equals(final Object other) {

        if (this == other) { return true; }
        if (!(other instanceof JoinEvent)) { return false; }
        final JoinEvent that = (JoinEvent) other;

        return known_peers.equals(that.known_peers) && super.equals(other);
    }

    @Override
    public String toString() {

        final StringBuilder sb = new StringBuilder("JoinEvent{");
        sb.append("time=").append(getTimeInNanos());
        sb.append(", peer=").append(getSource());
        sb.append(", known_peers=").append(Arrays.toString(known_peers.toArray()));
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

        this.known_peers = known_peers;
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

    @Override
    int getCode() {

        return JOIN_EVENT_CODE;
    }

    @Override
    String getParameters() {

        if (duration_nanos == null) { throw new IllegalArgumentException("duration must be specified when peer is available"); }

        final StringBuilder parameters = new StringBuilder();
        parameters.append(duration_nanos);
        parameters.append(PARAMETER_DELIMITER);
        for (Participant known_peer : known_peers) {
            parameters.append(known_peer.getId());
            parameters.append(PARAMETER_DELIMITER);
        }
        return parameters.toString().trim();
    }

    void setDurationInNanos(Long duration_nanos) {

        this.duration_nanos = duration_nanos;
    }

    Long getDurationInNanos() {

        return duration_nanos;
    }
}
