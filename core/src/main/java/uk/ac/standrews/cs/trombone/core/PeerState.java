package uk.ac.standrews.cs.trombone.core;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;
import uk.ac.standrews.cs.trombone.core.key.Key;
import uk.ac.standrews.cs.trombone.core.util.RelativeRingDistanceComparator;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class PeerState implements Iterable<InternalPeerReference> {

    private final Key local_key;
    private final ConcurrentSkipListMap<Key, InternalPeerReference> state;

    public PeerState(final Key local_key) {

        this.local_key = local_key;
        state = new ConcurrentSkipListMap<Key, InternalPeerReference>(new RelativeRingDistanceComparator(local_key));
    }

    public InternalPeerReference getInternalReference(final PeerReference reference) {

        final Key key = reference.getKey();
        add(reference);
        return state.get(key);
    }

    public boolean inLocalKeyRange(Key target) {

        final PeerReference last_reachable = lastReachable();
        return last_reachable == null || local_key.equals(target) || last_reachable.getKey().compareRingDistance(local_key, target) > 0;
    }

    public boolean add(final PeerReference reference) {

        if (reference == null) { return false; }
        final Key key = reference.getKey();
        if (key.equals(local_key)) { return false; }
        final InternalPeerReference internal_reference = toInternalPeerReference(reference);
        final InternalPeerReference existing_reference = state.putIfAbsent(key, internal_reference);

        if (existing_reference != null) {

            existing_reference.setReachable(reference.isReachable());
        }

        return existing_reference == null;
    }

    public PeerReference remove(PeerReference reference) {

        return state.remove(reference.getKey());
    }

    public PeerReference lower(final Key target) {

        final Map.Entry<Key, InternalPeerReference> lower_entry = state.lowerEntry(target);
        return getEntryValue(lower_entry);
    }

    public PeerReference higher(final Key target) {

        final Map.Entry<Key, InternalPeerReference> higher_entry = state.higherEntry(target);
        return getEntryValue(higher_entry);
    }

    public PeerReference[] topReachable(final int size) {

        final PeerReference[] references = new PeerReference[size];
        final Iterator<InternalPeerReference> iterator = state.values().iterator();
        int index = 0;
        while (iterator.hasNext() && index < size) {
            final InternalPeerReference next = iterator.next();
            if (next.isReachable()) {
                references[index] = next;
                index++;
            }
        }
        return references;
    }

    public PeerReference[] bottomReachable(final int size) {

        final PeerReference[] references = new PeerReference[size];
        final Iterator<Key> iterator = state.descendingKeySet().iterator();
        int index = 0;
        while (iterator.hasNext() && index < size) {
            final InternalPeerReference next = state.get(iterator.next());
            if (next.isReachable()) {
                references[index] = next;
                index++;
            }
        }
        return references;
    }

    public PeerReference ceilingReachable(final Key target) {

        PeerReference ceiling = ceiling(target);
        while (ceiling != null && !ceiling.isReachable()) {
            ceiling = lower(ceiling.getKey());
        }
        return ceiling;
    }

    public PeerReference ceiling(final Key target) {

        final Map.Entry<Key, InternalPeerReference> ceiling_entry = state.ceilingEntry(target);
        return getEntryValue(ceiling_entry);
    }

    public PeerReference first() {

        return getEntryValue(state.firstEntry());
    }

    public PeerReference firstReachable() {

        PeerReference first = first();
        while (first != null && !first.isReachable()) {
            first = lower(first.getKey());
        }
        return first;
    }

    public PeerReference last() {

        return getEntryValue(state.lastEntry());
    }

    public PeerReference lastReachable() {

        PeerReference last = last();
        while (last != null && !last.isReachable()) {
            last = higher(last.getKey());
        }

        return last;
    }

    public List<PeerReference> getReferences() {

        return new CopyOnWriteArrayList<PeerReference>(state.values());
    }

    public int size() {

        //TODO this is expensive; implement internal counter
        return state.size();
    }

    @Override
    public Iterator<InternalPeerReference> iterator() {

        return state.values().iterator();
    }

    private static InternalPeerReference toInternalPeerReference(final PeerReference reference) {

        return reference instanceof InternalPeerReference ? (InternalPeerReference) reference : new InternalPeerReference(reference);
    }

    private static InternalPeerReference getEntryValue(final Map.Entry<Key, InternalPeerReference> entry) {

        return entry != null ? entry.getValue() : null;
    }
}
