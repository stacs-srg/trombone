package uk.ac.standrews.cs.trombone.trombone;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;
import uk.ac.standrews.cs.trombone.trombone.key.Key;
import uk.ac.standrews.cs.trombone.trombone.util.RelativeRingDistanceComparator;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class PeerState {

    private final Key key;
    private final ConcurrentSkipListMap<Key, InternalPeerReference> state;

    public PeerState(final Key key) {

        this.key = key;
        state = new ConcurrentSkipListMap<Key, InternalPeerReference>(new RelativeRingDistanceComparator(key));
    }

    public InternalPeerReference getInternalReference(final PeerReference reference) {

        //TODO is this the most efficient way of writing this
        final Key key = reference.getKey();
        final InternalPeerReference internal_reference = toInternalPeerReference(reference);
        final InternalPeerReference added_reference = state.putIfAbsent(key, internal_reference);
        return added_reference != null ? added_reference : internal_reference;
    }

    public boolean inLocalKeyRange(Key target) {

        final PeerReference last = last();
        return last == null || key.equals(target) || last.getKey().compareRingDistance(key, target) > 0;
    }

    public boolean add(final PeerReference reference) {

        final Key key = reference.getKey();
        if (key.equals(this.key)) { return false; }
        final InternalPeerReference internal_reference = toInternalPeerReference(reference);
        return state.putIfAbsent(key, internal_reference) == null;
    }

    public PeerReference lower(final Key target) {

        final Map.Entry<Key, InternalPeerReference> lower_entry = state.lowerEntry(target);
        return getEntryValue(lower_entry);
    }

    public PeerReference[] top(final int n) {

        final int size = Math.min(n, size());
        final PeerReference[] references = new PeerReference[size];
        final Iterator<InternalPeerReference> iterator = state.values().iterator();
        int index = 0;
        while (iterator.hasNext() && index < size) {
            references[index] = iterator.next();
            index++;
        }
        return references;
    }

    public PeerReference[] bottom(final int n) {

        final int size = Math.min(n, size());
        final PeerReference[] references = new PeerReference[size];
        final Iterator<Key> iterator = state.descendingKeySet().iterator();
        int index = 0;
        while (iterator.hasNext() && index < size) {
            references[index] = state.get(iterator.next());
            index++;
        }
        return references;
    }

    public PeerReference ceiling(final Key target) {

        final Map.Entry<Key, InternalPeerReference> ceiling_entry = state.ceilingEntry(target);
        return getEntryValue(ceiling_entry);
    }

    public PeerReference first() {

        return getEntryValue(state.firstEntry());
    }

    public List<PeerReference> getReferences() {

        return new CopyOnWriteArrayList<PeerReference>(state.values());
    }

    public PeerReference last() {

        return getEntryValue(state.lastEntry());
    }

    public int size() {

        //TODO this is expensive; implement internal counter
        return state.size();
    }

    private static InternalPeerReference toInternalPeerReference(final PeerReference reference) {

        return reference instanceof InternalPeerReference ? (InternalPeerReference) reference : new InternalPeerReference(reference);
    }

    private static InternalPeerReference getEntryValue(final Map.Entry<Key, InternalPeerReference> entry) {

        return entry != null ? entry.getValue() : null;
    }
}
