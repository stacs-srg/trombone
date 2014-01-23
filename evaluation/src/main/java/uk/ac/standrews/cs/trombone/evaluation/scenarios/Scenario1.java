package uk.ac.standrews.cs.trombone.evaluation.scenarios;

import java.util.concurrent.TimeUnit;
import org.mashti.sina.distribution.ExponentialDistribution;
import org.mashti.sina.distribution.ProbabilityDistribution;
import uk.ac.standrews.cs.shabdiz.util.Duration;
import uk.ac.standrews.cs.trombone.core.key.RandomKeyProvider;
import uk.ac.standrews.cs.trombone.core.key.ZipfKeyProvider;
import uk.ac.standrews.cs.trombone.event.Scenario;
import uk.ac.standrews.cs.trombone.event.provider.ConstantRateWorkloadProvider;
import uk.ac.standrews.cs.trombone.event.provider.NoChurnProvider;
import uk.ac.standrews.cs.trombone.event.provider.PortNumberProvider;
import uk.ac.standrews.cs.trombone.event.provider.RandomSeedProvider;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class Scenario1 extends Scenario {

    private static final Duration EXPERIMENT_DURATION = new Duration(3, TimeUnit.MINUTES);
    private static final ProbabilityDistribution workload_intervals_distribution = new ExponentialDistribution(Double.valueOf(new Duration(500, TimeUnit.MILLISECONDS).getLength(TimeUnit.NANOSECONDS)));

    public Scenario1() {

        super("scenario_1", 89562);
        setChurnProvider(NoChurnProvider.getInstance());
        setWorkloadProvider(new ConstantRateWorkloadProvider(workload_intervals_distribution, new ZipfKeyProvider(20000, 1, generateSeed()), new RandomSeedProvider(generateSeed())));
        setExperimentDuration(EXPERIMENT_DURATION);
        setPeerKeyProvider(new RandomKeyProvider(generateSeed()));
        addHost("localhost", 48, new PortNumberProvider(45000));
    }
}
