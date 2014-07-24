package uk.ac.standrews.cs.trombone.core.strategy;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import uk.ac.standrews.cs.trombone.core.PeerMetric;
import uk.ac.standrews.cs.trombone.core.PeerReference;
import uk.ac.standrews.cs.trombone.core.key.Key;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public interface LookupStrategy extends BiFunction<Key, Optional<PeerMetric.LookupMeasurement>, CompletableFuture<PeerReference>> {}
