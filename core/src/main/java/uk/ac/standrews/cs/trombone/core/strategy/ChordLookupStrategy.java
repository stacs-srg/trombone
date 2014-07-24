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

package uk.ac.standrews.cs.trombone.core.strategy;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.trombone.core.AsynchronousPeerRemote;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerMetric;
import uk.ac.standrews.cs.trombone.core.PeerReference;
import uk.ac.standrews.cs.trombone.core.key.Key;

/**
 * Implements Chord's lookup protocol.
 *
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class ChordLookupStrategy implements LookupStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChordLookupStrategy.class);
    private final Peer local;
    private final boolean learn_from_hops;
    private final Key local_key;
    private final PeerReference self_reference;

    public ChordLookupStrategy(final Peer local, final boolean learn_from_hops) {

        this.local = local;
        this.learn_from_hops = learn_from_hops;
        local_key = local.getKeySync();
        self_reference = local.getSelfReference();
    }

    @Override
    public CompletableFuture<PeerReference> apply(final Key target, final Optional<PeerMetric.LookupMeasurement> optional_measurement) {

        final CompletableFuture<PeerReference> future_lookup = new CompletableFuture<>();
        lookup(future_lookup, target, self_reference, optional_measurement);
        return future_lookup;
    }

    void lookup(CompletableFuture<PeerReference> future_lookup, Key target, PeerReference current_hop, Optional<PeerMetric.LookupMeasurement> measurement) {

        final AsynchronousPeerRemote current_hop_remote = local.getAsynchronousRemote(current_hop);
        final CompletableFuture<PeerReference> future_next_hop = current_hop_remote.nextHop(target);

        future_next_hop.whenCompleteAsync((next_hop, error) -> {

            // Next hop mustn't be this node, or further from us than the target.
            // assert !local_key.equals(next_hop.getKey());
            // assert local_key.compareRingDistance(next_hop.getKey(), target) > 0;

            if (future_next_hop.isCompletedExceptionally()) {
                future_lookup.completeExceptionally(error);
            }
            else {

                if (isFinalHop(target, current_hop, next_hop)) {
                    future_lookup.complete(next_hop);
                }
                else {

                    lookup(future_lookup, target, next_hop, measurement);
                }

                updateMeasurement(current_hop, measurement);

                if (learn_from_hops) {
                    local.push(next_hop);
                }
            }

        }, local.getExecutor());
    }

    private void updateMeasurement(final PeerReference current_hop, final Optional<PeerMetric.LookupMeasurement> measurement) {

        if (measurement.isPresent() && !isLocal(current_hop)) {
            final PeerMetric.LookupMeasurement lookupMeasurement = measurement.get();
            lookupMeasurement.incrementHopCount();
        }
    }

    private boolean isLocal(final PeerReference current) {

        return self_reference.equals(current);
    }

    private boolean isFinalHop(final Key target, final PeerReference current, final PeerReference next_hop) {

        return current.equals(next_hop);
    }

}
