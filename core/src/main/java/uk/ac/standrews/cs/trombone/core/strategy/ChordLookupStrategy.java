package uk.ac.standrews.cs.trombone.core.strategy;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import uk.ac.standrews.cs.trombone.core.AsynchronousPeerRemote;
import uk.ac.standrews.cs.trombone.core.Key;
import uk.ac.standrews.cs.trombone.core.NextHopReference;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerMetric;
import uk.ac.standrews.cs.trombone.core.PeerReference;

/**
 * Implements Chord's lookup protocol.
 *
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class ChordLookupStrategy implements LookupStrategy {

    @Override
    public CompletableFuture<PeerReference> lookup(final Peer local, final Key target, final Optional<PeerMetric.LookupMeasurement> optional_measurement) {

        final CompletableFuture<PeerReference> future_lookup = new CompletableFuture<>();
        lookup(local, future_lookup, target, local.getSelfReference(), optional_measurement);
        return future_lookup;
    }

    void lookup(final Peer local, CompletableFuture<PeerReference> future_lookup, Key target, PeerReference current_hop, Optional<PeerMetric.LookupMeasurement> measurement) {

        final AsynchronousPeerRemote current_hop_remote = local.getAsynchronousRemote(current_hop, measurement.isPresent());
        final CompletableFuture<NextHopReference> future_next_hop = current_hop_remote.nextHop(target);

        future_next_hop.whenComplete((next_hop, error) -> {

            // Next hop mustn't be this node, or further from us than the target.
            // assert !local_key.equals(next_hop.getKey());
            // assert local_key.compareRingDistance(next_hop.getKey(), target) > 0;

            if (future_next_hop.isCompletedExceptionally()) {
                future_lookup.completeExceptionally(error);
            }
            else {
                updateMeasurement(local, current_hop, measurement);

                if (next_hop.isFinalHop() || isHopCountExcessive(measurement)) {
                    future_lookup.complete(next_hop);
                }
                else {
                    lookup(local, future_lookup, target, next_hop, measurement);
                }
            }
        });
    }

    private boolean isHopCountExcessive(final Optional<PeerMetric.LookupMeasurement> measurement) {

        return measurement.isPresent() && measurement.get()
                .getHopCount() >= 100;
    }

    private void updateMeasurement(final Peer local, final PeerReference current_hop, final Optional<PeerMetric.LookupMeasurement> measurement) {

        if (measurement.isPresent() && !isLocal(local, current_hop)) {
            final PeerMetric.LookupMeasurement lookupMeasurement = measurement.get();
            lookupMeasurement.incrementHopCount();
        }
    }

    private static boolean isLocal(final Peer local, final PeerReference current) {

        return local.key()
                .equals(current.getKey());
    }
}
