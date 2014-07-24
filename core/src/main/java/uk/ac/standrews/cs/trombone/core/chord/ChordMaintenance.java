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

import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import uk.ac.standrews.cs.trombone.core.AsynchronousPeerRemote;
import uk.ac.standrews.cs.trombone.core.Maintenance;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerReference;
import uk.ac.standrews.cs.trombone.core.key.Key;
import uk.ac.standrews.cs.trombone.core.selector.Last;
import uk.ac.standrews.cs.trombone.core.selector.Selector;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class ChordMaintenance extends Maintenance {

    private static final Last PREDECESSOR_SELECTOR = new Last(1, Selector.ReachabilityCriteria.REACHABLE);
    private final ChordPeerState state;
    private final ChordSuccessorList successor_list;
    private final ChordSuccessorListSelector successor_list_selector;
    private final ChordFingerTable finger_table;
    private final Key local_key;
    private final ArrayDeque<Key> fix_finger_keys;

    public ChordMaintenance(final Peer peer, final ScheduledExecutorService scheduler) {

        super(peer, null, scheduler);
        state = (ChordPeerState) peer.getPeerState();
        successor_list = state.getSuccessorList();
        successor_list_selector = new ChordSuccessorListSelector(successor_list.getMaxSize());

        local_key = peer.getKeySync();
        finger_table = state.getFingerTable();
        fix_finger_keys = generateKeysLogarithmically(local_key, finger_table.getMaxSize(), finger_table.getBase(), 32);

    }

    @Override
    protected Runnable newNonOpportunisticDisseminator() {

        return new Runnable() {

            @Override
            public void run() {

                pingPredecessor();
                stabilizeRing();
                refreshSuccessorList();
                fixNextFinger();
            }

            private void fixNextFinger() {

                final Key next_finger_key = fix_finger_keys.pollLast();
                peer.lookup(next_finger_key).thenAccept(result -> {
                    state.getFingerTable().replaceClosestSuccessorWith(next_finger_key, result);
                });
                fix_finger_keys.addFirst(next_finger_key);
            }

            private void refreshSuccessorList() {

                peer.getAsynchronousRemote(state.getSuccessor()).pull(successor_list_selector).thenAccept(successors_successor_list -> {
                    successor_list.refresh(successors_successor_list);
                });
            }

            private void stabilizeRing() {

                final PeerReference successor = state.getSuccessor();
                final AsynchronousPeerRemote successor_remote = peer.getAsynchronousRemote(successor);
                final PeerReference self_reference = peer.getSelfReference();
                final Key self_key = self_reference.getKey();
                successor_remote.pull(PREDECESSOR_SELECTOR).thenAccept(predecessor_of_successor -> {
                    if (predecessor_of_successor != null && !predecessor_of_successor.isEmpty()) {

                        final PeerReference potential_successor = predecessor_of_successor.get(0);
                        final Key successor_key = successor.getKey();
                        final Key potential_successor_key = potential_successor.getKey();

                        // Check whether the potential successor's key lies in this node's current successor's key range, and the potential successor is not the current successor.
                        if (!potential_successor_key.equals(successor_key) &&
                                !self_key.equals(successor_key) &&
                                !self_key.equals(potential_successor_key) &&
                                self_key.compareRingDistance(potential_successor_key, successor_key) < 0) {
                            state.setSuccessor(potential_successor);
                        }
                    }
                }).thenRun(() -> {
                    successor_remote.push(self_reference);
                });

            }

            private void pingPredecessor() {

                final CompletableFuture<Key> get_predecessor_key = peer.getAsynchronousRemote(state.getPredecessor()).getKey();
                get_predecessor_key.whenComplete((result, error) -> {
                    if (get_predecessor_key.isCompletedExceptionally()) {
                        state.setPredecessor(null);
                    }
                });
            }
        };
    }

    /**
     * Generates a fixed number of logarithmically distributed keys.
     *
     * @param start_key the start key
     * @param number_of_keys the number of keys
     * @param base the logarithmic base
     * @return a fixed number of clustered keys
     */
     static ArrayDeque<Key> generateKeysLogarithmically(final Key start_key, final int number_of_keys, final BigInteger base, int key_length_bits) {

        final BigInteger start_value = start_key.getValue();
        final ArrayDeque<Key> keys = new ArrayDeque<>(number_of_keys);
        BigInteger offset = BigInteger.valueOf(2).pow(key_length_bits);

        for (int i = number_of_keys - 1; i >= 0; i--) {
            offset = offset.divide(base);
            keys.addFirst(new Key(start_value.add(offset)));
        }
        return keys;
    }

}
