package uk.ac.standrews.cs.trombone.evaluation.scenarios;

import java.util.ArrayList;
import java.util.List;
import uk.ac.standrews.cs.shabdiz.util.Combinations;
import uk.ac.standrews.cs.trombone.core.Maintenance;
import uk.ac.standrews.cs.trombone.core.PeerConfiguration;
import uk.ac.standrews.cs.trombone.core.SyntheticDelay;
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
        setExperimentDuration(Constants.EXPERIMENT_DURATION);
        setObservationInterval(Constants.OBSERVATION_INTERVAL);
        setLookupRetryCount(Constants.LOOKUP_RETRY_COUNT);
        addHost("localhost", Constants.NETWORK_SIZE, Constants.PORT_NUMBER_PROVIDER, churn.copy(), workload.copy(), configuration);

    }

    public static List<BaseScenario> generateAll() {

        final List<BaseScenario> scenarios = new ArrayList<>();

        final List<Object[]> arguments = Combinations.generateArgumentCombinations(new Object[][] {

                {Constants.NO_CHURN, Constants.CHURN_1, Constants.CHURN_2, Constants.CHURN_3, Constants.CHURN_4, Constants.CHURN_5, Constants.CHURN_6},

                {Constants.NO_WORKLOAD, Constants.WORKLOAD_1, Constants.WORKLOAD_2, Constants.WORKLOAD_3},

                PeerConfigurationGenerator.generate(

                        new Maintenance[] {

                                Constants.NO_MAINTENANCE, 
                                Constants.SUCCESSOR_LIST_MAINTENANCE_5, 
                                Constants.SUCCESSOR_MAINTENANCE, Constants.RANDOM_MAINTENANCE_2,

                                Constants.EVOLUTIONARY_MAINTENANCE

                        },

                        new SyntheticDelay[] {Constants.BLUB_UNIFORMLY_DISTRIBUTED_SYNTHETIC_DELAY}

                ).toArray()
        });

        int index = 1;
        for (Object[] argument : arguments) {

            final Churn churn = (Churn) argument[0];
            final Workload workload = (Workload) argument[1];
            final PeerConfiguration configuration = (PeerConfiguration) argument[2];

            final String name = "scenario_" + index++;

            scenarios.add(new BaseScenario(name, churn, workload, configuration));

        }

        return scenarios;
    }

    public static void main(String[] args) {

        System.out.println(generateAll().size());
    }

}
