package uk.ac.standrews.cs.trombone.core.state;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;
import uk.ac.standrews.cs.trombone.core.InternalPeerReference;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerReference;
import uk.ac.standrews.cs.trombone.core.key.Key;
import uk.ac.standrews.cs.trombone.core.key.RingArithmetic;
import uk.ac.standrews.cs.trombone.core.util.RelativeRingDistanceComparator;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class TrombonePeerState implements PeerState {

    private final Key local_key;
    private final ConcurrentSkipListMap<Key, InternalPeerReference> state;

    public TrombonePeerState(final Peer local) {

        this(local.key());
    }

    public TrombonePeerState(final Key local_key) {

        this.local_key = local_key;
        state = new ConcurrentSkipListMap<Key, InternalPeerReference>(new RelativeRingDistanceComparator(local_key));
    }

    public InternalPeerReference getInternalReference(final PeerReference reference) {

        final Key key = reference.getKey();
        add(reference);
        return state.get(key);
    }

    @Override
    public boolean inLocalKeyRange(Key target) {

        final PeerReference last_reachable = lastReachable();
        return last_reachable == null || RingArithmetic.inSegment(last_reachable.getKey(), target, local_key);
    }

    @Override
    public boolean add(final PeerReference reference) {

        if (reference == null) { return false; }
        final Key key = reference.getKey();
        if (key.equals(local_key)) { return false; }
        final InternalPeerReference internal_reference = toInternalPeerReference(reference);
        final InternalPeerReference existing_reference = state.putIfAbsent(key, internal_reference);

        final boolean already_existed = existing_reference != null;
        if (already_existed) {
            existing_reference.setReachable(reference.isReachable());
        }

        return !already_existed;
    }

    @Override
    public PeerReference remove(PeerReference reference) {

        return state.remove(reference.getKey());
    }

    @Override
    public PeerReference closest(final Key target) {

        return ceilingReachable(target);
    }

    public PeerReference lower(final Key target) {

        final Map.Entry<Key, InternalPeerReference> lower_entry = state.lowerEntry(target);
        return getEntryValue(lower_entry);
    }

    public PeerReference higher(final Key target) {

        final Map.Entry<Key, InternalPeerReference> higher_entry = state.higherEntry(target);
        return getEntryValue(higher_entry);
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

    @Override
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

    @Override
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

    @Override
    public int size() {

        return state.size();
    }

    @Override
    public List<PeerReference> getReferences() {

        return new CopyOnWriteArrayList<>(state.values());
    }

    @Override
    public Stream<PeerReference> stream() {

        return state.values()
                .stream()
                .map(internal -> internal);
    }

    public Collection<InternalPeerReference> getValues() {

        return state.values();
    }

    public List<InternalPeerReference> getInternalReferences() {

        return new CopyOnWriteArrayList<>(getValues());
    }

    private static InternalPeerReference toInternalPeerReference(final PeerReference reference) {

        return reference instanceof InternalPeerReference ? (InternalPeerReference) reference : new InternalPeerReference(reference);
    }

    private static InternalPeerReference getEntryValue(final Map.Entry<Key, InternalPeerReference> entry) {

        return entry != null ? entry.getValue() : null;
    }
}
