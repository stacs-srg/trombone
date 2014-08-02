package uk.ac.standrews.cs.trombone.evaluation.util;

import java.net.InetAddress;
import java.util.Random;
import uk.ac.standrews.cs.trombone.core.SyntheticDelay;

import static uk.ac.standrews.cs.trombone.core.util.NetworkUtils.isLocalAddress;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class UniformSyntheticDelay implements SyntheticDelay {

    private final int min_delay_nanos;
    private final int max_delay_nanos;
    private final int range;

    public UniformSyntheticDelay(int min_delay_nanos, int max_delay_nanos) {

        this.min_delay_nanos = min_delay_nanos;
        this.max_delay_nanos = max_delay_nanos;
        range = max_delay_nanos - min_delay_nanos + 1;
    }

    @Override
    public long get(final InetAddress from, final InetAddress to, final Random random) {

        if (from.equals(to) || isLocalAddress(from) && isLocalAddress(to)) {
            return random.nextInt(range) + min_delay_nanos;
        }
        return 0;
    }

    public int getMinDelayInNanos() {

        return min_delay_nanos;
    }

    public int getMaxDelayInNanos() {

        return max_delay_nanos;
    }
}
