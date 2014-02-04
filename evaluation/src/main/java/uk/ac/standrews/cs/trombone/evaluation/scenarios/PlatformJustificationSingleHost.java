package uk.ac.standrews.cs.trombone.evaluation.scenarios;

import uk.ac.standrews.cs.trombone.event.Scenario;
import uk.ac.standrews.cs.trombone.event.provider.SequentialPortNumberProvider;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class PlatformJustificationSingleHost extends Scenario {

    public PlatformJustificationSingleHost() {

        super("PlatformJustificationSingleHost", Constants.SCENARIO_MASTER_SEED);
        setChurnProvider(Constants.NO_CHURN);
        setWorkloadProvider(Constants.WORKLOAD_1.clone());
        setExperimentDuration(Constants.EXPERIMENT_DURATION);
        setPeerKeyProvider(Constants.PEER_KEY_PROVIDER.clone());
        setPeerConfigurator(Constants.NO_MAINTENANCE);
        addHost("compute-0-0.local", 48, new SequentialPortNumberProvider(45000));
    }
}