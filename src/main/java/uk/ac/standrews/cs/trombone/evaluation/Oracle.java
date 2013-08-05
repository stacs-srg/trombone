package uk.ac.standrews.cs.trombone.evaluation;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import uk.ac.standrews.cs.trombone.key.Key;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class Oracle {

    private final NavigableMap<Long, NavigableSet<Key>> reachable_peers;
    private final Set<Participant> participants;
    private final File home;

    public Oracle(SortedSet<AvailabilityChangeEvent> availability_events, Set<Participant> participants, File home) throws IOException {

        this.participants = participants;
        this.home = home;
        reachable_peers = generateReachablePeersMap(availability_events);
    }

    public Participant lookupAtTime(Key target, long time) {

        final NavigableSet<Key> full_state_at_time = getAlivePeerKeysAtTime(time);
        if (full_state_at_time.isEmpty()) { throw new IllegalStateException("no peer is alive at the given time and a lookup is happening?! something is wrong"); }
        Key entry = full_state_at_time.ceiling(target);
        if (entry == null) {
            entry = full_state_at_time.first();
        }

        return getParticipantByKey(entry);
    }

    private NavigableSet<Key> getAlivePeerKeysAtTime(final long time) {

        final Map.Entry<Long, NavigableSet<Key>> time_of_interest = reachable_peers.floorEntry(time);
        if (time_of_interest == null) { throw new IllegalStateException("no peer is alive at the given time and a lookup is happening?! something is wrong"); }
        return time_of_interest.getValue();
    }

    private Participant getParticipantByKey(final Key key) {

        for (Participant participant : participants) {
            if (participant.getKey().equals(key)) { return participant; }
        }

        throw new IllegalStateException("no participant found with key " + key);
    }

    private NavigableMap<Long, NavigableSet<Key>> generateReachablePeersMap(final SortedSet<AvailabilityChangeEvent> availability_events) throws IOException {

        TreeMap<Long, NavigableSet<Key>> map = new TreeMap<Long, NavigableSet<Key>>();
        NavigableSet<Key> alive_peers = new TreeSet<Key>();

        for (AvailabilityChangeEvent event : availability_events) {

            final Key peer = event.getSource().getKey();
            final boolean changed = event.isAvailable() ? alive_peers.add(peer) : alive_peers.remove(peer);

            if (changed) {
                map.put(event.getTimeInNanos(), new TreeSet<Key>(alive_peers));
            }
        }

        return map;
    }
}
