package uk.ac.standrews.cs.trombone.evaluation.scenarios;

import java.util.concurrent.TimeUnit;
import org.mashti.sina.distribution.ExponentialDistribution;
import org.mashti.sina.distribution.ProbabilityDistribution;
import uk.ac.standrews.cs.shabdiz.util.Duration;
import uk.ac.standrews.cs.trombone.core.key.RandomKeyProvider;
import uk.ac.standrews.cs.trombone.evaluation.Scenario;
import uk.ac.standrews.cs.trombone.evaluation.provider.NoChurnProvider;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class BaseScenario extends Scenario {

    public static final NoChurnProvider NO_CHURN_PROVIDER = new NoChurnProvider();
    public static final Duration EXPERIMENT_DURATION = new Duration(5, TimeUnit.MINUTES);
    private static final ProbabilityDistribution workload_intervals_distribution = new ExponentialDistribution(Double.valueOf(new Duration(500, TimeUnit.MILLISECONDS).getLength(TimeUnit.NANOSECONDS)));

    protected BaseScenario(String name) {

        super(name, 89562);
        setPeerKeyProvider(new RandomKeyProvider(generateSeed()));
    }
}
