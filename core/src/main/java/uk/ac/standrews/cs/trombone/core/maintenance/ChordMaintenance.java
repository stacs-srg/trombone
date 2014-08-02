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

package uk.ac.standrews.cs.trombone.core.maintenance;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.trombone.core.AsynchronousPeerRemote;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerReference;
import uk.ac.standrews.cs.trombone.core.key.Key;
import uk.ac.standrews.cs.trombone.core.key.RingArithmetic;
import uk.ac.standrews.cs.trombone.core.selector.ChordPredecessorSelector;
import uk.ac.standrews.cs.trombone.core.selector.ChordSuccessorListSelector;
import uk.ac.standrews.cs.trombone.core.state.ChordFingerTable;
import uk.ac.standrews.cs.trombone.core.state.ChordPeerState;
import uk.ac.standrews.cs.trombone.core.state.ChordSuccessorList;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class ChordMaintenance extends Maintenance {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChordMaintenance.class);
    private static final ChordPredecessorSelector PREDECESSOR_SELECTOR = ChordPredecessorSelector.getInstance();
    private static final Void VOID = null;
    private static final CompletableFuture<Void> DONE = CompletableFuture.completedFuture(VOID);
    private final ChordPeerState state;
    private final ChordSuccessorList successor_list;
    private final ChordSuccessorListSelector successor_list_selector;
    private final Key local_key;
    private final ChordFingerTable finger_table;
    private final ScheduledExecutorService executor;
    private CompletableFuture<Void> future;

    public ChordMaintenance(final Peer peer, long interval, TimeUnit interval_unit) {

        super(peer, interval, interval_unit);
        state = (ChordPeerState) peer.getPeerState();
        successor_list = state.getSuccessorList();
        successor_list_selector = new ChordSuccessorListSelector(successor_list.getMaxSize());
        executor = local.getExecutor();
        local_key = peer.key();
        finger_table = state.getFingerTable();
    }

    int skipped_cycles = 0;

    boolean pingp;
    boolean stabilize;
    boolean fix;

    @Override
    public void run() {

        if (LOGGER.isDebugEnabled()) {
            final PeerReference predecessor = state.getPredecessor();
            final PeerReference successor = state.getSuccessor();
            LOGGER.debug("{} -> {} -> {}", predecessor != null ? predecessor.getKey() : "null", local_key, successor != null ? successor.getKey() : "null");
        }

        try {
            if (future == null || future.isDone()) {
                pingp = false;
                stabilize = false;
                fix = false;

                future = pingPredecessor().thenCompose(v -> {

                    pingp = true;
                    return stabilizeRing();
                })
                        .thenCompose(v -> {
                            stabilize = true;
                            return fixNextFinger().thenRun(() -> fix = true);
                        });

                LOGGER.debug("maintenance cycle finished after {} skipped cycles", skipped_cycles);
                skipped_cycles = 0;
            }
            else {
                LOGGER.info("still running {}, Pingp {}, stabilize {}, fix {} ", ++skipped_cycles, pingp, stabilize, fix);
            }
        }
        catch (Throwable error) {
            LOGGER.debug("failed to perform Chord maintenance cycle", error);
        }
    }

    private CompletableFuture<Boolean> fixNextFinger() {

        return finger_table.fixNextFinger();
    }

    private CompletableFuture<Void> refreshSuccessorList(Void v) {

        return local.getAsynchronousRemote(state.getSuccessor())
                .pull(successor_list_selector)
                .thenAcceptAsync(successors_successor_list -> {
                    successor_list.refresh(successors_successor_list);
                }, executor);
    }

    private CompletableFuture<Void> stabilizeRing() {

        return getPredecessorOfSuccessor().thenApply(this :: checkPotentialSuccessor)
                .thenComposeAsync(this :: notifySuccessor, executor)
                .thenComposeAsync(this :: refreshSuccessorList, executor)
                .whenCompleteAsync((result, error) -> {

                    if (error != null) {
                        LOGGER.debug("failed to stabilise ring ", error);
                        handleSuccessorFailure();
                    }
                    else {
                        LOGGER.debug("successfully stabilised ring");
                    }
                }, executor);
    }

    private CompletableFuture<Void> notifySuccessor(boolean successor_changed) {

        LOGGER.debug("successor changed? {}", successor_changed);

        final PeerReference successor = state.getSuccessor();
        final PeerReference self_reference = local.getSelfReference();
        final AsynchronousPeerRemote successor_remote = local.getAsynchronousRemote(successor);
        return successor_remote.push(self_reference);
    }

    private boolean checkPotentialSuccessor(PeerReference potential_successor) {

        if (potential_successor != null) {
            final PeerReference successor = state.getSuccessor();
            final Key successor_key = successor.getKey();
            final Key potential_successor_key = potential_successor.getKey();

            // Check whether the potential successor's key lies in this node's current successor's key range, and the potential successor is not the current successor.
            if (!potential_successor_key.equals(successor_key) && RingArithmetic.inSegment(local_key, potential_successor_key, successor_key)) {
                return state.setSuccessor(potential_successor);
            }
            return false;
        }
        return false;
    }

    private CompletableFuture<PeerReference> getPredecessorOfSuccessor() {

        final PeerReference successor = state.getSuccessor();
        return local.getAsynchronousRemote(successor)
                .pull(PREDECESSOR_SELECTOR)
                .thenApplyAsync(selection -> selection != null && !selection.isEmpty() ? selection.get(0) : null, executor);
    }

    private void handleSuccessorFailure() {

        final Optional<PeerReference> next_reachable_successor = successor_list.stream()
                .filter(entry -> isReachable(entry))
                .findFirst();
        if (next_reachable_successor.isPresent()) {
            state.setSuccessor(next_reachable_successor.get());
        }
        else {
            LOGGER.debug("no reachable successor is found in successor list");
            local.join(state.getPredecessor())
                    .whenComplete((result, error) -> {
                        if (error != null) {
                            LOGGER.debug("failed to join predecessor", error);
                            joinUsingFingerTable();
                        }
                        else {
                            LOGGER.debug("successfully joined predecessor");
                        }
                    });
        }
    }

    private void joinUsingFingerTable() {

        for (final PeerReference finger : finger_table.getFingers()) {

            assert finger != null;

            if (!finger.getKey()
                    .equals(local_key)) {
                try {
                    local.join(finger)
                            .get();
                    break;
                }
                catch (InterruptedException | ExecutionException e) {
                    // ignore;
                }
            }
        }
    }

    private boolean isReachable(final PeerReference reference) {

        try {
            ping(reference).get();
            return true;
        }
        catch (InterruptedException | ExecutionException error) {
            LOGGER.debug("failed to ping reference {} due to {}", reference, error);
            return false;
        }
    }

    private CompletableFuture<Void> pingPredecessor() {

        final PeerReference predecessor = state.getPredecessor();
        return predecessor != null ? ping(predecessor).whenComplete((success, error) -> {
            if (error != null) {
                LOGGER.debug("predecessor ping failure", error);
                handlePredecessorFailure();
            }
            else {
                LOGGER.debug("successfully pinged predecessor");
            }
        }) : DONE;
    }

    private void handlePredecessorFailure() {

        state.setPredecessor(null);
    }

    private CompletableFuture<Void> ping(final PeerReference predecessor) {

        return local.getAsynchronousRemote(predecessor)
                .push(Collections.emptyList());
    }
}
