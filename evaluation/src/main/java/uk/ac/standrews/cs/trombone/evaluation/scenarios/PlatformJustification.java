package uk.ac.standrews.cs.trombone.evaluation.scenarios;

import uk.ac.standrews.cs.trombone.event.Scenario;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class PlatformJustification extends Scenario {

    public PlatformJustification(String name) {

        super(name, Constants.SCENARIO_MASTER_SEED);
        setChurnProvider(Constants.NO_CHURN);
        setWorkloadProvider(Constants.WORKLOAD_1.clone());
        setExperimentDuration(Constants.EXPERIMENT_DURATION);
        setObservationInterval(Constants.OBSERVATION_INTERVAL);
        setLookupRetryCount(Constants.LOOKUP_RETRY_COUNT);
        setPeerKeyProvider(Constants.PEER_KEY_PROVIDER.clone());
        setPeerConfiguration(Constants.NO_MAINTENANCE);
    }
}
