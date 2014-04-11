package uk.ac.standrews.cs.trombone.evaluation;

import java.util.List;
import uk.ac.standrews.cs.trombone.core.Maintenance;
import uk.ac.standrews.cs.trombone.core.SyntheticDelay;
import uk.ac.standrews.cs.trombone.evaluation.scenarios.BaseScenario;
import uk.ac.standrews.cs.trombone.evaluation.scenarios.Constants;
import uk.ac.standrews.cs.trombone.evaluation.scenarios.PeerConfigurationGenerator;
import uk.ac.standrews.cs.trombone.event.Scenario;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public final class ScenarioBatches {

    private static final Object[] ALL_CHURN_MODELS = {Constants.NO_CHURN, Constants.CHURN_1, Constants.CHURN_2, Constants.CHURN_3, Constants.CHURN_4, Constants.CHURN_5, Constants.CHURN_6};
    private static final Object[] ALL_WORKLOAD_MODELS = {Constants.NO_WORKLOAD, Constants.WORKLOAD_1, Constants.WORKLOAD_2, Constants.WORKLOAD_3};

    private static final SyntheticDelay[] SYNTHETIC_DELAYS = {Constants.BLUB_UNIFORMLY_DISTRIBUTED_SYNTHETIC_DELAY};
    private static final Object[] CONFIGURATION_STATIC_AND_BASIC_ADAPTIVE = PeerConfigurationGenerator.generate(
            //@formatter:off
            new Maintenance[] {
                    //Static Maintenance
                    Constants.NO_MAINTENANCE, 
                    Constants.SUCCESSOR_LIST_MAINTENANCE_5, 
                    Constants.SUCCESSOR_MAINTENANCE, 
                    Constants.RANDOM_SELECTOR_MAINTENANCE_3, 
                    
                    //Basic Adaptive: no clustering
                    Constants.EVOLUTIONARY_MAINTENANCE
            },
            //@formatter:on

            SYNTHETIC_DELAYS
    ).toArray();
    private static final Object[] CONFIGURATION_ADAPTIVE = PeerConfigurationGenerator.generate(
            //@formatter:off
            new Maintenance[] {
                    Constants.EVOLUTIONARY_MAINTENANCE,
                    Constants.EVOLUTIONARY_MAINTENANCE_PFCLUST,
                    Constants.EVOLUTIONARY_MAINTENANCE_KMEAN_PLUS_PLUS,
            },
            //@formatter:on

            SYNTHETIC_DELAYS
    ).toArray();

    private static final Object[][] BATCH_1 = {
            ALL_CHURN_MODELS, ALL_WORKLOAD_MODELS, CONFIGURATION_STATIC_AND_BASIC_ADAPTIVE
    };

    private static final Object[][] BATCH_2 = {
            ALL_CHURN_MODELS, ALL_WORKLOAD_MODELS, CONFIGURATION_ADAPTIVE
    };

    private static final Object[][] BATCH_3 = {
            ALL_CHURN_MODELS, ALL_WORKLOAD_MODELS, PeerConfigurationGenerator.generate(new Maintenance[] {Constants.RANDOM_MAINTENANCE}, SYNTHETIC_DELAYS).toArray()
    };

    public static final List<Scenario> BATCH_1_SCENARIOS = BaseScenario.generateAll("scenario_", BATCH_1);
    public static final List<Scenario> BATCH_2_SCENARIOS = BaseScenario.generateAll("scenario_batch2_", BATCH_2);
    public static final List<Scenario> BATCH_3_SCENARIOS = BaseScenario.generateAll("scenario_batch3_", BATCH_3);
}
