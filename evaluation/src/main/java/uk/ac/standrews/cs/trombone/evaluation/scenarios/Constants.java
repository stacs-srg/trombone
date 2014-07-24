package uk.ac.standrews.cs.trombone.evaluation.scenarios;

import java.util.concurrent.TimeUnit;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.apache.commons.math3.random.MersenneTwister;
import uk.ac.standrews.cs.shabdiz.util.Duration;
import uk.ac.standrews.cs.trombone.core.MaintenanceFactory;
import uk.ac.standrews.cs.trombone.core.PeerConfiguration;
import uk.ac.standrews.cs.trombone.core.adaptation.EvaluatedDisseminationStrategy;
import uk.ac.standrews.cs.trombone.core.adaptation.EvolutionaryMaintenanceFactory;
import uk.ac.standrews.cs.trombone.core.adaptation.RandomMaintenanceFactory;
import uk.ac.standrews.cs.trombone.core.adaptation.clustering.PerPointClusterer;
import uk.ac.standrews.cs.trombone.core.adaptation.clustering.pfclust.PFClustClusterer;
import uk.ac.standrews.cs.trombone.core.key.KeyProvider;
import uk.ac.standrews.cs.trombone.core.util.Probability;
import uk.ac.standrews.cs.trombone.event.environment.Churn;
import uk.ac.standrews.cs.trombone.event.environment.FixedExponentialInterval;
import uk.ac.standrews.cs.trombone.event.environment.IntervalGenerator;
import uk.ac.standrews.cs.trombone.event.environment.OscillatingExponentialInterval;
import uk.ac.standrews.cs.trombone.event.environment.Workload;
import uk.ac.standrews.cs.trombone.event.provider.SequentialPortNumberProvider;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public final class Constants {

    private Constants() {

    }

    public static final long SCENARIO_MASTER_SEED = 1413;
    public static final int NUMBER_OF_REPETITIONS = 5;
    public static final int NETWORK_SIZE = 1_0;
    public static final PFClustClusterer<EvaluatedDisseminationStrategy> PF_CLUST_CLUSTERER = new PFClustClusterer<>(SCENARIO_MASTER_SEED);

    public static final Duration EXPERIMENT_DURATION_52 = new Duration(52, TimeUnit.HOURS);
    public static final Duration EXPERIMENT_DURATION_14 = new Duration(14, TimeUnit.HOURS);
    public static final Duration EXPERIMENT_DURATION_12 = new Duration(12, TimeUnit.HOURS);
    public static final Duration EXPERIMENT_DURATION_10 = new Duration(10, TimeUnit.HOURS);
    public static final Duration EXPERIMENT_DURATION_8 = new Duration(8, TimeUnit.HOURS);
    public static final Duration EXPERIMENT_DURATION_6 = new Duration(6, TimeUnit.HOURS);
    public static final Duration EXPERIMENT_DURATION_4 = new Duration(4, TimeUnit.HOURS);

    public static final Probability MUTATION_PROBABILITY = new Probability(0.1);
    public static final MaintenanceFactory NO_MAINTENANCE = new MaintenanceFactory(null);
    public static final MaintenanceFactory SUCCESSOR_MAINTENANCE = new MaintenanceFactory(new SuccessorMaintenance());
    public static final MaintenanceFactory SUCCESSOR_LIST_MAINTENANCE_5 = new MaintenanceFactory(new SuccessorListMaintenance(5));
    public static final MaintenanceFactory RANDOM_SELECTOR_MAINTENANCE_3 = new MaintenanceFactory(new RandomSelectorMaintenance(3, 3));
    public static final MaintenanceFactory MOST_RECENTLY_SEEN_3 = new MaintenanceFactory(new MostRecentlySeenMaintenance(3, 3));

    public static final EvolutionaryMaintenanceFactory.ElapsedTimeTerminationCondition TERMINATION_CONDITION_48 = new EvolutionaryMaintenanceFactory.ElapsedTimeTerminationCondition(48, TimeUnit.HOURS);
    public static final EvolutionaryMaintenanceFactory.ElapsedTimeTerminationCondition TERMINATION_CONDITION_10 = new EvolutionaryMaintenanceFactory.ElapsedTimeTerminationCondition(10, TimeUnit.HOURS);
    public static final EvolutionaryMaintenanceFactory.ElapsedTimeTerminationCondition TERMINATION_CONDITION_8 = new EvolutionaryMaintenanceFactory.ElapsedTimeTerminationCondition(8, TimeUnit.HOURS);
    public static final EvolutionaryMaintenanceFactory.ElapsedTimeTerminationCondition TERMINATION_CONDITION_6 = new EvolutionaryMaintenanceFactory.ElapsedTimeTerminationCondition(6, TimeUnit.HOURS);
    public static final EvolutionaryMaintenanceFactory.ElapsedTimeTerminationCondition TERMINATION_CONDITION_4 = new EvolutionaryMaintenanceFactory.ElapsedTimeTerminationCondition(4, TimeUnit.HOURS);
    public static final EvolutionaryMaintenanceFactory.ElapsedTimeTerminationCondition TERMINATION_CONDITION_2 = new EvolutionaryMaintenanceFactory.ElapsedTimeTerminationCondition(2, TimeUnit.HOURS);

    public static final EvolutionaryMaintenanceFactory EVOLUTIONARY_MAINTENANCE_PFCLUST_10_30_SEC = new EvolutionaryMaintenanceFactory(10, 2, MUTATION_PROBABILITY, 30, TimeUnit.SECONDS, PF_CLUST_CLUSTERER, 5, 3);
    public static final EvolutionaryMaintenanceFactory EVOLUTIONARY_MAINTENANCE_PFCLUST_10_1_MIN = new EvolutionaryMaintenanceFactory(10, 2, MUTATION_PROBABILITY, 1, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, 5, 3);
    public static final EvolutionaryMaintenanceFactory EVOLUTIONARY_MAINTENANCE_PFCLUST_10_4_MIN = new EvolutionaryMaintenanceFactory(10, 2, MUTATION_PROBABILITY, 4, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, 5, 3);
    public static final EvolutionaryMaintenanceFactory EVOLUTIONARY_MAINTENANCE_PFCLUST_10_8_MIN = new EvolutionaryMaintenanceFactory(10, 2, MUTATION_PROBABILITY, 8, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, 5, 3);
    public static final EvolutionaryMaintenanceFactory EVOLUTIONARY_MAINTENANCE_PFCLUST_10 = new EvolutionaryMaintenanceFactory(10, 2, MUTATION_PROBABILITY, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, 5, 3);
    public static final EvolutionaryMaintenanceFactory EVOLUTIONARY_MAINTENANCE_PFCLUST_10_STOP_AFTER_48_HOURS = new EvolutionaryMaintenanceFactory(10, 2, MUTATION_PROBABILITY, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, 5, 3);
    public static final EvolutionaryMaintenanceFactory EVOLUTIONARY_MAINTENANCE_PFCLUST_10_STOP_AFTER_10_HOURS = new EvolutionaryMaintenanceFactory(10, 2, MUTATION_PROBABILITY, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, 5, 3);
    public static final EvolutionaryMaintenanceFactory EVOLUTIONARY_MAINTENANCE_PFCLUST_10_STOP_AFTER_8_HOURS = new EvolutionaryMaintenanceFactory(10, 2, MUTATION_PROBABILITY, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, 5, 3);
    public static final EvolutionaryMaintenanceFactory EVOLUTIONARY_MAINTENANCE_PFCLUST_10_STOP_AFTER_6_HOURS = new EvolutionaryMaintenanceFactory(10, 2, MUTATION_PROBABILITY, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, 5, 3);
    public static final EvolutionaryMaintenanceFactory EVOLUTIONARY_MAINTENANCE_PFCLUST_10_STOP_AFTER_4_HOURS = new EvolutionaryMaintenanceFactory(10, 2, MUTATION_PROBABILITY, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, 5, 3);
    public static final EvolutionaryMaintenanceFactory EVOLUTIONARY_MAINTENANCE_PFCLUST_10_STOP_AFTER_2_HOURS = new EvolutionaryMaintenanceFactory(10, 2, MUTATION_PROBABILITY, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, 5, 3);

    public static final EvolutionaryMaintenanceFactory EVOLUTIONARY_MAINTENANCE_PFCLUST_10_7_5 = new EvolutionaryMaintenanceFactory(10, 2, MUTATION_PROBABILITY, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, 7, 5);
    public static final EvolutionaryMaintenanceFactory EVOLUTIONARY_MAINTENANCE_PFCLUST_10_9_7 = new EvolutionaryMaintenanceFactory(10, 2, MUTATION_PROBABILITY, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, 9, 7);
    public static final EvolutionaryMaintenanceFactory EVOLUTIONARY_MAINTENANCE_PFCLUST_10_11_9 = new EvolutionaryMaintenanceFactory(10, 2, MUTATION_PROBABILITY, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, 11, 9);
    public static final EvolutionaryMaintenanceFactory EVOLUTIONARY_MAINTENANCE_PFCLUST_10_13_11 = new EvolutionaryMaintenanceFactory(10, 2, MUTATION_PROBABILITY, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, 13, 11);
    public static final EvolutionaryMaintenanceFactory EVOLUTIONARY_MAINTENANCE_PFCLUST_10_15_13 = new EvolutionaryMaintenanceFactory(10, 2, MUTATION_PROBABILITY, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, 15, 13);
    public static final EvolutionaryMaintenanceFactory EVOLUTIONARY_MAINTENANCE_PFCLUST_10_17_15 = new EvolutionaryMaintenanceFactory(10, 2, MUTATION_PROBABILITY, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, 17, 15);

    public static final RandomMaintenanceFactory RANDOM_MAINTENANCE_10_7_5 = new RandomMaintenanceFactory(10, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, 7, 5);
    public static final RandomMaintenanceFactory RANDOM_MAINTENANCE_10_9_7 = new RandomMaintenanceFactory(10, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, 9, 7);
    public static final RandomMaintenanceFactory RANDOM_MAINTENANCE_10_11_9 = new RandomMaintenanceFactory(10, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, 11, 9);
    public static final RandomMaintenanceFactory RANDOM_MAINTENANCE_10_13_11 = new RandomMaintenanceFactory(10, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, 13, 11);
    public static final RandomMaintenanceFactory RANDOM_MAINTENANCE_10_15_13 = new RandomMaintenanceFactory(10, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, 15, 13);
    public static final RandomMaintenanceFactory RANDOM_MAINTENANCE_10_17_15 = new RandomMaintenanceFactory(10, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, 17, 15);

    public static final RandomMaintenanceFactory RANDOM_MAINTENANCE_10_30_SEC = new RandomMaintenanceFactory(10, 30, TimeUnit.SECONDS, PF_CLUST_CLUSTERER, 5, 3);
    public static final RandomMaintenanceFactory RANDOM_MAINTENANCE_10_1_MIN = new RandomMaintenanceFactory(10, 1, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, 5, 3);
    public static final RandomMaintenanceFactory RANDOM_MAINTENANCE_10_4_MIN = new RandomMaintenanceFactory(10, 4, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, 5, 3);
    public static final RandomMaintenanceFactory RANDOM_MAINTENANCE_10_8_MIN = new RandomMaintenanceFactory(10, 8, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, 5, 3);
    public static final RandomMaintenanceFactory RANDOM_MAINTENANCE_10 = new RandomMaintenanceFactory(10, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, 5, 3);
    public static final RandomMaintenanceFactory RANDOM_MAINTENANCE_10_STOP_AFTER_48_HOURS = new RandomMaintenanceFactory(10, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, 5, 3);
    public static final RandomMaintenanceFactory RANDOM_MAINTENANCE_10_STOP_AFTER_10_HOURS = new RandomMaintenanceFactory(10, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, 5, 3);
    public static final RandomMaintenanceFactory RANDOM_MAINTENANCE_10_STOP_AFTER_8_HOURS = new RandomMaintenanceFactory(10, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, 5, 3);
    public static final RandomMaintenanceFactory RANDOM_MAINTENANCE_10_STOP_AFTER_6_HOURS = new RandomMaintenanceFactory(10, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, 5, 3);
    public static final RandomMaintenanceFactory RANDOM_MAINTENANCE_10_STOP_AFTER_4_HOURS = new RandomMaintenanceFactory(10, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, 5, 3);
    public static final RandomMaintenanceFactory RANDOM_MAINTENANCE_10_STOP_AFTER_2_HOURS = new RandomMaintenanceFactory(10, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, 5, 3);

    static {
        EVOLUTIONARY_MAINTENANCE_PFCLUST_10_STOP_AFTER_48_HOURS.setTerminationCondition(TERMINATION_CONDITION_48);
        EVOLUTIONARY_MAINTENANCE_PFCLUST_10_STOP_AFTER_10_HOURS.setTerminationCondition(TERMINATION_CONDITION_10);
        EVOLUTIONARY_MAINTENANCE_PFCLUST_10_STOP_AFTER_8_HOURS.setTerminationCondition(TERMINATION_CONDITION_8);
        EVOLUTIONARY_MAINTENANCE_PFCLUST_10_STOP_AFTER_6_HOURS.setTerminationCondition(TERMINATION_CONDITION_6);
        EVOLUTIONARY_MAINTENANCE_PFCLUST_10_STOP_AFTER_4_HOURS.setTerminationCondition(TERMINATION_CONDITION_4);
        EVOLUTIONARY_MAINTENANCE_PFCLUST_10_STOP_AFTER_2_HOURS.setTerminationCondition(TERMINATION_CONDITION_2);

        RANDOM_MAINTENANCE_10_STOP_AFTER_48_HOURS.setTerminationCondition(TERMINATION_CONDITION_48);
        RANDOM_MAINTENANCE_10_STOP_AFTER_10_HOURS.setTerminationCondition(TERMINATION_CONDITION_10);
        RANDOM_MAINTENANCE_10_STOP_AFTER_8_HOURS.setTerminationCondition(TERMINATION_CONDITION_8);
        RANDOM_MAINTENANCE_10_STOP_AFTER_6_HOURS.setTerminationCondition(TERMINATION_CONDITION_6);
        RANDOM_MAINTENANCE_10_STOP_AFTER_4_HOURS.setTerminationCondition(TERMINATION_CONDITION_4);
        RANDOM_MAINTENANCE_10_STOP_AFTER_2_HOURS.setTerminationCondition(TERMINATION_CONDITION_2);
    }

    public static final EvolutionaryMaintenanceFactory EVOLUTIONARY_MAINTENANCE_PFCLUST_20 = new EvolutionaryMaintenanceFactory(20, 2, MUTATION_PROBABILITY, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, 5, 3);
    public static final EvolutionaryMaintenanceFactory EVOLUTIONARY_MAINTENANCE_PFCLUST_30 = new EvolutionaryMaintenanceFactory(30, 2, MUTATION_PROBABILITY, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, 5, 3);
    public static final EvolutionaryMaintenanceFactory EVOLUTIONARY_MAINTENANCE_PFCLUST_40 = new EvolutionaryMaintenanceFactory(40, 2, MUTATION_PROBABILITY, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, 5, 3);
    public static final EvolutionaryMaintenanceFactory EVOLUTIONARY_MAINTENANCE_PFCLUST_50 = new EvolutionaryMaintenanceFactory(50, 2, MUTATION_PROBABILITY, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, 5, 3);
    public static final EvolutionaryMaintenanceFactory EVOLUTIONARY_MAINTENANCE_KMEAN_PLUS_PLUS_10 = new EvolutionaryMaintenanceFactory(10, 2, MUTATION_PROBABILITY, 2, TimeUnit.MINUTES, new KMeansPlusPlusClusterer<EvaluatedDisseminationStrategy>(5, 100, new EuclideanDistance(), new MersenneTwister(852)), 5, 3);
    public static final EvolutionaryMaintenanceFactory EVOLUTIONARY_MAINTENANCE_PER_POINT_CLUSTER_10 = new EvolutionaryMaintenanceFactory(10, 2, MUTATION_PROBABILITY, 2, TimeUnit.MINUTES, new PerPointClusterer<EvaluatedDisseminationStrategy>(), 5, 3);

    private static final long SEED = 56982201;
    public static final UniformSyntheticDelay BLUB_UNIFORMLY_DISTRIBUTED_SYNTHETIC_DELAY = new UniformSyntheticDelay(233763, 866279, SEED);
    public static final int ACTIVE_MAINTENANCE_INTERVAL_MILLIS = 1_500;
    public static final int LOOKUP_RETRY_COUNT = 5;

    public static final SequentialPortNumberProvider PORT_NUMBER_PROVIDER = new SequentialPortNumberProvider(64000);
    public static final Duration OBSERVATION_INTERVAL = new Duration(10, TimeUnit.SECONDS);
    public static final int KEY_LENGTH_IN_BITS = Integer.SIZE;
    private static final KeyProvider TARGET_KEY_PROVIDER = new KeyProvider(KEY_LENGTH_IN_BITS, SEED);
    //    private static final ZipfKeyProvider TARGET_KEY_PROVIDER = new ZipfKeyProvider(20_000, 1, KEY_LENGTH_IN_BITS, SEED);
    public static final KeyProvider PEER_KEY_PROVIDER = new KeyProvider(KEY_LENGTH_IN_BITS, SEED);

    public static final Duration DURATION_1_S = new Duration(1, TimeUnit.SECONDS);
    public static final Duration DURATION_10_S = new Duration(10, TimeUnit.SECONDS);
    public static final Duration DURATION_30_S = new Duration(30, TimeUnit.SECONDS);
    public static final Duration DURATION_1_MIN = new Duration(1, TimeUnit.MINUTES);
    public static final Duration DURATION_10_MIN = new Duration(10, TimeUnit.MINUTES);
    public static final Duration DURATION_15_MIN = new Duration(15, TimeUnit.MINUTES);
    public static final Duration DURATION_30_MIN = new Duration(30, TimeUnit.MINUTES);
    public static final Duration DURATION_1_HOUR = new Duration(1, TimeUnit.HOURS);
    public static final Duration DURATION_2_HOUR = new Duration(2, TimeUnit.HOURS);

    // Distributions
    public static final IntervalGenerator FIXED_EXP_1_S = new FixedExponentialInterval(DURATION_1_S, SEED);
    public static final IntervalGenerator FIXED_EXP_10_S = new FixedExponentialInterval(DURATION_10_S, SEED);
    public static final IntervalGenerator FIXED_EXP_30_S = new FixedExponentialInterval(DURATION_30_S, SEED);
    public static final IntervalGenerator FIXED_EXP_10_MIN = new FixedExponentialInterval(DURATION_10_MIN, SEED);
    public static final IntervalGenerator FIXED_EXP_15_MIN = new FixedExponentialInterval(DURATION_15_MIN, SEED);
    public static final IntervalGenerator FIXED_EXP_30_MIN = new FixedExponentialInterval(DURATION_30_MIN, SEED);
    public static final IntervalGenerator FIXED_EXP_1_HOUR = new FixedExponentialInterval(DURATION_1_HOUR, SEED);
    public static final IntervalGenerator OSCILLATING_EXP_10_S_TO_1_S = new OscillatingExponentialInterval(DURATION_10_S, DURATION_1_S, DURATION_30_MIN, SEED);
    public static final IntervalGenerator OSCILLATING_EXP_10_MIN_TO_1_MIN = new OscillatingExponentialInterval(DURATION_10_MIN, DURATION_1_MIN, DURATION_2_HOUR, SEED);
    public static final IntervalGenerator OSCILLATING_EXP_1_MIN_TO_10_MIN = new OscillatingExponentialInterval(DURATION_1_MIN, DURATION_10_MIN, DURATION_2_HOUR, SEED);

    // Churn patterns
    public static final Churn NO_CHURN = Churn.NONE;
    //    public static final Churn CHURN_1 = new Churn(FIXED_EXP_30_S, FIXED_EXP_30_S);
    //    public static final Churn CHURN_2 = new Churn(FIXED_EXP_30_S, FIXED_EXP_30_MIN);
    //    public static final Churn CHURN_3 = new Churn(FIXED_EXP_30_MIN, FIXED_EXP_30_S);
    public static final Churn CHURN_1 = new Churn(FIXED_EXP_30_MIN, FIXED_EXP_10_MIN);
    public static final Churn CHURN_2 = new Churn(FIXED_EXP_10_MIN, FIXED_EXP_30_MIN);
    public static final Churn CHURN_4 = new Churn(FIXED_EXP_30_MIN, FIXED_EXP_30_MIN);
    public static final Churn CHURN_4_15_MIN = new Churn(FIXED_EXP_15_MIN, FIXED_EXP_15_MIN);
    public static final Churn CHURN_4_1_HOUR = new Churn(FIXED_EXP_1_HOUR, FIXED_EXP_1_HOUR);
    public static final Churn CHURN_5 = new Churn(OSCILLATING_EXP_10_MIN_TO_1_MIN, OSCILLATING_EXP_10_MIN_TO_1_MIN);
    public static final Churn CHURN_6 = new Churn(OSCILLATING_EXP_10_MIN_TO_1_MIN, OSCILLATING_EXP_1_MIN_TO_10_MIN);

    // Workload patterns
    public static final Workload NO_WORKLOAD = Workload.NONE;
    public static final Workload WORKLOAD_1 = new Workload(TARGET_KEY_PROVIDER, FIXED_EXP_1_S);
    public static final Workload WORKLOAD_2 = new Workload(TARGET_KEY_PROVIDER, FIXED_EXP_10_S);
    public static final Workload WORKLOAD_3 = new Workload(TARGET_KEY_PROVIDER, OSCILLATING_EXP_10_S_TO_1_S);

    public static final PeerConfiguration NO_MAINTENANCE_CONFIGURATION = new PeerConfiguration(NO_MAINTENANCE, BLUB_UNIFORMLY_DISTRIBUTED_SYNTHETIC_DELAY);

}
