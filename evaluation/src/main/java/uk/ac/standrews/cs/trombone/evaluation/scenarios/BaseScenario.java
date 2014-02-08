package uk.ac.standrews.cs.trombone.evaluation.scenarios;

import javax.inject.Provider;
import uk.ac.standrews.cs.shabdiz.util.Combinations;
import uk.ac.standrews.cs.trombone.core.PeerConfiguration;
import uk.ac.standrews.cs.trombone.event.Scenario;
import uk.ac.standrews.cs.trombone.event.churn.Churn;
import uk.ac.standrews.cs.trombone.event.workload.Workload;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class BaseScenario extends Scenario {

    protected BaseScenario(String name, Provider<Churn> churn_provider, Provider<Workload> workload_provider, PeerConfiguration configuration) {

        super(name, Constants.SCENARIO_MASTER_SEED);
        setPeerKeyProvider(Constants.PEER_KEY_PROVIDER.clone());
        setExperimentDuration(Constants.EXPERIMENT_DURATION);
        setObservationInterval(Constants.OBSERVATION_INTERVAL);
        setLookupRetryCount(Constants.LOOKUP_RETRY_COUNT);
        addHost("localhost", 1000, Constants.PORT_NUMBER_PROVIDER);

        setChurnProvider(churn_provider);
        setWorkloadProvider(workload_provider);
        setPeerConfiguration(configuration);
    }

    public static void main(String[] args) {

        String name = "Scenario_";
        int i = 1;

        Combinations.generateArgumentCombinations(new Object[][] {
                {"SCENARIO"}, {Constants.CHURN_1, Constants.CHURN_2, Constants.CHURN_3, Constants.CHURN_4}, {Constants.WORKLOAD_1, Constants.WORKLOAD_2, Constants.NO_WORKLOAD}, {Constants.NO_MAINTENANCE}
        });

    }

}
