package uk.ac.standrews.cs.trombone.evaluation.scenarios;

import java.net.InetAddress;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.mashti.sina.distribution.ProbabilityDistribution;
import org.mashti.sina.util.RandomNumberGenerator;
import uk.ac.standrews.cs.trombone.core.SyntheticDelay;

import static uk.ac.standrews.cs.trombone.core.util.NetworkUtils.isLocalAddress;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class DistributedSyntheticDelay implements SyntheticDelay {

    private final ProbabilityDistribution distribution;

    public DistributedSyntheticDelay(ProbabilityDistribution distribution) {

        this.distribution = distribution;
    }

    @Override
    public void apply(final InetAddress from, final InetAddress to) throws InterruptedException {

        if (from.equals(to) || isLocalAddress(from) && isLocalAddress(to)) {
            final Number delay = RandomNumberGenerator.generate(distribution, ThreadLocalRandom.current());
            TimeUnit.NANOSECONDS.sleep(delay.longValue());
        }
    }
}
