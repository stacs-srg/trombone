package uk.ac.standrews.cs.trombone.core;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;
import uk.ac.standrews.cs.trombone.core.util.Named;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public interface SyntheticDelay extends Serializable, Named {

    /**
     * Gets the synthetic delay in {@link TimeUnit#NANOSECONDS}.
     *
     * @param from the source address
     * @param to the destination address
     * @return the synthetic delay in {@link TimeUnit#NANOSECONDS}
     */
    long get(InetAddress from, InetAddress to);
}
