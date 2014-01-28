package uk.ac.standrews.cs.trombone.evaluation.scenarios;

import java.util.concurrent.TimeUnit;
import org.mashti.sina.distribution.ExponentialDistribution;
import org.mashti.sina.distribution.ProbabilityDistribution;
import uk.ac.standrews.cs.shabdiz.util.Duration;
import uk.ac.standrews.cs.trombone.core.key.ZipfKeyProvider;
import uk.ac.standrews.cs.trombone.event.provider.ConstantRateWorkloadProvider;
import uk.ac.standrews.cs.trombone.event.provider.NoChurnProvider;
import uk.ac.standrews.cs.trombone.event.provider.RandomSeedProvider;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class Scenario1 extends BaseScenario {

    private static final ProbabilityDistribution workload_intervals_distribution = new ExponentialDistribution(Double.valueOf(new Duration(2000, TimeUnit.MILLISECONDS).getLength(TimeUnit.NANOSECONDS)));

    public Scenario1() {

        super("scenario_1");
        setChurnProvider(NoChurnProvider.getInstance());
        setWorkloadProvider(new ConstantRateWorkloadProvider(workload_intervals_distribution, new ZipfKeyProvider(20000, 1, 32, generateSeed()), new RandomSeedProvider(generateSeed())));
    }
}
