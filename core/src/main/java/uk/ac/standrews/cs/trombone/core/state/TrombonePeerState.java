package uk.ac.standrews.cs.trombone.core.state;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;
import uk.ac.standrews.cs.trombone.core.Key;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerReference;
import uk.ac.standrews.cs.trombone.core.util.RelativeRingDistanceComparator;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class TrombonePeerState implements PeerState {

    private final Key local_key;
    private final ConcurrentSkipListMap<Key, PeerReference> state;
    private Peer local;

    TrombonePeerState(final Peer local) {

        this(local.key());
        this.local = local;

    }

    TrombonePeerState(final Key local_key) {

        this.local_key = local_key;
        state = new ConcurrentSkipListMap<Key, PeerReference>(new RelativeRingDistanceComparator(local_key));
    }

    @Override
    public boolean inLocalKeyRange(Key target) {

        final PeerReference last = last();
        if (last != null) {
            local.getAsynchronousRemote(last, false)
                    .push(local.getSelfReference());
        }
        return last == null || Key.inSegment(last.getKey(), target, local_key);
    }

    @Override
    public boolean add(final PeerReference reference) {

        if (reference == null) { return false; }
        final Key key = reference.getKey();
        if (key.equals(local_key)) { return false; }
        return state.putIfAbsent(key, reference) == null;
    }

    @Override
    public PeerReference remove(PeerReference reference) {

        return state.remove(reference.getKey());
    }

    @Override
    public PeerReference closest(final Key target) {

        return closestSuccessor(target);
    }

    private PeerReference closestSuccessor(final Key target) {

        final PeerReference ceiling = ceiling(target);
        return ceiling == null ? first() : ceiling;
    }

    public PeerReference lower(final Key target) {

        final Map.Entry<Key, PeerReference> lower_entry = state.lowerEntry(target);
        return getEntryValue(lower_entry);
    }

    public PeerReference higher(final Key target) {

        final Map.Entry<Key, PeerReference> higher_entry = state.higherEntry(target);
        return getEntryValue(higher_entry);
    }

    public PeerReference ceiling(final Key target) {

        final Map.Entry<Key, PeerReference> ceiling_entry = state.ceilingEntry(target);
        return getEntryValue(ceiling_entry);
    }

    public PeerReference floor(final Key target) {

        final Map.Entry<Key, PeerReference> floor_entry = state.floorEntry(target);
        return getEntryValue(floor_entry);
    }

    @Override
    public PeerReference first() {

        return getEntryValue(state.firstEntry());
    }

    @Override
    public PeerReference last() {

        return getEntryValue(state.lastEntry());
    }

    @Override
    public int size() {

        return state.size();
    }

    @Override
    public Collection<PeerReference> getReferences() {

        return state.values();
    }

    @Override
    public Stream<PeerReference> stream() {

        return state.values()
                .stream()
                .map(internal -> internal);
    }

    public Collection<PeerReference> getValues() {

        return state.values();
    }

    public List<PeerReference> getInternalReferences() {

        return new CopyOnWriteArrayList<>(getValues());
    }

    private static PeerReference getEntryValue(final Map.Entry<Key, PeerReference> entry) {

        return entry != null ? entry.getValue() : null;
    }
}
