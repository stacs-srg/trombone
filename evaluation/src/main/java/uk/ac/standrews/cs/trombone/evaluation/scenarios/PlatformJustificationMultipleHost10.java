package uk.ac.standrews.cs.trombone.evaluation.scenarios;

import uk.ac.standrews.cs.trombone.event.Scenario;
import uk.ac.standrews.cs.trombone.event.provider.SequentialPortNumberProvider;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class PlatformJustificationMultipleHost10 extends Scenario {

   

    public PlatformJustificationMultipleHost10() {

        super("PlatformJustificationMultipleHost48", Constants.SCENARIO_MASTER_SEED);
        setChurnProvider(Constants.NO_CHURN);
        setWorkloadProvider(Constants.WORKLOAD_1.clone());
        setExperimentDuration(Constants.EXPERIMENT_DURATION);
        setObservationInterval(Constants.OBSERVATION_INTERVAL);
        setLookupRetryCount(Constants.LOOKUP_RETRY_COUNT);
        setPeerKeyProvider(Constants.PEER_KEY_PROVIDER.clone());
        setPeerConfiguration(Constants.NO_MAINTENANCE);

        for (int i = 0; i < 10; i++) {
            addHost("compute-0-" + i + ".local", 1, new SequentialPortNumberProvider(45000));
        }
    }
}
