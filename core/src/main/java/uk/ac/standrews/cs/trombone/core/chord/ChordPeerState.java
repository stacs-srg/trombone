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

package uk.ac.standrews.cs.trombone.core.chord;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;
import uk.ac.standrews.cs.trombone.core.InternalPeerReference;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerReference;
import uk.ac.standrews.cs.trombone.core.RoutingState;
import uk.ac.standrews.cs.trombone.core.key.Key;

/**
 * Implements a Chord node's state. This is consist of reference to successor, reference to predecessor, {@link ChordSuccessorList} and {@link ChordFingerTable}.
 *
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class ChordPeerState implements RoutingState {

    private volatile PeerReference successor;
    private volatile PeerReference predecessor;
    private final ChordSuccessorList successor_list;
    private final ChordFingerTable finger_table;
    private final Key local_key;
    private final Peer local_node;

    public ChordPeerState(final Peer local_node, final int finger_table_size, final BigInteger inter_finger_ratio, final int successor_list_size) {

        this.local_node = local_node;
        local_key = local_node.getKeySync();
        finger_table = new ChordFingerTable(local_node, finger_table_size, inter_finger_ratio);
        successor_list = new ChordSuccessorList(local_node, successor_list_size);

        local_node.addExposureChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(final PropertyChangeEvent evt) {

                if (evt.getNewValue().equals(true)) {
                    finger_table.clear();
                    createRing();
                }
            }
        });
    }

    @Override
    public PeerReference first() {

        return successor;
    }

    @Override
    public PeerReference last() {

        return predecessor;
    }

    @Override
    public Stream<InternalPeerReference> stream() {

        return new ArrayList<InternalPeerReference>().stream();
    }

    @Override
    public boolean add(final PeerReference reference) {

        if (reference.isReachable()) {
            return notify(reference);
        }
        else {
            return finger_table.notifyFingerFailure(reference) != null;
        }
    }

    @Override
    public PeerReference remove(final PeerReference reference) {

        return finger_table.notifyFingerFailure(reference);
    }

    @Override
    public PeerReference closest(final Key target) {

        PeerReference closest;
        if (inLocalKeyRange(target)) {
            closest = local_node.getSelfReference();
        }
        else if (inSuccessorKeyRange(target)) {
            closest = successor;
        }
        else {
            try {
                closest = finger_table.closestPreceding(target);
            }
            catch (final RuntimeException e) {
                closest = successor;
            }
        }
        return closest;
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

        if (!key_of_potential_predecessor.equals(local_key) && (predecessor == null || inLocalKeyRange(key_of_potential_predecessor))) {
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

        return local_key.equals(target) || !predecessor_key.equals(target) && predecessor_key.compareRingDistance(target, local_key) < 0;
    }

    private boolean inSuccessorKeyRange(final Key target) {

        final Key successor_key = successor.getKey();

        return local_key.equals(successor_key) || !local_key.equals(target) && local_key.compareRingDistance(target, successor_key) >= 0;
        //        return RingArithmetic.inSegmentQuick(local_key, target, key);
    }

    @Override
    public int size() {

        return finger_table.getFingerCount();
    }

    @Override
    public List<PeerReference> getReferences() {

        return new CopyOnWriteArrayList<>(finger_table.getFingers());
    }

    public void reset() {

        setSuccessor(local_node.getSelfReference());
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

    PeerReference getSuccessor() {

        return successor;
    }

    PeerReference getPredecessor() {

        return predecessor;
    }

    public boolean setSuccessor(final PeerReference new_successor) {

        if (new_successor == null) {
            throw new RuntimeException("Null successor on " + local_key);
        }
        final PeerReference old_successor = successor;
        successor = new_successor;

        return old_successor != null && !old_successor.equals(new_successor);
    }

    boolean setPredecessor(final PeerReference new_predecessor) {

        final PeerReference old_predecessor = predecessor;
        predecessor = new_predecessor;

        return new_predecessor == null || !new_predecessor.equals(old_predecessor);
    }

    /**
     * Checks whether this node's successor is itself, i.e. whether it is in a one-node ring.
     *
     * @return true if this node's successor is itself
     */
    boolean isSuccessorSelf() {

        return successor.getKey().equals(local_key);
    }

    /** Sets data structures for a new ring. */
    private synchronized void createRing() {

        setPredecessor(null);
        setSuccessor(local_node.getSelfReference());
        successor_list.clear();
    }
}
