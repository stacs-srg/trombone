package uk.ac.standrews.cs.trombone.core.state;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerReference;
import uk.ac.standrews.cs.trombone.core.Key;

/**
 * Chord's successor list implementation.
 *
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class ChordSuccessorList {

    private final List<PeerReference> successor_list;
    private final int max_size;
    private final Key local_key;
    private final Peer local;

    ChordSuccessorList(final Peer local, final int max_size) {

        if (max_size < 0) {
            throw new IllegalArgumentException("max_size cannot be negative");
        }

        this.local = local;
        this.max_size = max_size;
        local_key = local.key();
        successor_list = Collections.synchronizedList(new ArrayList<PeerReference>(max_size));
    }

    public List<PeerReference> values() {

        return new CopyOnWriteArrayList<>(successor_list);
    }

    public Stream<PeerReference> stream() {

        return successor_list.stream();
    }

    void clear() {

        successor_list.clear();
    }

    public void refresh(List<PeerReference> replacements) {

        final PeerReference self_reference = local.getSelfReference();
        final PeerReference successor = local.getPeerState()
                .first();

        successor_list.clear();
        successor_list.add(successor);

        final int successor_list_size = Math.min(replacements.size(), max_size - 1);
        for (int i = 0; i < successor_list_size; i++) {
            final PeerReference replacement = replacements.get(i);
            if (isReplacementSuitable(replacement)) {
                if (replacement.equals(self_reference)) { break;}
                successor_list.add(replacement);
            }
        }
    }

    private boolean isReplacementSuitable(final PeerReference replacement) {

        return replacement != null && !replacement.getKey()
                .equals(local_key);
    }

    public int getMaxSize() {

        return max_size;
    }

    public int size() {

        return successor_list.size();
    }
}
