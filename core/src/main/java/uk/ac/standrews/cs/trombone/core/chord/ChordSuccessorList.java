package uk.ac.standrews.cs.trombone.core.chord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerReference;
import uk.ac.standrews.cs.trombone.core.key.Key;

/**
 * Chord's successor list implementation.
 *
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class ChordSuccessorList {

    private final List<PeerReference> successor_list;
    private final int max_size;
    private final Key local_key;

    ChordSuccessorList(final Peer local_node, final int max_size) {

        local_key = local_node.getKeySync();
        this.max_size = max_size;
        successor_list = Collections.synchronizedList(new ArrayList<PeerReference>(max_size));
    }

    public List<PeerReference> values() {

        return new CopyOnWriteArrayList<>(successor_list);
    }

    PeerReference findFirstReachableSuccessor() {

        return successor_list.stream().filter(successor -> successor.isReachable()).findFirst().get();
    }

    void clear() {

        successor_list.clear();
    }

    void refresh(List<PeerReference> replacements) {

        for (int i = 0; i < replacements.size(); i++) {
            final PeerReference replacement = replacements.get(i);
            if (isReplacementSuitable(replacement)) {
                successor_list.set(i, replacement);
            }
        }
    }

    private boolean isReplacementSuitable(final PeerReference replacement) {

        return replacement != null && !replacement.getKey().equals(local_key);
    }

    public int getMaxSize() {

        return max_size;
    }

    public int size() {

        return successor_list.size();
    }
}
