package uk.ac.standrews.cs.trombone.evaluation.scenarios;

import java.util.concurrent.TimeUnit;
import org.mashti.sina.distribution.ExponentialDistribution;
import org.mashti.sina.distribution.ProbabilityDistribution;
import uk.ac.standrews.cs.shabdiz.util.Duration;
import uk.ac.standrews.cs.trombone.core.key.RandomKeyProvider;
import uk.ac.standrews.cs.trombone.core.key.ZipfKeyProvider;
import uk.ac.standrews.cs.trombone.event.Scenario;
import uk.ac.standrews.cs.trombone.event.provider.ConstantRateUncorrelatedUniformChurnProvider;
import uk.ac.standrews.cs.trombone.event.provider.ConstantRateWorkloadProvider;
import uk.ac.standrews.cs.trombone.event.provider.RandomSeedProvider;
import uk.ac.standrews.cs.trombone.event.provider.SequentialPortNumberProvider;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class PlatformJustificationSingleHost extends Scenario {

    private static final Duration EXPERIMENT_DURATION = new Duration(30, TimeUnit.MINUTES);
    private static final ProbabilityDistribution session_length_distribution = new ExponentialDistribution(Double.valueOf(new Duration(100, TimeUnit.SECONDS).getLength(TimeUnit.NANOSECONDS)));
    private static final ProbabilityDistribution downtime_distribution = new ExponentialDistribution(Double.valueOf(new Duration(100, TimeUnit.SECONDS).getLength(TimeUnit.NANOSECONDS)));
    private static final ProbabilityDistribution workload_intervals_distribution = new ExponentialDistribution(Double.valueOf(new Duration(2000, TimeUnit.MILLISECONDS).getLength(TimeUnit.NANOSECONDS)));

    public PlatformJustificationSingleHost() {

        super("PlatformJustificationSingleHost", 89562);
        setChurnProvider(new ConstantRateUncorrelatedUniformChurnProvider(session_length_distribution, downtime_distribution, new RandomSeedProvider(generateSeed())));
        setWorkloadProvider(new ConstantRateWorkloadProvider(workload_intervals_distribution, new ZipfKeyProvider(20000, 1, 32, generateSeed()), new RandomSeedProvider(generateSeed())));
        setExperimentDuration(EXPERIMENT_DURATION);
        setPeerKeyProvider(new RandomKeyProvider(generateSeed(), 32));
        addHost("compute-0-0.local", 48, new SequentialPortNumberProvider(45000));
    }
}
