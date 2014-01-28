package uk.ac.standrews.cs.trombone.evaluation.scenarios;

import java.util.concurrent.TimeUnit;
import uk.ac.standrews.cs.shabdiz.util.Duration;
import uk.ac.standrews.cs.trombone.core.key.RandomKeyProvider;
import uk.ac.standrews.cs.trombone.event.Scenario;
import uk.ac.standrews.cs.trombone.event.provider.SequentialPortNumberProvider;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class BaseScenario extends Scenario {

    private static final Duration EXPERIMENT_DURATION = new Duration(4, TimeUnit.HOURS);

    protected BaseScenario(String name) {

        super(name, 89562);
        setPeerKeyProvider(new RandomKeyProvider(generateSeed(), 32));
        setExperimentDuration(EXPERIMENT_DURATION);
        addHost("localhost", 1000, new SequentialPortNumberProvider(45000));
    }
}
