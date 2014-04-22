package uk.ac.standrews.cs.trombone.evaluation.scenarios;

import uk.ac.standrews.cs.trombone.event.Scenario;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class PlatformJustification extends Scenario {

    public PlatformJustification(String name) {

        super(name, Constants.SCENARIO_MASTER_SEED);
        setExperimentDuration(Constants.EXPERIMENT_DURATION_4);
        setObservationInterval(Constants.OBSERVATION_INTERVAL);
        setLookupRetryCount(Constants.LOOKUP_RETRY_COUNT);
        setPeerKeyProvider(Constants.PEER_KEY_PROVIDER.copy());
    }
}
