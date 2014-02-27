package uk.ac.standrews.cs.trombone.evaluation.scenarios;

import java.util.concurrent.TimeUnit;
import uk.ac.standrews.cs.shabdiz.util.Duration;
import uk.ac.standrews.cs.trombone.core.PeerConfiguration;
import uk.ac.standrews.cs.trombone.event.provider.SequentialPortNumberProvider;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class LocalTestScenario extends PlatformJustification {

    public LocalTestScenario(int network_size) {

        super(LocalTestScenario.class.getSimpleName() + network_size);
        setExperimentDuration(new Duration(10, TimeUnit.MINUTES));
        addHost("localhost", network_size, new SequentialPortNumberProvider(45000), Constants.NO_CHURN.copy(), Constants.WORKLOAD_1.copy(), new PeerConfiguration(Constants.SUCCESSOR_MAINTENANCE, Constants.BLUB_UNIFORMLY_DISTRIBUTED_SYNTHETIC_DELAY));
//        addHost("localhost", network_size, new SequentialPortNumberProvider(45000), Constants.CHURN_1.copy(), Constants.WORKLOAD_1.copy(), new PeerConfiguration(new EvolutionaryMaintenance(5, 1, new Probability(0.1), 30, TimeUnit.SECONDS), Constants.BLUB_UNIFORMLY_DISTRIBUTED_SYNTHETIC_DELAY));
        //        addHost("localhost", network_size, new SequentialPortNumberProvider(45000), Constants.CHURN_1.copy(), Constants.WORKLOAD_1.copy(), Constants.NO_MAINTENANCE_CONFIGURATION);
        //        addHost("localhost", network_size, new SequentialPortNumberProvider(45000), Constants.CHURN_1.copy(), Constants.WORKLOAD_1.copy(), new PeerConfiguration(new Maintenance(new SuccessorListMaintenance(5)), Constants.BLUB_UNIFORMLY_DISTRIBUTED_SYNTHETIC_DELAY));
    }
}
