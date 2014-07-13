package uk.ac.standrews.cs.trombone.event;

import java.util.Arrays;
import java.util.Set;
import uk.ac.standrews.cs.trombone.core.PeerReference;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class JoinEvent extends Event {

    private static final long serialVersionUID = 82641656657049852L;
    private Set<PeerReference> known_peer_references;

    JoinEvent(final Participant source, long time_nanos) {

        super(source, time_nanos);
    }

    public void setKnownPeerReferences(final Set<PeerReference> known_peer_references) {

        this.known_peer_references = known_peer_references;
    }

    public Set<PeerReference> getKnownPeerReferences() {

        return known_peer_references;
    }

    @Override
    public boolean equals(final Object other) {

        if (this == other) { return true; }
        if (!(other instanceof JoinEvent)) { return false; }
        if (!super.equals(other)) { return false; }

        final JoinEvent joinEvent = (JoinEvent) other;
        return known_peer_references.equals(joinEvent.known_peer_references);
    }

    @Override
    public int hashCode() {

        return 31 * super.hashCode() + known_peer_references.hashCode();
    }

    @Override
    public String toString() {

        return "JoinEvent{" + "time=" + getTimeInNanos() + ", peer=" + getSource() + ", known_peer_references=" + Arrays.toString(known_peer_references.toArray()) + '}';
    }
}
