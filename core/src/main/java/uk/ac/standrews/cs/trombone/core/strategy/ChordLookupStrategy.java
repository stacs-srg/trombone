package uk.ac.standrews.cs.trombone.core.strategy;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import uk.ac.standrews.cs.trombone.core.AsynchronousPeerRemote;
import uk.ac.standrews.cs.trombone.core.NextHopReference;
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

    private final Peer local;
    private final Key local_key;
    private final ScheduledExecutorService executor;

    public ChordLookupStrategy(final Peer local) {

        this.local = local;
        local_key = local.key();
        executor = local.getExecutor();
    }

    @Override
    public CompletableFuture<PeerReference> apply(final Key target, final Optional<PeerMetric.LookupMeasurement> optional_measurement) {

        final CompletableFuture<PeerReference> future_lookup = new CompletableFuture<>();
        lookup(future_lookup, target, local.getSelfReference(), optional_measurement);
        return future_lookup;
    }

    void lookup(CompletableFuture<PeerReference> future_lookup, Key target, PeerReference current_hop, Optional<PeerMetric.LookupMeasurement> measurement) {

        final AsynchronousPeerRemote current_hop_remote = local.getAsynchronousRemote(current_hop);
        final CompletableFuture<NextHopReference> future_next_hop = current_hop_remote.nextHop(target);

        future_next_hop.whenCompleteAsync((next_hop, error) -> {

            // Next hop mustn't be this node, or further from us than the target.
            // assert !local_key.equals(next_hop.getKey());
            // assert local_key.compareRingDistance(next_hop.getKey(), target) > 0;

            if (future_next_hop.isCompletedExceptionally()) {
                future_lookup.completeExceptionally(error);
            }
            else {
                updateMeasurement(current_hop, measurement);

                if (next_hop.isFinalHop()) {
                    future_lookup.complete(next_hop);
                }
                else {
                    lookup(future_lookup, target, next_hop, measurement);
                }
            }
        }, executor);
    }

    private void updateMeasurement(final PeerReference current_hop, final Optional<PeerMetric.LookupMeasurement> measurement) {

        if (measurement.isPresent() && !isLocal(current_hop)) {
            final PeerMetric.LookupMeasurement lookupMeasurement = measurement.get();
            lookupMeasurement.incrementHopCount();
        }
    }

    private boolean isLocal(final PeerReference current) {

        return local_key.equals(current.getKey());
    }
}
