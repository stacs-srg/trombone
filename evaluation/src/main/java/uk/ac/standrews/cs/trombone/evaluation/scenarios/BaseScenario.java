package uk.ac.standrews.cs.trombone.evaluation.scenarios;

import java.util.ArrayList;
import java.util.List;
import uk.ac.standrews.cs.shabdiz.util.Combinations;
import uk.ac.standrews.cs.trombone.core.PeerConfiguration;
import uk.ac.standrews.cs.trombone.event.Scenario;
import uk.ac.standrews.cs.trombone.event.churn.Churn;
import uk.ac.standrews.cs.trombone.event.churn.Workload;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class BaseScenario extends Scenario {

    protected BaseScenario(String name, Churn churn, Workload workload, PeerConfiguration configuration) {

        super(name, Constants.SCENARIO_MASTER_SEED);
        setPeerKeyProvider(Constants.PEER_KEY_PROVIDER.copy());
        setExperimentDuration(Constants.EXPERIMENT_DURATION_4);
        setObservationInterval(Constants.OBSERVATION_INTERVAL);
        setLookupRetryCount(Constants.LOOKUP_RETRY_COUNT);
        addHost("localhost", Constants.NETWORK_SIZE, Constants.PORT_NUMBER_PROVIDER, churn.copy(), workload.copy(), configuration);

    }

    public static List<Scenario> generateAll(String scenario_name_prefix, Object[][] arguments) {

        final List<Scenario> scenarios = new ArrayList<>();
        final List<Object[]> constructor_arguments = Combinations.generateArgumentCombinations(arguments);

        int index = 1;
        for (Object[] argument : constructor_arguments) {
            final Churn churn = (Churn) argument[0];
            final Workload workload = (Workload) argument[1];
            final PeerConfiguration configuration = (PeerConfiguration) argument[2];
            final String name = scenario_name_prefix + index++;
            scenarios.add(new BaseScenario(name, churn, workload, configuration));

        }

        return scenarios;
    }
}
