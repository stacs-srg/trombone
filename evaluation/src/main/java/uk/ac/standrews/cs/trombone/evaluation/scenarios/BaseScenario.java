package uk.ac.standrews.cs.trombone.evaluation.scenarios;

import uk.ac.standrews.cs.trombone.event.Scenario;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class BaseScenario extends Scenario {

    protected BaseScenario(String name) {

        super(name, Constants.SCENARIO_MASTER_SEED);
        setPeerKeyProvider(Constants.PEER_KEY_PROVIDER.clone());
        setExperimentDuration(Constants.EXPERIMENT_DURATION);
        setObservationInterval(Constants.OBSERVATION_INTERVAL);
        addHost("localhost", 1000, Constants.PORT_NUMBER_PROVIDER);
    }
}
