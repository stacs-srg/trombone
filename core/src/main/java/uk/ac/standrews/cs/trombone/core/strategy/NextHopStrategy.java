package uk.ac.standrews.cs.trombone.core.strategy;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import uk.ac.standrews.cs.trombone.core.NextHopReference;
import uk.ac.standrews.cs.trombone.core.key.Key;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public interface NextHopStrategy extends Function<Key, CompletableFuture<NextHopReference>> {}
