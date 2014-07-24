package uk.ac.standrews.cs.trombone.core;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;
import uk.ac.standrews.cs.trombone.core.util.Named;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public interface SyntheticDelay extends Serializable, Named {

    SyntheticDelay ZERO = new SyntheticDelay() {

        private static final long serialVersionUID = -6878108170239697600L;

        @Override
        public long get(final InetAddress from, final InetAddress to) {

            return 0;
        }

        @Override
        public String getName() {

            return "none";
        }
    };

    /**
     * Gets the synthetic delay in {@link TimeUnit#NANOSECONDS}.
     *
     * @param from the source address
     * @param to the destination address
     * @return the synthetic delay in {@link TimeUnit#NANOSECONDS}
     */
    long get(InetAddress from, InetAddress to);
}
