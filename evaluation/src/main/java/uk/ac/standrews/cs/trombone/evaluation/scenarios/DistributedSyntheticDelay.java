package uk.ac.standrews.cs.trombone.evaluation.scenarios;

import java.net.InetAddress;
import org.mashti.sina.distribution.ProbabilityDistribution;
import org.mashti.sina.util.RandomNumberGenerator;
import org.uncommons.maths.random.MersenneTwisterRNG;
import uk.ac.standrews.cs.trombone.core.SyntheticDelay;
import uk.ac.standrews.cs.trombone.core.util.NamingUtils;

import static uk.ac.standrews.cs.trombone.core.util.NetworkUtils.isLocalAddress;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class DistributedSyntheticDelay implements SyntheticDelay {

    private static final long serialVersionUID = -6653326115826807344L;
    private final ProbabilityDistribution distribution;
    private final byte[] seed;
    private final MersenneTwisterRNG random;

    public DistributedSyntheticDelay(ProbabilityDistribution distribution, byte[] seed) {

        this.distribution = distribution;
        this.seed = seed;
        random = new MersenneTwisterRNG(seed);
    }

    @Override
    public long get(final InetAddress from, final InetAddress to) {

        if (from.equals(to) || isLocalAddress(from) && isLocalAddress(to)) {
            return RandomNumberGenerator.generate(distribution, random).longValue();
        }
        return 0;
    }

    public ProbabilityDistribution getDistribution() {

        return distribution;
    }

    public byte[] getSeed() {

        return seed;
    }

    @Override
    public String getName() {

        return NamingUtils.name(this);
    }
}
