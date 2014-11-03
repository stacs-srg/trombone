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
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.trombone.core.AsynchronousPeerRemote;
import uk.ac.standrews.cs.trombone.core.Key;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerReference;
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
    private static final CompletableFuture<Boolean> SKIP = CompletableFuture.completedFuture(Boolean.TRUE);
    public static final int MAX_SKIPPED_CYCLES = 20;
    private final ChordPeerState state;
    private final ChordSuccessorList successor_list;
    private final ChordSuccessorListSelector successor_list_selector;
    private final Key local_key;
    private final ChordFingerTable finger_table;
    private final ScheduledExecutorService executor;
    private CompletableFuture<Void> maintenance_cycle;

    ChordMaintenance(final Peer peer, long interval, TimeUnit interval_unit) {

        super(peer, interval, interval_unit);
        state = (ChordPeerState) peer.getPeerState();
        successor_list = state.getSuccessorList();
        successor_list_selector = new ChordSuccessorListSelector(successor_list.getMaxSize());
        executor = local.getExecutor();
        local_key = peer.key();
        finger_table = state.getFingerTable();
    }

    int skipped_cycles;
    boolean pingp, stab, fix, pred_of_suc, notif, succ_list;

    @Override
    public void run() {

        if (LOGGER.isDebugEnabled()) {
            final PeerReference predecessor = state.getPredecessor();
            final PeerReference successor = state.getSuccessor();
            LOGGER.debug("{} -> {} -> {}\n\n", predecessor != null ? predecessor.getKey() : "null", local.getSelfReference()
                    .getKey(), successor != null ? successor.getKey() : "null");
        }

        try {
            if (isMaintenanceCycleCompleted()) {
                pingp = stab = fix = pred_of_suc = notif = succ_list = false;
                maintenance_cycle = pingPredecessor().thenCompose(v -> {
                    pingp = true;
                    return stabilizeRing();
                })
                        .thenCompose(v -> {
                            stab = true;
                            return fixNextFinger();
                        })
                        .thenAccept(v -> fix = true);

                LOGGER.debug("maintenance cycle completed after {} skipped cycles", skipped_cycles);
                skipped_cycles = 0;
            }
            else {
                ++skipped_cycles;
                if (skipped_cycles >= MAX_SKIPPED_CYCLES) {
                    maintenance_cycle.cancel(true);
                }
                LOGGER.error("still running {}, Ping predecessor {}, stabilize {} (pred {}, notif {}, refresh {}), fix {} ", skipped_cycles, pingp, stab, pred_of_suc, notif, succ_list, fix);
            }
        }
        catch (Throwable error) {
            LOGGER.warn("failed to perform Chord maintenance cycle", error);
        }
    }

    private boolean isMaintenanceCycleCompleted() {

        return maintenance_cycle == null || maintenance_cycle.isDone();
    }

    private CompletableFuture<Boolean> fixNextFinger() {

        return finger_table.fixNextFinger();
    }

    private CompletableFuture<Void> refreshSuccessorList(Void v) {

        notif = true;
        return local.getAsynchronousRemote(state.getSuccessor(), false)
                .pull(successor_list_selector)
                .thenAccept(successors_successor_list -> {
                    successor_list.refresh(successors_successor_list);
                    succ_list = true;
                });
    }

    private CompletableFuture<Void> stabilizeRing() {

        return getPredecessorOfSuccessor().thenApply(this :: checkPotentialSuccessor)
                .thenCompose(this :: notifySuccessor)
                .thenCompose(this :: refreshSuccessorList)
                .handle(this :: handleStabilizeRing)
                .thenCompose(failed -> {
                    if (failed) {
                        return handleSuccessorFailure();
                    }
                    else {
                        return CompletableFuture.completedFuture(null);
                    }
                });
    }

    private boolean handleStabilizeRing(final Void success, final Throwable error) {

        final boolean stabilization_failed = error != null;
        if (stabilization_failed) {
            LOGGER.debug("failed to stabilise ring ", error);
        }
        else {
            LOGGER.debug("successfully stabilised ring");
        }
        return stabilization_failed;
    }

    private CompletableFuture<Void> notifySuccessor(boolean successor_changed) {

        pred_of_suc = true;
        LOGGER.debug("successor changed? {}", successor_changed);

        final PeerReference successor = state.getSuccessor();
        final PeerReference self_reference = local.getSelfReference();
        final AsynchronousPeerRemote successor_remote = local.getAsynchronousRemote(successor, false);
        return successor_remote.push(self_reference);
    }

    private boolean checkPotentialSuccessor(PeerReference potential_successor) {

        pred_of_suc = true;
        if (potential_successor != null) {
            final PeerReference successor = state.getSuccessor();
            final Key successor_key = successor.getKey();
            final Key potential_successor_key = potential_successor.getKey();

            // Check whether the potential successor's key lies in this node's current successor's key range, and the potential successor is not the current successor.
            if (!potential_successor_key.equals(successor_key) && Key.inSegment(local_key, potential_successor_key, successor_key)) {
                return state.setSuccessor(potential_successor);
            }
            return false;
        }
        return false;
    }

    private CompletableFuture<PeerReference> getPredecessorOfSuccessor() {

        final PeerReference successor = state.getSuccessor();
        return local.getAsynchronousRemote(successor, false)
                .pull(PREDECESSOR_SELECTOR)
                .thenApply(selection -> {
                    return selection != null && !selection.isEmpty() ? selection.get(0) : null;
                });
    }

    private CompletableFuture<Void> handleSuccessorFailure() {

        final Iterator<PeerReference> successors_iterator = successor_list.stream()
                .iterator();
        return findReachable(successors_iterator).thenCompose(next_reachable_successor -> {

            if (next_reachable_successor.isPresent()) {
                state.setSuccessor(next_reachable_successor.get());
                return CompletableFuture.completedFuture(null);
            }
            else {
                LOGGER.debug("no reachable successor is found in successor list");

                final PeerReference predecessor = state.getPredecessor();
                return local.join(predecessor);
            }
        });

    }

    private CompletableFuture<Optional<PeerReference>> findReachable(Iterator<PeerReference> references) {

        if (references.hasNext()) {

            final PeerReference next = references.next();

            return isReachable(next).thenCompose(reachable -> {
                if (reachable) {
                    return CompletableFuture.completedFuture(Optional.of(next));
                }
                else {
                    return findReachable(references);
                }
            });
        }
        else {
            return CompletableFuture.completedFuture(Optional.empty());
        }
    }

    private CompletableFuture<Boolean> isReachable(final PeerReference reference) {

        return ping(reference).handle((result, error) -> {

            final boolean ping_failed = error != null;

            if (ping_failed) {

                LOGGER.debug("failed to ping reference {} due to {}", reference, error);
            }

            return !ping_failed;
        });
    }

    private CompletableFuture<Boolean> pingPredecessor() {

        final PeerReference predecessor = state.getPredecessor();
        return predecessor != null ? ping(predecessor).handle(this :: handlePredecessorPing) : SKIP;
    }

    private boolean handlePredecessorPing(Void success, Throwable error) {

        final boolean ping_failed = error != null;

        if (ping_failed) {
            LOGGER.debug("predecessor ping failure", error);
            handlePredecessorPingFailure();
        }
        else {
            LOGGER.debug("successfully pinged predecessor");
        }
        return ping_failed;
    }

    private void handlePredecessorPingFailure() {

        state.setPredecessor(null);
    }

    private CompletableFuture<Void> ping(final PeerReference predecessor) {

        final AsynchronousPeerRemote predecessor_remote = local.getAsynchronousRemote(predecessor, false);
        return predecessor_remote.push(Collections.emptyList());
    }
}
