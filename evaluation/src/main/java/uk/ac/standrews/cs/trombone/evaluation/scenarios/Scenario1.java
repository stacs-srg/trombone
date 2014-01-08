package uk.ac.standrews.cs.trombone.evaluation.scenarios;

import java.util.concurrent.TimeUnit;
import org.mashti.sina.distribution.ExponentialDistribution;
import org.mashti.sina.distribution.ProbabilityDistribution;
import uk.ac.standrews.cs.shabdiz.util.Duration;
import uk.ac.standrews.cs.trombone.core.key.RandomKeyProvider;
import uk.ac.standrews.cs.trombone.core.key.ZipfKeyProvider;
import uk.ac.standrews.cs.trombone.evaluation.Scenario;
import uk.ac.standrews.cs.trombone.evaluation.provider.ConstantRateWorkloadProvider;
import uk.ac.standrews.cs.trombone.evaluation.provider.NoChurnProvider;
import uk.ac.standrews.cs.trombone.evaluation.provider.PortNumberProvider;
import uk.ac.standrews.cs.trombone.evaluation.provider.RandomSeedProvider;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class Scenario1 extends Scenario {

    public static final NoChurnProvider NO_CHURN_PROVIDER = new NoChurnProvider();
    public static final Duration EXPERIMENT_DURATION = new Duration(5, TimeUnit.MINUTES);
    private static final ProbabilityDistribution workload_intervals_distribution = new ExponentialDistribution(Double.valueOf(new Duration(500, TimeUnit.MILLISECONDS).getLength(TimeUnit.NANOSECONDS)));

    protected Scenario1() {

        super("s", 89562);
        setChurnProvider(NO_CHURN_PROVIDER);
        setWorkloadProvider(new ConstantRateWorkloadProvider(workload_intervals_distribution, new ZipfKeyProvider(1000, 1, generateSeed()), 5, new RandomSeedProvider(generateSeed())));
        setExperimentDuration(EXPERIMENT_DURATION);
        setPeerKeyProvider(new RandomKeyProvider(generateSeed()));
        addHost("localhost", 48, new PortNumberProvider(45000));
    }


}
