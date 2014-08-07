package uk.ac.standrews.cs.trombone.core.strategy;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import uk.ac.standrews.cs.trombone.core.Key;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerMetric;
import uk.ac.standrews.cs.trombone.core.PeerReference;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public interface LookupStrategy {

    CompletableFuture<PeerReference> lookup(Peer local, Key target, Optional<PeerMetric.LookupMeasurement> measurement);
}
