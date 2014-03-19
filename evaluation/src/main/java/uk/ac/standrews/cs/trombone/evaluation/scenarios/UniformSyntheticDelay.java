package uk.ac.standrews.cs.trombone.evaluation.scenarios;

import java.net.InetAddress;
import org.uncommons.maths.random.MersenneTwisterRNG;
import uk.ac.standrews.cs.trombone.core.SyntheticDelay;
import uk.ac.standrews.cs.trombone.core.util.NamingUtils;

import static uk.ac.standrews.cs.trombone.core.util.NetworkUtils.isLocalAddress;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class UniformSyntheticDelay implements SyntheticDelay {

    private static final long serialVersionUID = -6653326115826807344L;
    private final int min_delay_nanos;
    private final int max_delay_nanos;
    private final byte[] seed;
    private final MersenneTwisterRNG random;
    private final int range;

    public UniformSyntheticDelay(int min_delay_nanos, int max_delay_nanos, byte[] seed) {

        this.min_delay_nanos = min_delay_nanos;
        this.max_delay_nanos = max_delay_nanos;
        range = max_delay_nanos - min_delay_nanos + 1;

        this.seed = seed;
        random = new MersenneTwisterRNG(seed);
    }

    @Override
    public long get(final InetAddress from, final InetAddress to) {

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

    public byte[] getSeed() {

        return seed;
    }

    @Override
    public String getName() {

        return NamingUtils.name(this);
    }
}
