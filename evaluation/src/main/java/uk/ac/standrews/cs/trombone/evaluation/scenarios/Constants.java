package uk.ac.standrews.cs.trombone.evaluation.scenarios;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.apache.commons.math3.ml.clustering.Clusterer;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.apache.commons.math3.random.MersenneTwister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.shabdiz.util.Duration;
import uk.ac.standrews.cs.trombone.core.Key;
import uk.ac.standrews.cs.trombone.core.PeerConfiguration;
import uk.ac.standrews.cs.trombone.core.maintenance.ChordMaintenanceFactory;
import uk.ac.standrews.cs.trombone.core.maintenance.DisseminationStrategyGenerator;
import uk.ac.standrews.cs.trombone.core.maintenance.EvaluatedDisseminationStrategy;
import uk.ac.standrews.cs.trombone.core.maintenance.EvolutionaryMaintenance;
import uk.ac.standrews.cs.trombone.core.maintenance.EvolutionaryMaintenanceFactory;
import uk.ac.standrews.cs.trombone.core.maintenance.MaintenanceFactory;
import uk.ac.standrews.cs.trombone.core.maintenance.NoMaintenanceFactory;
import uk.ac.standrews.cs.trombone.core.maintenance.PerPointClusterer;
import uk.ac.standrews.cs.trombone.core.maintenance.RandomMaintenanceFactory;
import uk.ac.standrews.cs.trombone.core.maintenance.StrategicMaintenanceFactory;
import uk.ac.standrews.cs.trombone.core.maintenance.pfclust.PFClustClusterer;
import uk.ac.standrews.cs.trombone.core.state.ChordPeerStateFactory;
import uk.ac.standrews.cs.trombone.core.state.TrombonePeerStateFactory;
import uk.ac.standrews.cs.trombone.core.strategy.ChordJoinStrategy;
import uk.ac.standrews.cs.trombone.core.strategy.ChordLookupStrategy;
import uk.ac.standrews.cs.trombone.core.strategy.ChordNextHopStrategy;
import uk.ac.standrews.cs.trombone.core.strategy.TromboneNextHopStrategy;
import uk.ac.standrews.cs.trombone.core.util.Probability;
import uk.ac.standrews.cs.trombone.evaluation.util.UniformSyntheticDelay;
import uk.ac.standrews.cs.trombone.event.environment.Churn;
import uk.ac.standrews.cs.trombone.event.environment.ExponentialIntervalGenerator;
import uk.ac.standrews.cs.trombone.event.environment.IntervalGenerator;
import uk.ac.standrews.cs.trombone.event.environment.OscillatingExponentialInterval;
import uk.ac.standrews.cs.trombone.event.environment.RandomKeySupplier;
import uk.ac.standrews.cs.trombone.event.environment.Workload;
import uk.ac.standrews.cs.trombone.event.util.SequentialPortNumberSupplier;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public final class Constants {

    private static final Logger LOGGER = LoggerFactory.getLogger(Constants.class);

    private Constants() {

    }

    public static final int KEY_LENGTH_IN_BITS = Integer.SIZE;

    static {
        System.setProperty(PeerConfiguration.PEER_KEY_LENGTH_SYSTEM_PROPERTY, String.valueOf(KEY_LENGTH_IN_BITS));
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            LOGGER.warn("uncaught exception ", e);
        });
    }

    static final long SCENARIO_MASTER_SEED = 1413;
    static final Duration OBSERVATION_INTERVAL = new Duration(10, TimeUnit.SECONDS);
    static final int LOOKUP_RETRY_COUNT = 5;
    public static final int NUMBER_OF_REPETITIONS = 5;
    public static final int NETWORK_SIZE = 1_000;
    private static final long SEED = 56982201;
    public static final PFClustClusterer<EvaluatedDisseminationStrategy> PF_CLUST_CLUSTERER = new PFClustClusterer<>(SEED);

    public static final Duration EXPERIMENT_DURATION_52 = new Duration(52, TimeUnit.HOURS);
    public static final Duration EXPERIMENT_DURATION_20 = new Duration(20, TimeUnit.HOURS);
    public static final Duration EXPERIMENT_DURATION_14 = new Duration(14, TimeUnit.HOURS);
    public static final Duration EXPERIMENT_DURATION_12 = new Duration(12, TimeUnit.HOURS);
    public static final Duration EXPERIMENT_DURATION_10 = new Duration(10, TimeUnit.HOURS);
    public static final Duration EXPERIMENT_DURATION_8 = new Duration(8, TimeUnit.HOURS);
    public static final Duration EXPERIMENT_DURATION_6 = new Duration(6, TimeUnit.HOURS);
    public static final Duration EXPERIMENT_DURATION_4 = new Duration(4, TimeUnit.HOURS);

    public static final Probability MUTATION_PROBABILITY = new Probability(0.1);

    public static final EvolutionaryMaintenance.ElapsedTimeTerminationCondition TERMINATION_CONDITION_48 = new EvolutionaryMaintenance.ElapsedTimeTerminationCondition(48, TimeUnit.HOURS);
    public static final EvolutionaryMaintenance.ElapsedTimeTerminationCondition TERMINATION_CONDITION_16 = new EvolutionaryMaintenance.ElapsedTimeTerminationCondition(16, TimeUnit.HOURS);
    public static final EvolutionaryMaintenance.ElapsedTimeTerminationCondition TERMINATION_CONDITION_10 = new EvolutionaryMaintenance.ElapsedTimeTerminationCondition(10, TimeUnit.HOURS);
    public static final EvolutionaryMaintenance.ElapsedTimeTerminationCondition TERMINATION_CONDITION_8 = new EvolutionaryMaintenance.ElapsedTimeTerminationCondition(8, TimeUnit.HOURS);
    public static final EvolutionaryMaintenance.ElapsedTimeTerminationCondition TERMINATION_CONDITION_6 = new EvolutionaryMaintenance.ElapsedTimeTerminationCondition(6, TimeUnit.HOURS);
    public static final EvolutionaryMaintenance.ElapsedTimeTerminationCondition TERMINATION_CONDITION_4 = new EvolutionaryMaintenance.ElapsedTimeTerminationCondition(4, TimeUnit.HOURS);
    public static final EvolutionaryMaintenance.ElapsedTimeTerminationCondition TERMINATION_CONDITION_2 = new EvolutionaryMaintenance.ElapsedTimeTerminationCondition(30, TimeUnit.SECONDS);

    public static final UniformSyntheticDelay BLUB_UNIFORMLY_DISTRIBUTED_SYNTHETIC_DELAY = new UniformSyntheticDelay(233763, 866279);
    public static final int ACTIVE_MAINTENANCE_INTERVAL_MILLIS = 1_500;

    public static final SequentialPortNumberSupplier PORT_NUMBER_PROVIDER = new SequentialPortNumberSupplier(64000);

    public static final RandomKeySupplier TARGET_KEY_PROVIDER = new RandomKeySupplier(SEED);
    public static final Supplier<RandomKeySupplier> PEER_KEY_PROVIDER = () -> new RandomKeySupplier(SEED);

    public static final Duration DURATION_1_SEC = new Duration(1, TimeUnit.SECONDS);
    public static final Duration DURATION_10_SEC = new Duration(10, TimeUnit.SECONDS);
    public static final Duration DURATION_30_SEC = new Duration(30, TimeUnit.SECONDS);
    public static final Duration DURATION_1_MIN = new Duration(1, TimeUnit.MINUTES);
    public static final Duration DURATION_10_MIN = new Duration(10, TimeUnit.MINUTES);
    public static final Duration DURATION_15_MIN = new Duration(15, TimeUnit.MINUTES);
    public static final Duration DURATION_30_MIN = new Duration(30, TimeUnit.MINUTES);
    public static final Duration DURATION_1_HOUR = new Duration(1, TimeUnit.HOURS);
    public static final Duration DURATION_2_HOUR = new Duration(2, TimeUnit.HOURS);

    // Distributions
    public static final IntervalGenerator FIXED_EXP_1_S = new ExponentialIntervalGenerator(DURATION_1_SEC, SEED);
    public static final IntervalGenerator FIXED_EXP_10_S = new ExponentialIntervalGenerator(DURATION_10_SEC, SEED);
    public static final IntervalGenerator FIXED_EXP_30_S = new ExponentialIntervalGenerator(DURATION_30_SEC, SEED);
    public static final IntervalGenerator FIXED_EXP_10_MIN = new ExponentialIntervalGenerator(DURATION_10_MIN, SEED);
    public static final IntervalGenerator FIXED_EXP_15_MIN = new ExponentialIntervalGenerator(DURATION_15_MIN, SEED);
    public static final IntervalGenerator FIXED_EXP_30_MIN = new ExponentialIntervalGenerator(DURATION_30_MIN, SEED);
    public static final IntervalGenerator FIXED_EXP_1_HOUR = new ExponentialIntervalGenerator(DURATION_1_HOUR, SEED);
    public static final IntervalGenerator OSCILLATING_EXP_10_S_TO_1_S = new OscillatingExponentialInterval(DURATION_10_SEC, DURATION_1_SEC, DURATION_30_MIN, SEED);
    public static final IntervalGenerator OSCILLATING_EXP_10_MIN_TO_1_SEC = new OscillatingExponentialInterval(DURATION_10_MIN, DURATION_1_SEC, DURATION_2_HOUR, SEED);
    public static final IntervalGenerator OSCILLATING_EXP_10_MIN_TO_1_MIN = new OscillatingExponentialInterval(DURATION_10_MIN, DURATION_1_MIN, DURATION_2_HOUR, SEED);
    public static final IntervalGenerator OSCILLATING_EXP_1_MIN_TO_10_MIN = new OscillatingExponentialInterval(DURATION_1_MIN, DURATION_10_MIN, DURATION_2_HOUR, SEED);

    // Churn
    public static final Churn NO_CHURN = Churn.NONE;
    public static final Churn CHURN_15_MIN = new Churn(FIXED_EXP_15_MIN, FIXED_EXP_15_MIN);
    public static final Churn CHURN_30_MIN = new Churn(FIXED_EXP_30_MIN, FIXED_EXP_30_MIN);
    public static final Churn CHURN_1_HOUR = new Churn(FIXED_EXP_1_HOUR, FIXED_EXP_1_HOUR);
    public static final Churn CHURN_OSCILLATING = new Churn(OSCILLATING_EXP_10_MIN_TO_1_MIN, OSCILLATING_EXP_10_MIN_TO_1_MIN);
    static final Churn[] ALL_CHURN_MODELS = {
            NO_CHURN, CHURN_15_MIN, CHURN_30_MIN, CHURN_1_HOUR, CHURN_OSCILLATING
    };

    // Workload
    public static final Workload NO_WORKLOAD = Workload.NONE;
    public static final Workload WORKLOAD_1_SEC = new Workload(TARGET_KEY_PROVIDER, FIXED_EXP_1_S);
    public static final Workload WORKLOAD_10_SEC = new Workload(TARGET_KEY_PROVIDER, FIXED_EXP_10_S);
    public static final Workload WORKLOAD_10_MIN = new Workload(TARGET_KEY_PROVIDER, FIXED_EXP_10_MIN);
    public static final Workload WORKLOAD_OSCILLATING = new Workload(TARGET_KEY_PROVIDER, OSCILLATING_EXP_10_MIN_TO_1_SEC);
    static final Workload[] WORKLOADS = {
            NO_WORKLOAD, WORKLOAD_1_SEC, WORKLOAD_10_SEC, WORKLOAD_10_MIN, WORKLOAD_OSCILLATING
    };

    public static final PeerConfiguration NO_MAINTENANCE_CONFIGURATION = null;// new PeerConfiguration(NO_MAINTENANCE, BLUB_UNIFORMLY_DISTRIBUTED_SYNTHETIC_DELAY);

    public static final MaintenanceFactory NO_MAINTENANCE = NoMaintenanceFactory.getInstance();
    public static final ScheduledExecutorService SCHEDULED_EXECUTOR_SERVICE = new ScheduledThreadPoolExecutor(800);

    private static final PeerConfiguration.Builder BASE_BUILDER = PeerConfiguration.builder()
            .enableApplicationFeedback(false)
            .learnFromCommunications(false)
            .syntheticDelay(BLUB_UNIFORMLY_DISTRIBUTED_SYNTHETIC_DELAY)
            .executor(() -> SCHEDULED_EXECUTOR_SERVICE);

    private static final int FINGER_TABLE_SIZE = 10;
    private static final int SUCCESSOR_LIST_SIZE = 8;
    private static final int MAINTENANCE_INTERVAL = 3;
    private static final TimeUnit MAINTENANCE_INTERVAL_UNIT = TimeUnit.SECONDS;
    private static final BigInteger INTER_FINGER_RATIO = Key.TWO;

    public static final PeerConfiguration CHORD = PeerConfiguration.builder(BASE_BUILDER)
            .peerState(new ChordPeerStateFactory(FINGER_TABLE_SIZE, INTER_FINGER_RATIO, SUCCESSOR_LIST_SIZE))
            .joinStrategy(new ChordJoinStrategy())
            .nextHopStrategy(new ChordNextHopStrategy())
            .lookupStrategy(new ChordLookupStrategy())
            .maintenance(new ChordMaintenanceFactory(MAINTENANCE_INTERVAL, MAINTENANCE_INTERVAL_UNIT))
            .build();

    private static final PeerConfiguration.Builder BASE_TROMBONE_CONFIGURATION = PeerConfiguration.builder(BASE_BUILDER)
            .learnFromCommunications(true)
            .peerState(new TrombonePeerStateFactory())
            .joinStrategy(new ChordJoinStrategy())
            .nextHopStrategy(new TromboneNextHopStrategy())
            .lookupStrategy(new ChordLookupStrategy());

    private static final MaintenanceFactory PERIODIC_STABILIZATION = new StrategicMaintenanceFactory(new StrongStabilization(), MAINTENANCE_INTERVAL, MAINTENANCE_INTERVAL_UNIT);
    private static final Clusterer<EvaluatedDisseminationStrategy> PFCLUST_CLUSTERER = new PFClustClusterer<>(4121);
    private static final Clusterer<EvaluatedDisseminationStrategy> K_MEANS_PLUS_PLUS_CLUSTERER = new KMeansPlusPlusClusterer<>(5, 100, new EuclideanDistance(), new MersenneTwister(852));
    private static final Clusterer<EvaluatedDisseminationStrategy> PER_POINT_CLUSTERER = new PerPointClusterer<>();
    private static final DisseminationStrategyGenerator STRATEGY_GENERATOR = new DisseminationStrategyGenerator(15, 5);
    private static final int POPULATION_SIZE = 10;

    private static final EvolutionaryMaintenanceFactory.Builder BASE_EVOLUTIONARY_MAINTENANCE_FACTORY_BUILDER = EvolutionaryMaintenanceFactory.builder()
            .populationSize(POPULATION_SIZE)
            .eliteCount(1)
            .periodicMaintenanceInterval(MAINTENANCE_INTERVAL, MAINTENANCE_INTERVAL_UNIT)
            .mutationProbability(MUTATION_PROBABILITY)
            .evaluationDuration(1, TimeUnit.MINUTES)
            .clusterer(PFCLUST_CLUSTERER)
            .disseminationStrategyGenerator(STRATEGY_GENERATOR);

    private static final MaintenanceFactory EVOLUTIONARY_MAINTENANCE = EvolutionaryMaintenanceFactory.builder(BASE_EVOLUTIONARY_MAINTENANCE_FACTORY_BUILDER)
            .build();

    public static final MaintenanceFactory EVOLUTIONARY_MAINTENANCE_STOP_AFTER_2 = EvolutionaryMaintenanceFactory.builder(BASE_EVOLUTIONARY_MAINTENANCE_FACTORY_BUILDER)
            .terminationCondition(TERMINATION_CONDITION_2)
            .build();
    public static final MaintenanceFactory EVOLUTIONARY_MAINTENANCE_STOP_AFTER_4 = EvolutionaryMaintenanceFactory.builder(BASE_EVOLUTIONARY_MAINTENANCE_FACTORY_BUILDER)
            .terminationCondition(TERMINATION_CONDITION_4)
            .build();
    public static final MaintenanceFactory EVOLUTIONARY_MAINTENANCE_STOP_AFTER_8 = EvolutionaryMaintenanceFactory.builder(BASE_EVOLUTIONARY_MAINTENANCE_FACTORY_BUILDER)
            .terminationCondition(TERMINATION_CONDITION_8)
            .build();
    public static final MaintenanceFactory EVOLUTIONARY_MAINTENANCE_STOP_AFTER_16 = EvolutionaryMaintenanceFactory.builder(BASE_EVOLUTIONARY_MAINTENANCE_FACTORY_BUILDER)
            .terminationCondition(TERMINATION_CONDITION_16)
            .build();

    public static final EvolutionaryMaintenanceFactory.Builder EVOLUTIONARY_KMEAN_CLUSTERER = EvolutionaryMaintenanceFactory.builder(BASE_EVOLUTIONARY_MAINTENANCE_FACTORY_BUILDER)
            .clusterer(K_MEANS_PLUS_PLUS_CLUSTERER);
    public static final MaintenanceFactory EVOLUTIONARY_MAINTENANCE_KMEAN = EvolutionaryMaintenanceFactory.builder(EVOLUTIONARY_KMEAN_CLUSTERER)
            .build();
    private static final MaintenanceFactory EVOLUTIONARY_MAINTENANCE_KMEAN_STOP_AFTER_2 = EvolutionaryMaintenanceFactory.builder(EVOLUTIONARY_KMEAN_CLUSTERER)
            .terminationCondition(TERMINATION_CONDITION_2)
            .build();
    private static final MaintenanceFactory EVOLUTIONARY_MAINTENANCE_KMEAN_STOP_AFTER_4 = EvolutionaryMaintenanceFactory.builder(EVOLUTIONARY_KMEAN_CLUSTERER)
            .terminationCondition(TERMINATION_CONDITION_4)
            .build();
    private static final MaintenanceFactory EVOLUTIONARY_MAINTENANCE_KMEAN_STOP_AFTER_8 = EvolutionaryMaintenanceFactory.builder(EVOLUTIONARY_KMEAN_CLUSTERER)
            .terminationCondition(TERMINATION_CONDITION_8)
            .build();
    private static final MaintenanceFactory EVOLUTIONARY_MAINTENANCE_KMEAN_STOP_AFTER_16 = EvolutionaryMaintenanceFactory.builder(EVOLUTIONARY_KMEAN_CLUSTERER)
            .terminationCondition(TERMINATION_CONDITION_16)
            .build();

    public static final EvolutionaryMaintenanceFactory.Builder EVOLUTIONARY_PER_POINT_CLUSTERER = EvolutionaryMaintenanceFactory.builder(BASE_EVOLUTIONARY_MAINTENANCE_FACTORY_BUILDER)
            .clusterer(PER_POINT_CLUSTERER);

    private static final MaintenanceFactory EVOLUTIONARY_MAINTENANCE_PER_POINT = EvolutionaryMaintenanceFactory.builder(EVOLUTIONARY_PER_POINT_CLUSTERER)
            .build();
    private static final MaintenanceFactory EVOLUTIONARY_MAINTENANCE_PER_POINT_STOP_AFTER_2 = EvolutionaryMaintenanceFactory.builder(EVOLUTIONARY_PER_POINT_CLUSTERER)
            .terminationCondition(TERMINATION_CONDITION_2)
            .build();
    private static final MaintenanceFactory EVOLUTIONARY_MAINTENANCE_PER_POINT_STOP_AFTER_4 = EvolutionaryMaintenanceFactory.builder(EVOLUTIONARY_PER_POINT_CLUSTERER)
            .terminationCondition(TERMINATION_CONDITION_4)
            .build();
    private static final MaintenanceFactory EVOLUTIONARY_MAINTENANCE_PER_POINT_STOP_AFTER_8 = EvolutionaryMaintenanceFactory.builder(EVOLUTIONARY_PER_POINT_CLUSTERER)
            .terminationCondition(TERMINATION_CONDITION_8)
            .build();
    private static final MaintenanceFactory EVOLUTIONARY_MAINTENANCE_PER_POINT_STOP_AFTER_16 = EvolutionaryMaintenanceFactory.builder(EVOLUTIONARY_PER_POINT_CLUSTERER)
            .terminationCondition(TERMINATION_CONDITION_16)
            .build();

    private static final RandomMaintenanceFactory.Builder BASE_RANDOM_MAINTENANCE_FACTORY_BUILDER = RandomMaintenanceFactory.builder()
            .populationSize(POPULATION_SIZE)
            .periodicMaintenanceInterval(MAINTENANCE_INTERVAL, MAINTENANCE_INTERVAL_UNIT)
            .evaluationDuration(2, TimeUnit.MINUTES)
            .clusterer(PFCLUST_CLUSTERER)
            .disseminationStrategyGenerator(STRATEGY_GENERATOR);

    private static final MaintenanceFactory RANDOM_MAINTENANCE = RandomMaintenanceFactory.builder(BASE_RANDOM_MAINTENANCE_FACTORY_BUILDER)
            .build();
    public static final MaintenanceFactory RANDOM_MAINTENANCE_STOP_AFTER_2 = RandomMaintenanceFactory.builder(BASE_RANDOM_MAINTENANCE_FACTORY_BUILDER)
            .terminationCondition(TERMINATION_CONDITION_2)
            .build();
    public static final MaintenanceFactory RANDOM_MAINTENANCE_STOP_AFTER_4 = RandomMaintenanceFactory.builder(BASE_RANDOM_MAINTENANCE_FACTORY_BUILDER)
            .terminationCondition(TERMINATION_CONDITION_4)
            .build();
    public static final MaintenanceFactory RANDOM_MAINTENANCE_STOP_AFTER_8 = RandomMaintenanceFactory.builder(BASE_RANDOM_MAINTENANCE_FACTORY_BUILDER)
            .terminationCondition(TERMINATION_CONDITION_8)
            .build();
    public static final MaintenanceFactory RANDOM_MAINTENANCE_STOP_AFTER_16 = RandomMaintenanceFactory.builder(BASE_RANDOM_MAINTENANCE_FACTORY_BUILDER)
            .terminationCondition(TERMINATION_CONDITION_16)
            .build();

    private static final MaintenanceFactory RANDOM_MAINTENANCE_KMEAN = RandomMaintenanceFactory.builder(BASE_RANDOM_MAINTENANCE_FACTORY_BUILDER)
            .clusterer(K_MEANS_PLUS_PLUS_CLUSTERER)
            .build();
    private static final MaintenanceFactory RANDOM_MAINTENANCE_KMEAN_STOP_AFTER_2 = RandomMaintenanceFactory.builder(BASE_RANDOM_MAINTENANCE_FACTORY_BUILDER)
            .clusterer(K_MEANS_PLUS_PLUS_CLUSTERER)
            .terminationCondition(TERMINATION_CONDITION_2)
            .build();
    private static final MaintenanceFactory RANDOM_MAINTENANCE_KMEAN_STOP_AFTER_4 = RandomMaintenanceFactory.builder(BASE_RANDOM_MAINTENANCE_FACTORY_BUILDER)
            .clusterer(K_MEANS_PLUS_PLUS_CLUSTERER)
            .terminationCondition(TERMINATION_CONDITION_4)
            .build();
    private static final MaintenanceFactory RANDOM_MAINTENANCE_KMEAN_STOP_AFTER_8 = RandomMaintenanceFactory.builder(BASE_RANDOM_MAINTENANCE_FACTORY_BUILDER)
            .clusterer(K_MEANS_PLUS_PLUS_CLUSTERER)
            .terminationCondition(TERMINATION_CONDITION_8)
            .build();
    private static final MaintenanceFactory RANDOM_MAINTENANCE_KMEAN_STOP_AFTER_16 = RandomMaintenanceFactory.builder(BASE_RANDOM_MAINTENANCE_FACTORY_BUILDER)
            .clusterer(K_MEANS_PLUS_PLUS_CLUSTERER)
            .terminationCondition(TERMINATION_CONDITION_16)
            .build();

    private static final MaintenanceFactory RANDOM_MAINTENANCE_PER_POINT = RandomMaintenanceFactory.builder(BASE_RANDOM_MAINTENANCE_FACTORY_BUILDER)
            .clusterer(PER_POINT_CLUSTERER)
            .build();
    private static final MaintenanceFactory RANDOM_MAINTENANCE_PER_POINT_STOP_AFTER_2 = RandomMaintenanceFactory.builder(BASE_RANDOM_MAINTENANCE_FACTORY_BUILDER)
            .clusterer(PER_POINT_CLUSTERER)
            .terminationCondition(TERMINATION_CONDITION_2)
            .build();
    private static final MaintenanceFactory RANDOM_MAINTENANCE_PER_POINT_STOP_AFTER_4 = RandomMaintenanceFactory.builder(BASE_RANDOM_MAINTENANCE_FACTORY_BUILDER)
            .clusterer(PER_POINT_CLUSTERER)
            .terminationCondition(TERMINATION_CONDITION_4)
            .build();
    private static final MaintenanceFactory RANDOM_MAINTENANCE_PER_POINT_STOP_AFTER_8 = RandomMaintenanceFactory.builder(BASE_RANDOM_MAINTENANCE_FACTORY_BUILDER)
            .clusterer(PER_POINT_CLUSTERER)
            .terminationCondition(TERMINATION_CONDITION_8)
            .build();
    private static final MaintenanceFactory RANDOM_MAINTENANCE_PER_POINT_STOP_AFTER_16 = RandomMaintenanceFactory.builder(BASE_RANDOM_MAINTENANCE_FACTORY_BUILDER)
            .clusterer(PER_POINT_CLUSTERER)
            .terminationCondition(TERMINATION_CONDITION_16)
            .build();

    public static final PeerConfiguration TROMBONE_NO_MAINTENANCE = PeerConfiguration.builder(BASE_TROMBONE_CONFIGURATION)
            .maintenance(NO_MAINTENANCE)
            .build();

    public static final PeerConfiguration TROMBONE_STABILISATION = PeerConfiguration.builder(BASE_TROMBONE_CONFIGURATION)
            .maintenance(PERIODIC_STABILIZATION)
            .build();
    public static final PeerConfiguration.Builder TROMBONE_ADAPTIVE_GA_BUILDER = PeerConfiguration.builder(BASE_TROMBONE_CONFIGURATION)
            .maintenance(EVOLUTIONARY_MAINTENANCE);
    public static final PeerConfiguration TROMBONE_ADAPTIVE_GA = TROMBONE_ADAPTIVE_GA_BUILDER
            .build();
    public static final PeerConfiguration TROMBONE_ADAPTIVE_GA_2 = PeerConfiguration.builder(BASE_TROMBONE_CONFIGURATION)
            .maintenance(EVOLUTIONARY_MAINTENANCE_STOP_AFTER_2)
            .build();
    public static final PeerConfiguration TROMBONE_ADAPTIVE_GA_4 = PeerConfiguration.builder(BASE_TROMBONE_CONFIGURATION)
            .maintenance(EVOLUTIONARY_MAINTENANCE_STOP_AFTER_4)
            .build();
    public static final PeerConfiguration TROMBONE_ADAPTIVE_GA_8 = PeerConfiguration.builder(BASE_TROMBONE_CONFIGURATION)
            .maintenance(EVOLUTIONARY_MAINTENANCE_STOP_AFTER_8)
            .build();
    public static final PeerConfiguration TROMBONE_ADAPTIVE_GA_16 = PeerConfiguration.builder(BASE_TROMBONE_CONFIGURATION)
            .maintenance(EVOLUTIONARY_MAINTENANCE_STOP_AFTER_16)
            .build();

    public static final PeerConfiguration TROMBONE_ADAPTIVE_GA_KMEAN = PeerConfiguration.builder(BASE_TROMBONE_CONFIGURATION)
            .maintenance(EVOLUTIONARY_MAINTENANCE_KMEAN)
            .build();
    public static final PeerConfiguration TROMBONE_ADAPTIVE_GA_KMEAN_2 = PeerConfiguration.builder(BASE_TROMBONE_CONFIGURATION)
            .maintenance(EVOLUTIONARY_MAINTENANCE_KMEAN_STOP_AFTER_2)
            .build();
    public static final PeerConfiguration TROMBONE_ADAPTIVE_GA_KMEAN_4 = PeerConfiguration.builder(BASE_TROMBONE_CONFIGURATION)
            .maintenance(EVOLUTIONARY_MAINTENANCE_KMEAN_STOP_AFTER_4)
            .build();
    public static final PeerConfiguration TROMBONE_ADAPTIVE_GA_KMEAN_8 = PeerConfiguration.builder(BASE_TROMBONE_CONFIGURATION)
            .maintenance(EVOLUTIONARY_MAINTENANCE_KMEAN_STOP_AFTER_8)
            .build();
    public static final PeerConfiguration TROMBONE_ADAPTIVE_GA_KMEAN_16 = PeerConfiguration.builder(BASE_TROMBONE_CONFIGURATION)
            .maintenance(EVOLUTIONARY_MAINTENANCE_KMEAN_STOP_AFTER_16)
            .build();
    public static final PeerConfiguration TROMBONE_ADAPTIVE_GA_PER_POINT = PeerConfiguration.builder(BASE_TROMBONE_CONFIGURATION)
            .maintenance(EVOLUTIONARY_MAINTENANCE_PER_POINT)
            .build();
    public static final PeerConfiguration TROMBONE_ADAPTIVE_GA_PER_POINT_2 = PeerConfiguration.builder(BASE_TROMBONE_CONFIGURATION)
            .maintenance(EVOLUTIONARY_MAINTENANCE_PER_POINT_STOP_AFTER_2)
            .build();
    public static final PeerConfiguration TROMBONE_ADAPTIVE_GA_PER_POINT_4 = PeerConfiguration.builder(BASE_TROMBONE_CONFIGURATION)
            .maintenance(EVOLUTIONARY_MAINTENANCE_PER_POINT_STOP_AFTER_4)
            .build();
    public static final PeerConfiguration TROMBONE_ADAPTIVE_GA_PER_POINT_8 = PeerConfiguration.builder(BASE_TROMBONE_CONFIGURATION)
            .maintenance(EVOLUTIONARY_MAINTENANCE_PER_POINT_STOP_AFTER_8)
            .build();
    public static final PeerConfiguration TROMBONE_ADAPTIVE_GA_PER_POINT_16 = PeerConfiguration.builder(BASE_TROMBONE_CONFIGURATION)
            .maintenance(EVOLUTIONARY_MAINTENANCE_PER_POINT_STOP_AFTER_16)
            .build();

    public static final PeerConfiguration.Builder TROMBONE_ADAPTIVE_RANDOM_BUILDER = PeerConfiguration.builder(BASE_TROMBONE_CONFIGURATION)
            .maintenance(RANDOM_MAINTENANCE);
    public static final PeerConfiguration TROMBONE_ADAPTIVE_RANDOM = TROMBONE_ADAPTIVE_RANDOM_BUILDER
            .build();
    public static final PeerConfiguration TROMBONE_ADAPTIVE_RANDOM_2 = PeerConfiguration.builder(BASE_TROMBONE_CONFIGURATION)
            .maintenance(RANDOM_MAINTENANCE_STOP_AFTER_2)
            .build();
    public static final PeerConfiguration TROMBONE_ADAPTIVE_RANDOM_4 = PeerConfiguration.builder(BASE_TROMBONE_CONFIGURATION)
            .maintenance(RANDOM_MAINTENANCE_STOP_AFTER_4)
            .build();
    public static final PeerConfiguration TROMBONE_ADAPTIVE_RANDOM_8 = PeerConfiguration.builder(BASE_TROMBONE_CONFIGURATION)
            .maintenance(RANDOM_MAINTENANCE_STOP_AFTER_8)
            .build();
    public static final PeerConfiguration TROMBONE_ADAPTIVE_RANDOM_16 = PeerConfiguration.builder(BASE_TROMBONE_CONFIGURATION)
            .maintenance(RANDOM_MAINTENANCE_STOP_AFTER_16)
            .build();

    public static final PeerConfiguration TROMBONE_ADAPTIVE_RANDOM_KMEAN = PeerConfiguration.builder(BASE_TROMBONE_CONFIGURATION)
            .maintenance(RANDOM_MAINTENANCE_KMEAN)
            .build();
    public static final PeerConfiguration TROMBONE_ADAPTIVE_RANDOM_KMEAN_2 = PeerConfiguration.builder(BASE_TROMBONE_CONFIGURATION)
            .maintenance(RANDOM_MAINTENANCE_KMEAN_STOP_AFTER_2)
            .build();
    public static final PeerConfiguration TROMBONE_ADAPTIVE_RANDOM_KMEAN_4 = PeerConfiguration.builder(BASE_TROMBONE_CONFIGURATION)
            .maintenance(RANDOM_MAINTENANCE_KMEAN_STOP_AFTER_4)
            .build();
    public static final PeerConfiguration TROMBONE_ADAPTIVE_RANDOM_KMEAN_8 = PeerConfiguration.builder(BASE_TROMBONE_CONFIGURATION)
            .maintenance(RANDOM_MAINTENANCE_KMEAN_STOP_AFTER_8)
            .build();
    public static final PeerConfiguration TROMBONE_ADAPTIVE_RANDOM_KMEAN_16 = PeerConfiguration.builder(BASE_TROMBONE_CONFIGURATION)
            .maintenance(RANDOM_MAINTENANCE_KMEAN_STOP_AFTER_16)
            .build();

    public static final PeerConfiguration TROMBONE_ADAPTIVE_RANDOM_PER_POINT = PeerConfiguration.builder(BASE_TROMBONE_CONFIGURATION)
            .maintenance(RANDOM_MAINTENANCE_PER_POINT)
            .build();
    public static final PeerConfiguration TROMBONE_ADAPTIVE_RANDOM_PER_POINT_2 = PeerConfiguration.builder(BASE_TROMBONE_CONFIGURATION)
            .maintenance(RANDOM_MAINTENANCE_PER_POINT_STOP_AFTER_2)
            .build();
    public static final PeerConfiguration TROMBONE_ADAPTIVE_RANDOM_PER_POINT_4 = PeerConfiguration.builder(BASE_TROMBONE_CONFIGURATION)
            .maintenance(RANDOM_MAINTENANCE_PER_POINT_STOP_AFTER_4)
            .build();
    public static final PeerConfiguration TROMBONE_ADAPTIVE_RANDOM_PER_POINT_8 = PeerConfiguration.builder(BASE_TROMBONE_CONFIGURATION)
            .maintenance(RANDOM_MAINTENANCE_PER_POINT_STOP_AFTER_8)
            .build();
    public static final PeerConfiguration TROMBONE_ADAPTIVE_RANDOM_PER_POINT_16 = PeerConfiguration.builder(BASE_TROMBONE_CONFIGURATION)
            .maintenance(RANDOM_MAINTENANCE_PER_POINT_STOP_AFTER_16)
            .build();

    static final PeerConfiguration[] PEER_CONFIGURATIONS = {
            CHORD, TROMBONE_NO_MAINTENANCE, TROMBONE_STABILISATION, TROMBONE_ADAPTIVE_GA, TROMBONE_ADAPTIVE_RANDOM
    };

    public static final List<PeerConfiguration> ALL = new CopyOnWriteArrayList<>(PEER_CONFIGURATIONS);

}
