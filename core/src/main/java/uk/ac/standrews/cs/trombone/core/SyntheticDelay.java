package uk.ac.standrews.cs.trombone.core;

import java.net.InetAddress;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Generates a synthetic delay in {@link TimeUnit#NANOSECONDS nanoseconds} for a given pair of {@link InetAddress addresses}.
 *
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
@FunctionalInterface
public interface SyntheticDelay {

    /** Returns zero for any given pair of {@link InetAddress addresses}. */
    SyntheticDelay ZERO = (from, to, random) -> 0L;

    /**
     * Gets the synthetic delay in {@link TimeUnit#NANOSECONDS}.
     *
     * @param from the source address
     * @param to the destination address
     * @return the synthetic delay in {@link TimeUnit#NANOSECONDS}
     */
    long get(InetAddress from, InetAddress to, Random random);

}
