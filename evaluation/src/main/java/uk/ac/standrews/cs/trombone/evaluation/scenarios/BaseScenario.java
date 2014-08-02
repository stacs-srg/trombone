package uk.ac.standrews.cs.trombone.evaluation.scenarios;

import java.util.ArrayList;
import java.util.List;
import uk.ac.standrews.cs.shabdiz.util.Combinations;
import uk.ac.standrews.cs.shabdiz.util.Duration;
import uk.ac.standrews.cs.trombone.core.PeerConfiguration;
import uk.ac.standrews.cs.trombone.event.Scenario;
import uk.ac.standrews.cs.trombone.event.environment.Churn;
import uk.ac.standrews.cs.trombone.event.environment.Workload;

import static uk.ac.standrews.cs.trombone.evaluation.scenarios.Constants.LOOKUP_RETRY_COUNT;
import static uk.ac.standrews.cs.trombone.evaluation.scenarios.Constants.NETWORK_SIZE;
import static uk.ac.standrews.cs.trombone.evaluation.scenarios.Constants.OBSERVATION_INTERVAL;
import static uk.ac.standrews.cs.trombone.evaluation.scenarios.Constants.PEER_KEY_PROVIDER;
import static uk.ac.standrews.cs.trombone.evaluation.scenarios.Constants.PORT_NUMBER_PROVIDER;
import static uk.ac.standrews.cs.trombone.evaluation.scenarios.Constants.SCENARIO_MASTER_SEED;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class BaseScenario extends Scenario {

    protected BaseScenario(String name, Churn churn, Workload workload, PeerConfiguration configuration) {

        this(name, churn, workload, configuration, Constants.EXPERIMENT_DURATION_4);
    }

    protected BaseScenario(String name, Churn churn, Workload workload, PeerConfiguration configuration, Duration experiment_duration) {

        super(name, SCENARIO_MASTER_SEED);
        setPeerKeyProvider(PEER_KEY_PROVIDER.get());
        setExperimentDuration(experiment_duration);
        setObservationInterval(OBSERVATION_INTERVAL);
        setLookupRetryCount(LOOKUP_RETRY_COUNT);
        addHost("localhost", NETWORK_SIZE, PORT_NUMBER_PROVIDER, churn, workload, configuration);
    }

    static List<Scenario> generateAll(String scenario_name_prefix, Churn[] churns, Workload[] workloads, PeerConfiguration[] configurations, Duration[] experiment_durations) {

        final List<Scenario> scenarios = new ArrayList<>();
        final Object[][] arguments = {churns, workloads, configurations, experiment_durations};
        final List<Object[]> constructor_arguments = Combinations.generateArgumentCombinations(arguments);

        int index = 1;
        for (final Object[] argument : constructor_arguments) {

            final Churn churn = (Churn) argument[0];
            final Workload workload = (Workload) argument[1];
            final PeerConfiguration configuration = (PeerConfiguration) argument[2];
            final Duration experiment_duration = (Duration) argument[3];
            final String name = scenario_name_prefix + index++;
            scenarios.add(new BaseScenario(name, churn, workload, configuration, experiment_duration));
        }

        return scenarios;
    }
}
