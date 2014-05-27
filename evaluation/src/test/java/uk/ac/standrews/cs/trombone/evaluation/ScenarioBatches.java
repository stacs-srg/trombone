package uk.ac.standrews.cs.trombone.evaluation;

import java.util.ArrayList;
import java.util.List;
import uk.ac.standrews.cs.trombone.core.Maintenance;
import uk.ac.standrews.cs.trombone.core.SyntheticDelay;
import uk.ac.standrews.cs.trombone.evaluation.scenarios.BaseScenario;
import uk.ac.standrews.cs.trombone.evaluation.scenarios.Constants;
import uk.ac.standrews.cs.trombone.event.Scenario;

import static uk.ac.standrews.cs.trombone.evaluation.scenarios.PeerConfigurationGenerator.generate;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public final class ScenarioBatches {

    private static final Object[] ALL_CHURN_MODELS = {
            Constants.NO_CHURN, Constants.CHURN_1, Constants.CHURN_2, Constants.CHURN_4,
            Constants.CHURN_5, Constants.CHURN_6
    };
    private static final Object[] CHURN_RATE_MODELS = {
            Constants.CHURN_4_15_MIN, Constants.CHURN_4_1_HOUR
    };
    private static final Object[] CHURN_RATE_MODELS_WITH_OSCI = {
            Constants.CHURN_4, Constants.CHURN_4_15_MIN, Constants.CHURN_4_1_HOUR, Constants.CHURN_5
    };
    private static final Object[] ALL_WORKLOAD_MODELS = {
            Constants.NO_WORKLOAD, Constants.WORKLOAD_1, Constants.WORKLOAD_2, Constants.WORKLOAD_3
    };

    private static final SyntheticDelay[] SYNTHETIC_DELAYS = {
            Constants.BLUB_UNIFORMLY_DISTRIBUTED_SYNTHETIC_DELAY
    };
    private static final Object[] CONFIGURATION_STATIC_AND_ADAPTIVE = generate(
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

    private static final Object[] EVOLUTIONARY_RANDOM_TRIAL_TIME = generate(
            //@formatter:off
            new Maintenance[] {
                    Constants.EVOLUTIONARY_MAINTENANCE_PFCLUST_10_30_SEC ,
                    Constants.EVOLUTIONARY_MAINTENANCE_PFCLUST_10_1_MIN ,
                    Constants.EVOLUTIONARY_MAINTENANCE_PFCLUST_10_4_MIN ,
                    Constants.EVOLUTIONARY_MAINTENANCE_PFCLUST_10_8_MIN, 
                    Constants.RANDOM_MAINTENANCE_10_30_SEC ,
                    Constants.RANDOM_MAINTENANCE_10_1_MIN ,
                    Constants.RANDOM_MAINTENANCE_10_4_MIN ,
                    Constants.RANDOM_MAINTENANCE_10_8_MIN 
            },
            //@formatter:on

            SYNTHETIC_DELAYS
    ).toArray();

    private static final Object[][] STATIC_AND_ADAPTIVE_4H = {
            ALL_CHURN_MODELS, ALL_WORKLOAD_MODELS, CONFIGURATION_STATIC_AND_ADAPTIVE,
            {Constants.EXPERIMENT_DURATION_4}
    };

    public static final Object[] EVOLUTIONARY_POPULATION_20_50 = generate(new Maintenance[] {
                    Constants.EVOLUTIONARY_MAINTENANCE_PFCLUST_20,
                    Constants.EVOLUTIONARY_MAINTENANCE_PFCLUST_30,
                    Constants.EVOLUTIONARY_MAINTENANCE_PFCLUST_40,
                    Constants.EVOLUTIONARY_MAINTENANCE_PFCLUST_50
            }, SYNTHETIC_DELAYS
    ).toArray();
    public static final Object[] EVOLUTIONARY_RANDOM_48_H = generate(new Maintenance[] {
                    Constants.EVOLUTIONARY_MAINTENANCE_PFCLUST_10_STOP_AFTER_48_HOURS,
                    Constants.RANDOM_MAINTENANCE_10_STOP_AFTER_48_HOURS
            }, SYNTHETIC_DELAYS
    ).toArray();
    private static final Object[][] EVOLUTIONARY_POPULATION_SIZES = {
            ALL_CHURN_MODELS, ALL_WORKLOAD_MODELS, EVOLUTIONARY_POPULATION_20_50,
            {Constants.EXPERIMENT_DURATION_4}
    };

    private static final Object[][] EVOLUTIONARY_14H = {
            ALL_CHURN_MODELS, ALL_WORKLOAD_MODELS, generate(new Maintenance[] {
            Constants.EVOLUTIONARY_MAINTENANCE_PFCLUST_10_STOP_AFTER_10_HOURS
    }, SYNTHETIC_DELAYS).toArray(), {Constants.EXPERIMENT_DURATION_14}
    };
    private static final Object[][] EVOLUTIONARY_12H = {
            ALL_CHURN_MODELS, ALL_WORKLOAD_MODELS, generate(new Maintenance[] {
            Constants.EVOLUTIONARY_MAINTENANCE_PFCLUST_10_STOP_AFTER_8_HOURS
    }, SYNTHETIC_DELAYS).toArray(), {Constants.EXPERIMENT_DURATION_12}
    };
    private static final Object[][] EVOLUTIONARY_10H = {
            ALL_CHURN_MODELS, ALL_WORKLOAD_MODELS, generate(new Maintenance[] {
            Constants.EVOLUTIONARY_MAINTENANCE_PFCLUST_10_STOP_AFTER_6_HOURS
    }, SYNTHETIC_DELAYS).toArray(), {Constants.EXPERIMENT_DURATION_10}
    };
    private static final Object[][] EVOLUTIONARY_8H = {
            ALL_CHURN_MODELS, ALL_WORKLOAD_MODELS, generate(new Maintenance[] {
            Constants.EVOLUTIONARY_MAINTENANCE_PFCLUST_10_STOP_AFTER_4_HOURS
    }, SYNTHETIC_DELAYS).toArray(), {Constants.EXPERIMENT_DURATION_8}
    };
    private static final Object[][] EVOLUTIONARY_6H = {
            ALL_CHURN_MODELS, ALL_WORKLOAD_MODELS, generate(new Maintenance[] {
            Constants.EVOLUTIONARY_MAINTENANCE_PFCLUST_10_STOP_AFTER_2_HOURS
    }, SYNTHETIC_DELAYS).toArray(), {Constants.EXPERIMENT_DURATION_6}
    };

    private static final Object[][] RANDOM_14H = {
            ALL_CHURN_MODELS, ALL_WORKLOAD_MODELS, generate(new Maintenance[] {
            Constants.RANDOM_MAINTENANCE_10_STOP_AFTER_10_HOURS
    }, SYNTHETIC_DELAYS).toArray(), {Constants.EXPERIMENT_DURATION_14}
    };
    private static final Object[][] RANDOM_12H = {
            ALL_CHURN_MODELS, ALL_WORKLOAD_MODELS, generate(new Maintenance[] {
            Constants.RANDOM_MAINTENANCE_10_STOP_AFTER_8_HOURS
    }, SYNTHETIC_DELAYS).toArray(), {Constants.EXPERIMENT_DURATION_12}
    };
    private static final Object[][] RANDOM_10H = {
            ALL_CHURN_MODELS, ALL_WORKLOAD_MODELS, generate(new Maintenance[] {
            Constants.RANDOM_MAINTENANCE_10_STOP_AFTER_6_HOURS
    }, SYNTHETIC_DELAYS).toArray(), {Constants.EXPERIMENT_DURATION_10}
    };
    private static final Object[][] RANDOM_8H = {
            ALL_CHURN_MODELS, ALL_WORKLOAD_MODELS, generate(new Maintenance[] {
            Constants.RANDOM_MAINTENANCE_10_STOP_AFTER_4_HOURS
    }, SYNTHETIC_DELAYS).toArray(), {Constants.EXPERIMENT_DURATION_8}
    };
    private static final Object[][] RANDOM_6H = {
            ALL_CHURN_MODELS, ALL_WORKLOAD_MODELS, generate(new Maintenance[] {
            Constants.RANDOM_MAINTENANCE_10_STOP_AFTER_2_HOURS
    }, SYNTHETIC_DELAYS).toArray(), {Constants.EXPERIMENT_DURATION_6}
    };
    private static final Object[][] RANDOM_4H = {
            ALL_CHURN_MODELS, ALL_WORKLOAD_MODELS, generate(new Maintenance[] {
            Constants.RANDOM_MAINTENANCE_10
    }, SYNTHETIC_DELAYS).toArray(), {Constants.EXPERIMENT_DURATION_4}
    };

    private static final Object[][] CHURN_RATE = {
            CHURN_RATE_MODELS, {Constants.WORKLOAD_2}, CONFIGURATION_STATIC_AND_ADAPTIVE,
            {Constants.EXPERIMENT_DURATION_4}
    };

    private static final Object[][] TRIAL_TIME = {
            CHURN_RATE_MODELS, {Constants.WORKLOAD_2}, EVOLUTIONARY_RANDOM_TRIAL_TIME,
            {Constants.EXPERIMENT_DURATION_4}
    };
    private static final Object[][] CHURN_POPULATION_SIZE = {
            CHURN_RATE_MODELS, {Constants.WORKLOAD_2}, EVOLUTIONARY_POPULATION_20_50,
            {Constants.EXPERIMENT_DURATION_4}
    };
    private static final Object[][] EVOLUTIONARY_RANDOM_52_H = {
            CHURN_RATE_MODELS_WITH_OSCI, {Constants.WORKLOAD_2}, EVOLUTIONARY_RANDOM_48_H,
            {Constants.EXPERIMENT_DURATION_52}
    };

    public static final List<Scenario> STATIC_AND_ADAPTIVE_4H_SCENARIOS = BaseScenario.generateAll("scenario_", STATIC_AND_ADAPTIVE_4H);
    public static final List<Scenario> EVOLUTIONARY_POPULATIONS_20_TO_50_SCENARIOS = BaseScenario.generateAll("evolutionary_p_4h_", EVOLUTIONARY_POPULATION_SIZES);

    public static final List<Scenario> EVOLUTIONARY_6H_PLUS_SCENARIOS = new ArrayList<>();
    public static final List<Scenario> EVOLUTIONARY_14H_SCENARIOS = BaseScenario.generateAll("evolutionary_14h_", EVOLUTIONARY_14H);
    public static final List<Scenario> EVOLUTIONARY_12H_SCENARIOS = BaseScenario.generateAll("evolutionary_12h_", EVOLUTIONARY_12H);
    public static final List<Scenario> EVOLUTIONARY_10H_SCENARIOS = BaseScenario.generateAll("evolutionary_10h_", EVOLUTIONARY_10H);
    public static final List<Scenario> EVOLUTIONARY_8H_SCENARIOS = BaseScenario.generateAll("evolutionary_8h_", EVOLUTIONARY_8H);
    public static final List<Scenario> EVOLUTIONARY_6H_SCENARIOS = BaseScenario.generateAll("evolutionary_6h_", EVOLUTIONARY_6H);

    public static final List<Scenario> RANDOM_4H_PLUS_SCENARIOS = new ArrayList<>();
    public static final List<Scenario> RANDOM_14H_SCENARIOS = BaseScenario.generateAll("random_14h_", RANDOM_14H);
    public static final List<Scenario> RANDOM_12H_SCENARIOS = BaseScenario.generateAll("random_12h_", RANDOM_12H);
    public static final List<Scenario> RANDOM_10H_SCENARIOS = BaseScenario.generateAll("random_10h_", RANDOM_10H);
    public static final List<Scenario> RANDOM_8H_SCENARIOS = BaseScenario.generateAll("random_8h_", RANDOM_8H);
    public static final List<Scenario> RANDOM_6H_SCENARIOS = BaseScenario.generateAll("random_6h_", RANDOM_6H);
    public static final List<Scenario> RANDOM_4H_SCENARIOS = BaseScenario.generateAll("random_4h_", RANDOM_4H);

    public static final List<Scenario> CHURN_RATE_SCENARIOS = BaseScenario.generateAll("churn_4h_", CHURN_RATE);
    public static final List<Scenario> TRIAL_TIME_SCENARIOS = BaseScenario.generateAll("trial_time_4h_", TRIAL_TIME);

    public static final List<Scenario> CHURN_POPULATION_SIZE_SCENARIOS = BaseScenario.generateAll("churn_population_size_4h_", CHURN_POPULATION_SIZE);
    public static final List<Scenario> EVOLUTIONARY_RANDOM_52_H_SCENARIOS = BaseScenario.generateAll("evolutionary_random_52h_", EVOLUTIONARY_RANDOM_52_H);

    static {

        EVOLUTIONARY_6H_PLUS_SCENARIOS.addAll(EVOLUTIONARY_14H_SCENARIOS);
        EVOLUTIONARY_6H_PLUS_SCENARIOS.addAll(EVOLUTIONARY_12H_SCENARIOS);
        EVOLUTIONARY_6H_PLUS_SCENARIOS.addAll(EVOLUTIONARY_10H_SCENARIOS);
        EVOLUTIONARY_6H_PLUS_SCENARIOS.addAll(EVOLUTIONARY_8H_SCENARIOS);
        EVOLUTIONARY_6H_PLUS_SCENARIOS.addAll(EVOLUTIONARY_6H_SCENARIOS);

        RANDOM_4H_PLUS_SCENARIOS.addAll(RANDOM_14H_SCENARIOS);
        RANDOM_4H_PLUS_SCENARIOS.addAll(RANDOM_12H_SCENARIOS);
        RANDOM_4H_PLUS_SCENARIOS.addAll(RANDOM_10H_SCENARIOS);
        RANDOM_4H_PLUS_SCENARIOS.addAll(RANDOM_8H_SCENARIOS);
        RANDOM_4H_PLUS_SCENARIOS.addAll(RANDOM_6H_SCENARIOS);
        RANDOM_4H_PLUS_SCENARIOS.addAll(RANDOM_4H_SCENARIOS);
    }
}
