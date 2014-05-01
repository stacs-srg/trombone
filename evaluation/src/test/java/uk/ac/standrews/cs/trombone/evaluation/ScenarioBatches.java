package uk.ac.standrews.cs.trombone.evaluation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import uk.ac.standrews.cs.shabdiz.util.Duration;
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

    //    private static final Object[] ALL_CHURN_MODELS = {Constants.NO_CHURN, Constants.CHURN_1, Constants.CHURN_2, Constants.CHURN_3, Constants.CHURN_4, Constants.CHURN_5, Constants.CHURN_6};
    private static final Object[] ALL_CHURN_MODELS = {Constants.NO_CHURN, Constants.CHURN_1, Constants.CHURN_2, Constants.CHURN_4, Constants.CHURN_5, Constants.CHURN_6};
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
                    Constants.MOST_RECENTLY_SEEN_3,
                    
                    Constants.EVOLUTIONARY_MAINTENANCE_PFCLUST_10 ,
                    Constants.EVOLUTIONARY_MAINTENANCE_PER_POINT_CLUSTER_10,
                    Constants.EVOLUTIONARY_MAINTENANCE_KMEAN_PLUS_PLUS_10,
                    Constants.RANDOM_MAINTENANCE_10
            },
            //@formatter:on

            SYNTHETIC_DELAYS
    ).toArray();
    private static final Object[] CONFIGURATION_ADAPTIVE = PeerConfigurationGenerator.generate(
            //@formatter:off
            new Maintenance[] {
                    Constants.EVOLUTIONARY_MAINTENANCE_PER_POINT_CLUSTER_10,
                    Constants.EVOLUTIONARY_MAINTENANCE_PFCLUST_10,
                    Constants.EVOLUTIONARY_MAINTENANCE_KMEAN_PLUS_PLUS_10,
            },
            //@formatter:on
            SYNTHETIC_DELAYS
    ).toArray();

    private static final Object[][] BATCH_1 = {
            ALL_CHURN_MODELS, ALL_WORKLOAD_MODELS, CONFIGURATION_STATIC_AND_BASIC_ADAPTIVE, {Constants.EXPERIMENT_DURATION_4}
    };

    private static final Object[][] BATCH_2 = {
            ALL_CHURN_MODELS, ALL_WORKLOAD_MODELS, CONFIGURATION_ADAPTIVE, {Constants.EXPERIMENT_DURATION_4}
    };

    private static final Object[][] BATCH_3 = {
            ALL_CHURN_MODELS, ALL_WORKLOAD_MODELS, PeerConfigurationGenerator.generate(new Maintenance[] {Constants.RANDOM_MAINTENANCE_10}, SYNTHETIC_DELAYS).toArray(), {Constants.EXPERIMENT_DURATION_4}
    };

    private static final Object[][] BATCH_4 = {
            ALL_CHURN_MODELS, ALL_WORKLOAD_MODELS, PeerConfigurationGenerator.generate(new Maintenance[] {
                    Constants.EVOLUTIONARY_MAINTENANCE_PFCLUST_10, Constants.EVOLUTIONARY_MAINTENANCE_PFCLUST_20, Constants.EVOLUTIONARY_MAINTENANCE_PFCLUST_30, Constants.EVOLUTIONARY_MAINTENANCE_PFCLUST_40, Constants.EVOLUTIONARY_MAINTENANCE_PFCLUST_50
            }, SYNTHETIC_DELAYS
    ).toArray(), {Constants.EXPERIMENT_DURATION_4}
    };

    private static final Object[][] BATCH_5 = {
            ALL_CHURN_MODELS, ALL_WORKLOAD_MODELS, PeerConfigurationGenerator.generate(new Maintenance[] {Constants.EVOLUTIONARY_MAINTENANCE_PFCLUST_10_STOP_AFTER_10_HOURS}, SYNTHETIC_DELAYS).toArray(), {Constants.EXPERIMENT_DURATION_14}
    };
    private static final Object[][] BATCH_6 = {
            ALL_CHURN_MODELS, ALL_WORKLOAD_MODELS, PeerConfigurationGenerator.generate(new Maintenance[] {Constants.EVOLUTIONARY_MAINTENANCE_PFCLUST_10_STOP_AFTER_8_HOURS}, SYNTHETIC_DELAYS).toArray(), {Constants.EXPERIMENT_DURATION_12}
    };
    private static final Object[][] BATCH_7 = {
            ALL_CHURN_MODELS, ALL_WORKLOAD_MODELS, PeerConfigurationGenerator.generate(new Maintenance[] {Constants.EVOLUTIONARY_MAINTENANCE_PFCLUST_10_STOP_AFTER_6_HOURS}, SYNTHETIC_DELAYS).toArray(), {Constants.EXPERIMENT_DURATION_10}
    };
    private static final Object[][] BATCH_8 = {
            ALL_CHURN_MODELS, ALL_WORKLOAD_MODELS, PeerConfigurationGenerator.generate(new Maintenance[] {Constants.EVOLUTIONARY_MAINTENANCE_PFCLUST_10_STOP_AFTER_4_HOURS}, SYNTHETIC_DELAYS).toArray(), {Constants.EXPERIMENT_DURATION_8}
    };
    private static final Object[][] BATCH_9 = {
            ALL_CHURN_MODELS, ALL_WORKLOAD_MODELS, PeerConfigurationGenerator.generate(new Maintenance[] {Constants.EVOLUTIONARY_MAINTENANCE_PFCLUST_10_STOP_AFTER_2_HOURS}, SYNTHETIC_DELAYS).toArray(), {Constants.EXPERIMENT_DURATION_6}
    };

    public static final List<Scenario> BATCH_1_SCENARIOS = BaseScenario.generateAll("scenario_", BATCH_1);
    public static final List<Scenario> BATCH_2_SCENARIOS = BaseScenario.generateAll("scenario_batch2_", BATCH_2);
    public static final List<Scenario> BATCH_3_SCENARIOS = BaseScenario.generateAll("scenario_batch3_", BATCH_3);
    public static final List<Scenario> BATCH_4_SCENARIOS = BaseScenario.generateAll("scenario_batch4_", BATCH_4);

    public static final List<Scenario> BATCH_5_SCENARIOS = BaseScenario.generateAll("scenario_batch5_", BATCH_5);
    public static final List<Scenario> BATCH_6_SCENARIOS = BaseScenario.generateAll("scenario_batch6_", BATCH_6);
    public static final List<Scenario> BATCH_7_SCENARIOS = BaseScenario.generateAll("scenario_batch7_", BATCH_7);
    public static final List<Scenario> BATCH_8_SCENARIOS = BaseScenario.generateAll("scenario_batch8_", BATCH_8);
    public static final List<Scenario> BATCH_9_SCENARIOS = BaseScenario.generateAll("scenario_batch9_", BATCH_9);
    public static final List<Scenario> BATCHES_5_TO_9_SCENARIOS = new ArrayList<>();

    static {

        BATCHES_5_TO_9_SCENARIOS.addAll(BATCH_5_SCENARIOS);
        BATCHES_5_TO_9_SCENARIOS.addAll(BATCH_6_SCENARIOS);
        BATCHES_5_TO_9_SCENARIOS.addAll(BATCH_7_SCENARIOS);
        BATCHES_5_TO_9_SCENARIOS.addAll(BATCH_8_SCENARIOS);
        BATCHES_5_TO_9_SCENARIOS.addAll(BATCH_9_SCENARIOS);
    }

    public static void main(String[] args) {

        System.out.println(BATCH_1_SCENARIOS.size());
        Duration dd = new Duration(0, TimeUnit.HOURS);
        for (Scenario scenario : BATCH_1_SCENARIOS) {

            dd = dd.add(scenario.getExperimentDuration());
        }

        System.out.println(dd);
    }

}
