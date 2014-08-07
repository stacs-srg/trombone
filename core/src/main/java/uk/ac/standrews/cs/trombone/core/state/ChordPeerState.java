/*
 * Copyright 2013 Masih Hajiarabderkani
 *
 * This file is part of Trombone.
 *
 * Trombone is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Trombone is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Trombone.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.standrews.cs.trombone.core.state;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigInteger;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import uk.ac.standrews.cs.trombone.core.Key;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerReference;

/**
 * Implements a Chord node's state. This is consist of reference to successor, reference to predecessor, {@link ChordSuccessorList} and {@link ChordFingerTableMapBased}.
 *
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class ChordPeerState implements PeerState {

    private final AtomicReference<PeerReference> successor;
    private final AtomicReference<PeerReference> predecessor;
    private final ChordSuccessorList successor_list;
    private final ChordFingerTable finger_table;
    private final Key local_key;
    private final Peer local;

    ChordPeerState(final Peer local, final int finger_table_size, final BigInteger inter_finger_ratio, final int successor_list_size) {

        this.local = local;
        local_key = local.key();
        successor = new AtomicReference<>();
        predecessor = new AtomicReference<>();

        //        finger_table = new ChordFingerTableArrayBased(local, finger_table_size, inter_finger_ratio);
        finger_table = new ChordFingerTableMapBased(local, finger_table_size, inter_finger_ratio);
        successor_list = new ChordSuccessorList(local, successor_list_size);

        local.addExposureChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(final PropertyChangeEvent evt) {

                if (evt.getNewValue()
                        .equals(true)) {
                    finger_table.clear();
                    createRing();
                }
            }
        });
    }

    @Override
    public PeerReference first() {

        return successor.get();
    }

    @Override
    public PeerReference last() {

        return predecessor.get();
    }

    @Override
    public Stream<PeerReference> stream() {

        return finger_table.getFingers()
                .stream();
    }

    @Override
    public boolean add(final PeerReference reference) {

        if (reference.isReachable()) {
            return notify(reference);
        }
        else {
            return finger_table.notifyFailure(reference) != null;
        }
    }

    @Override
    public PeerReference remove(final PeerReference reference) {

        return finger_table.notifyFailure(reference);
    }

    @Override
    public PeerReference closest(final Key target) {

        try {
            return finger_table.closestPreceding(target);
        }
        catch (NoSuchElementException e) {
            return getSuccessor();
        }
    }

    public boolean notify(final PeerReference potential_predecessor) {

        /* Case: predecessor is null and potential_predecessor is this node.
           We have a one-node ring, and the predecessor should stay null.

           Case: predecessor is null and potential_predecessor is not this node.
           Ring has at least two nodes, so any predecessor is better than nothing.

           Case: predecessor is not null and potential_predecessor is this node.
           Ignore, since a node's predecessor is never itself.

           Case: predecessor is not null and potential_predecessor is not in this node's current key range.
           Ignore, since the current predecessor doesn't appear to have failed, so only valid case is a
           new node joining.

           Case: predecessor is not null and potential_predecessor is in this node's current key range.
           A new node has joined between the current predecessor and this node.
         */
        final Key key_of_potential_predecessor = potential_predecessor.getKey();

        if (!key_of_potential_predecessor.equals(local_key) && (predecessor.get() == null || inLocalKeyRange(key_of_potential_predecessor))) {
            return setPredecessor(potential_predecessor);
        }
        return false;
    }

    @Override
    public boolean inLocalKeyRange(final Key target) {

        // It's possible that predecessor and successor will change during execution of this method, leading to transiently
        // incorrect results. We don't care about this, so only synchronize enough of the method to avoid NPEs.

        final Key predecessor_key;
        final boolean successor_is_self;

        synchronized (this) {
            final PeerReference predecessor = getPredecessor();
            // getKey() is a non-open call, holding lock on this node.
            // It may make a remote getKey() call on the predecessor, which doesn't require any further locks.
            predecessor_key = predecessor != null ? predecessor.getKey() : null;
            successor_is_self = isSuccessorSelf();
        }

        if (predecessor_key == null) {
            if (successor_is_self) { return true; }

            // No predecessor and successor not self, so not a one-node ring - don't know local key range.
            throw new RuntimeException("Unable to determine local key range because the predecessor is null");
        }

        return Key.inSegment(predecessor_key, target, local_key);
    }

    @Override
    public int size() {

        return finger_table.size();
    }

    @Override
    public List<PeerReference> getReferences() {

        return new CopyOnWriteArrayList<>(finger_table.getFingers());
    }

    public void reset() {

        setSuccessor(local.getSelfReference());
        setPredecessor(null);
        finger_table.clear();
        successor_list.clear();
    }

    /**
     * Gets the real successor list.
     *
     * @return the successor list
     */
    public ChordSuccessorList getSuccessorList() {

        return successor_list;
    }

    /**
     * Gets the real finger table.
     *
     * @return the finger table
     */
    public ChordFingerTable getFingerTable() {

        return finger_table;
    }

    public PeerReference getSuccessor() {

        return successor.get();
    }

    public PeerReference getPredecessor() {

        return predecessor.get();
    }

    public boolean setSuccessor(final PeerReference new_successor) {

        if (new_successor == null) {
            throw new RuntimeException("Null successor on " + local_key);
        }
        final PeerReference old_successor = successor.getAndSet(new_successor);
        return !new_successor.equals(old_successor);
    }

    public boolean setPredecessor(final PeerReference new_predecessor) {

        final PeerReference old_predecessor = predecessor.getAndSet(new_predecessor);
        return old_predecessor != new_predecessor && old_predecessor != null && !old_predecessor.equals(new_predecessor);
    }

    /**
     * Checks whether this node's successor is itself, i.e. whether it is in a one-node ring.
     *
     * @return true if this node's successor is itself
     */
    boolean isSuccessorSelf() {

        return successor.get()
                .getKey()
                .equals(local_key);
    }

    /** Sets data structures for a new ring. */
    private synchronized void createRing() {

        setPredecessor(null);
        setSuccessor(local.getSelfReference());
        successor_list.clear();
    }
}
