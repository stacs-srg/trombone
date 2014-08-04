package uk.ac.standrews.cs.trombone.evaluation.scenarios;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.apache.commons.math3.random.MersenneTwister;
import uk.ac.standrews.cs.shabdiz.util.Duration;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerConfiguration;
import uk.ac.standrews.cs.trombone.core.key.Key;
import uk.ac.standrews.cs.trombone.core.key.KeySupplier;
import uk.ac.standrews.cs.trombone.core.maintenance.ChordMaintenance;
import uk.ac.standrews.cs.trombone.core.maintenance.DisseminationStrategyGenerator;
import uk.ac.standrews.cs.trombone.core.maintenance.EvaluatedDisseminationStrategy;
import uk.ac.standrews.cs.trombone.core.maintenance.EvolutionaryMaintenance;
import uk.ac.standrews.cs.trombone.core.maintenance.Maintenance;
import uk.ac.standrews.cs.trombone.core.maintenance.PerPointClusterer;
import uk.ac.standrews.cs.trombone.core.maintenance.RandomMaintenance;
import uk.ac.standrews.cs.trombone.core.maintenance.StrategicMaintenance;
import uk.ac.standrews.cs.trombone.core.maintenance.pfclust.PFClustClusterer;
import uk.ac.standrews.cs.trombone.core.state.ChordPeerState;
import uk.ac.standrews.cs.trombone.core.state.TrombonePeerState;
import uk.ac.standrews.cs.trombone.core.strategy.ChordJoinStrategy;
import uk.ac.standrews.cs.trombone.core.strategy.ChordLookupStrategy;
import uk.ac.standrews.cs.trombone.core.strategy.ChordNextHopStrategy;
import uk.ac.standrews.cs.trombone.core.strategy.MinimalJoinStrategy;
import uk.ac.standrews.cs.trombone.core.strategy.TromboneNextHopStrategy;
import uk.ac.standrews.cs.trombone.core.util.Probability;
import uk.ac.standrews.cs.trombone.evaluation.util.UniformSyntheticDelay;
import uk.ac.standrews.cs.trombone.event.environment.Churn;
import uk.ac.standrews.cs.trombone.event.environment.ExponentialIntervalGenerator;
import uk.ac.standrews.cs.trombone.event.environment.IntervalGenerator;
import uk.ac.standrews.cs.trombone.event.environment.OscillatingExponentialInterval;
import uk.ac.standrews.cs.trombone.event.environment.Workload;
import uk.ac.standrews.cs.trombone.event.util.SequentialPortNumberSupplier;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public final class Constants {

    private Constants() {

    }

    public static final int KEY_LENGTH_IN_BITS = Integer.SIZE;

    static {
        System.setProperty(PeerConfiguration.PEER_KEY_LENGTH_SYSTEM_PROPERTY, String.valueOf(KEY_LENGTH_IN_BITS));
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
    public static final Function<Peer, StrategicMaintenance> SUCCESSOR_MAINTENANCE = peer -> new StrategicMaintenance(peer, new StrongStabilization(), 2, TimeUnit.SECONDS);
    public static final Function<Peer, StrategicMaintenance> SUCCESSOR_LIST_MAINTENANCE_5 = peer -> new StrategicMaintenance(peer, new PeriodicSuccessorListPull(5), 2, TimeUnit.SECONDS);

    public static final EvolutionaryMaintenance.ElapsedTimeTerminationCondition TERMINATION_CONDITION_48 = new EvolutionaryMaintenance.ElapsedTimeTerminationCondition(48, TimeUnit.HOURS);
    public static final EvolutionaryMaintenance.ElapsedTimeTerminationCondition TERMINATION_CONDITION_16 = new EvolutionaryMaintenance.ElapsedTimeTerminationCondition(16, TimeUnit.HOURS);
    public static final EvolutionaryMaintenance.ElapsedTimeTerminationCondition TERMINATION_CONDITION_10 = new EvolutionaryMaintenance.ElapsedTimeTerminationCondition(10, TimeUnit.HOURS);
    public static final EvolutionaryMaintenance.ElapsedTimeTerminationCondition TERMINATION_CONDITION_8 = new EvolutionaryMaintenance.ElapsedTimeTerminationCondition(8, TimeUnit.HOURS);
    public static final EvolutionaryMaintenance.ElapsedTimeTerminationCondition TERMINATION_CONDITION_6 = new EvolutionaryMaintenance.ElapsedTimeTerminationCondition(6, TimeUnit.HOURS);
    public static final EvolutionaryMaintenance.ElapsedTimeTerminationCondition TERMINATION_CONDITION_4 = new EvolutionaryMaintenance.ElapsedTimeTerminationCondition(4, TimeUnit.HOURS);
    public static final EvolutionaryMaintenance.ElapsedTimeTerminationCondition TERMINATION_CONDITION_2 = new EvolutionaryMaintenance.ElapsedTimeTerminationCondition(2, TimeUnit.HOURS);

    public static final Function<Peer, EvolutionaryMaintenance> EVOLUTIONARY_MAINTENANCE_PFCLUST_10_30_SEC = peer -> new EvolutionaryMaintenance(peer, 10, 2, MUTATION_PROBABILITY, 30, TimeUnit.SECONDS, PF_CLUST_CLUSTERER, new DisseminationStrategyGenerator(5, 3));
    public static final Function<Peer, EvolutionaryMaintenance> EVOLUTIONARY_MAINTENANCE_PFCLUST_10_1_MIN = peer -> new EvolutionaryMaintenance(peer, 10, 2, MUTATION_PROBABILITY, 1, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, new DisseminationStrategyGenerator(5, 3));
    public static final Function<Peer, EvolutionaryMaintenance> EVOLUTIONARY_MAINTENANCE_PFCLUST_10_4_MIN = peer -> new EvolutionaryMaintenance(peer, 10, 2, MUTATION_PROBABILITY, 4, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, new DisseminationStrategyGenerator(5, 3));
    public static final Function<Peer, EvolutionaryMaintenance> EVOLUTIONARY_MAINTENANCE_PFCLUST_10_8_MIN = peer -> new EvolutionaryMaintenance(peer, 10, 2, MUTATION_PROBABILITY, 8, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, new DisseminationStrategyGenerator(5, 3));
    public static final Function<Peer, EvolutionaryMaintenance> EVOLUTIONARY_MAINTENANCE_PFCLUST_10 = peer -> new EvolutionaryMaintenance(peer, 10, 2, MUTATION_PROBABILITY, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, new DisseminationStrategyGenerator(5, 3));
    public static final Function<Peer, EvolutionaryMaintenance> EVOLUTIONARY_MAINTENANCE_PFCLUST_10_STOP_AFTER_48_HOURS = peer -> new EvolutionaryMaintenance(peer, 10, 2, MUTATION_PROBABILITY, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, new DisseminationStrategyGenerator(5, 3), TERMINATION_CONDITION_48);
    public static final Function<Peer, EvolutionaryMaintenance> EVOLUTIONARY_MAINTENANCE_PFCLUST_10_STOP_AFTER_10_HOURS = peer -> new EvolutionaryMaintenance(peer, 10, 2, MUTATION_PROBABILITY, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, new DisseminationStrategyGenerator(5, 3), TERMINATION_CONDITION_10);
    public static final Function<Peer, EvolutionaryMaintenance> EVOLUTIONARY_MAINTENANCE_PFCLUST_10_STOP_AFTER_8_HOURS = peer -> new EvolutionaryMaintenance(peer, 10, 2, MUTATION_PROBABILITY, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, new DisseminationStrategyGenerator(5, 3), TERMINATION_CONDITION_8);
    public static final Function<Peer, EvolutionaryMaintenance> EVOLUTIONARY_MAINTENANCE_PFCLUST_10_STOP_AFTER_6_HOURS = peer -> new EvolutionaryMaintenance(peer, 10, 2, MUTATION_PROBABILITY, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, new DisseminationStrategyGenerator(5, 3), TERMINATION_CONDITION_6);
    public static final Function<Peer, EvolutionaryMaintenance> EVOLUTIONARY_MAINTENANCE_PFCLUST_10_STOP_AFTER_4_HOURS = peer -> new EvolutionaryMaintenance(peer, 10, 2, MUTATION_PROBABILITY, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, new DisseminationStrategyGenerator(5, 3), TERMINATION_CONDITION_4);
    public static final Function<Peer, EvolutionaryMaintenance> EVOLUTIONARY_MAINTENANCE_PFCLUST_10_STOP_AFTER_2_HOURS = peer -> new EvolutionaryMaintenance(peer, 10, 2, MUTATION_PROBABILITY, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, new DisseminationStrategyGenerator(5, 3), TERMINATION_CONDITION_2);
    public static final Function<Peer, EvolutionaryMaintenance> EVOLUTIONARY_MAINTENANCE_PFCLUST_10_7_5 = peer -> new EvolutionaryMaintenance(peer, 10, 2, MUTATION_PROBABILITY, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, new DisseminationStrategyGenerator(7, 5));
    public static final Function<Peer, EvolutionaryMaintenance> EVOLUTIONARY_MAINTENANCE_PFCLUST_10_9_7 = peer -> new EvolutionaryMaintenance(peer, 10, 2, MUTATION_PROBABILITY, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, new DisseminationStrategyGenerator(9, 7));
    public static final Function<Peer, EvolutionaryMaintenance> EVOLUTIONARY_MAINTENANCE_PFCLUST_10_11_9 = peer -> new EvolutionaryMaintenance(peer, 10, 2, MUTATION_PROBABILITY, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, new DisseminationStrategyGenerator(11, 9));
    public static final Function<Peer, EvolutionaryMaintenance> EVOLUTIONARY_MAINTENANCE_PFCLUST_10_13_11 = peer -> new EvolutionaryMaintenance(peer, 10, 2, MUTATION_PROBABILITY, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, new DisseminationStrategyGenerator(13, 11));
    public static final Function<Peer, EvolutionaryMaintenance> EVOLUTIONARY_MAINTENANCE_PFCLUST_10_15_13 = peer -> new EvolutionaryMaintenance(peer, 10, 2, MUTATION_PROBABILITY, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, new DisseminationStrategyGenerator(15, 13));
    public static final Function<Peer, EvolutionaryMaintenance> EVOLUTIONARY_MAINTENANCE_PFCLUST_10_17_15 = peer -> new EvolutionaryMaintenance(peer, 10, 2, MUTATION_PROBABILITY, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, new DisseminationStrategyGenerator(17, 15));
    public static final Function<Peer, RandomMaintenance> RANDOM_MAINTENANCE_10_7_5 = peer -> new RandomMaintenance(peer, 10, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, new DisseminationStrategyGenerator(7, 5));
    public static final Function<Peer, RandomMaintenance> RANDOM_MAINTENANCE_10_9_7 = peer -> new RandomMaintenance(peer, 10, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, new DisseminationStrategyGenerator(9, 7));
    public static final Function<Peer, RandomMaintenance> RANDOM_MAINTENANCE_10_11_9 = peer -> new RandomMaintenance(peer, 10, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, new DisseminationStrategyGenerator(11, 9));
    public static final Function<Peer, RandomMaintenance> RANDOM_MAINTENANCE_10_13_11 = peer -> new RandomMaintenance(peer, 10, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, new DisseminationStrategyGenerator(13, 11));
    public static final Function<Peer, RandomMaintenance> RANDOM_MAINTENANCE_10_15_13 = peer -> new RandomMaintenance(peer, 10, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, new DisseminationStrategyGenerator(15, 13));
    public static final Function<Peer, RandomMaintenance> RANDOM_MAINTENANCE_10_17_15 = peer -> new RandomMaintenance(peer, 10, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, new DisseminationStrategyGenerator(17, 15));
    public static final Function<Peer, RandomMaintenance> RANDOM_MAINTENANCE_10_30_SEC = peer -> new RandomMaintenance(peer, 10, 30, TimeUnit.SECONDS, PF_CLUST_CLUSTERER, new DisseminationStrategyGenerator(5, 3));
    public static final Function<Peer, RandomMaintenance> RANDOM_MAINTENANCE_10_1_MIN = peer -> new RandomMaintenance(peer, 10, 1, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, new DisseminationStrategyGenerator(5, 3));
    public static final Function<Peer, RandomMaintenance> RANDOM_MAINTENANCE_10_4_MIN = peer -> new RandomMaintenance(peer, 10, 4, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, new DisseminationStrategyGenerator(5, 3));
    public static final Function<Peer, RandomMaintenance> RANDOM_MAINTENANCE_10_8_MIN = peer -> new RandomMaintenance(peer, 10, 8, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, new DisseminationStrategyGenerator(5, 3));
    public static final Function<Peer, RandomMaintenance> RANDOM_MAINTENANCE_10 = peer -> new RandomMaintenance(peer, 10, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, new DisseminationStrategyGenerator(5, 3));
    public static final Function<Peer, RandomMaintenance> RANDOM_MAINTENANCE_10_STOP_AFTER_48_HOURS = peer -> new RandomMaintenance(peer, 10, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, new DisseminationStrategyGenerator(5, 3), TERMINATION_CONDITION_48);
    public static final Function<Peer, RandomMaintenance> RANDOM_MAINTENANCE_10_STOP_AFTER_10_HOURS = peer -> new RandomMaintenance(peer, 10, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, new DisseminationStrategyGenerator(5, 3), TERMINATION_CONDITION_10);
    public static final Function<Peer, RandomMaintenance> RANDOM_MAINTENANCE_10_STOP_AFTER_8_HOURS = peer -> new RandomMaintenance(peer, 10, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, new DisseminationStrategyGenerator(5, 3), TERMINATION_CONDITION_8);
    public static final Function<Peer, RandomMaintenance> RANDOM_MAINTENANCE_10_STOP_AFTER_6_HOURS = peer -> new RandomMaintenance(peer, 10, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, new DisseminationStrategyGenerator(5, 3), TERMINATION_CONDITION_6);
    public static final Function<Peer, RandomMaintenance> RANDOM_MAINTENANCE_10_STOP_AFTER_4_HOURS = peer -> new RandomMaintenance(peer, 10, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, new DisseminationStrategyGenerator(5, 3), TERMINATION_CONDITION_4);
    public static final Function<Peer, RandomMaintenance> RANDOM_MAINTENANCE_10_STOP_AFTER_2_HOURS = peer -> new RandomMaintenance(peer, 10, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, new DisseminationStrategyGenerator(5, 3), TERMINATION_CONDITION_2);

    public static final Function<Peer, EvolutionaryMaintenance> EVOLUTIONARY_MAINTENANCE_PFCLUST_20 = peer -> new EvolutionaryMaintenance(peer, 20, 2, MUTATION_PROBABILITY, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, new DisseminationStrategyGenerator(5, 3));
    public static final Function<Peer, EvolutionaryMaintenance> EVOLUTIONARY_MAINTENANCE_PFCLUST_30 = peer -> new EvolutionaryMaintenance(peer, 30, 2, MUTATION_PROBABILITY, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, new DisseminationStrategyGenerator(5, 3));
    public static final Function<Peer, EvolutionaryMaintenance> EVOLUTIONARY_MAINTENANCE_PFCLUST_40 = peer -> new EvolutionaryMaintenance(peer, 40, 2, MUTATION_PROBABILITY, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, new DisseminationStrategyGenerator(5, 3));
    public static final Function<Peer, EvolutionaryMaintenance> EVOLUTIONARY_MAINTENANCE_PFCLUST_50 = peer -> new EvolutionaryMaintenance(peer, 50, 2, MUTATION_PROBABILITY, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER, new DisseminationStrategyGenerator(5, 3));
    public static final Function<Peer, EvolutionaryMaintenance> EVOLUTIONARY_MAINTENANCE_KMEAN_PLUS_PLUS_10 = peer -> new EvolutionaryMaintenance(peer, 10, 2, MUTATION_PROBABILITY, 2, TimeUnit.MINUTES, new KMeansPlusPlusClusterer<EvaluatedDisseminationStrategy>(5, 100, new EuclideanDistance(), new MersenneTwister(852)), new DisseminationStrategyGenerator(5, 3));
    public static final Function<Peer, EvolutionaryMaintenance> EVOLUTIONARY_MAINTENANCE_PER_POINT_CLUSTER_10 = peer -> new EvolutionaryMaintenance(peer, 10, 2, MUTATION_PROBABILITY, 2, TimeUnit.MINUTES, new PerPointClusterer<EvaluatedDisseminationStrategy>(), new DisseminationStrategyGenerator(5, 3));

    public static final UniformSyntheticDelay BLUB_UNIFORMLY_DISTRIBUTED_SYNTHETIC_DELAY = new UniformSyntheticDelay(233763, 866279);
    public static final int ACTIVE_MAINTENANCE_INTERVAL_MILLIS = 1_500;

    public static final SequentialPortNumberSupplier PORT_NUMBER_PROVIDER = new SequentialPortNumberSupplier(64000);

    public static final KeySupplier TARGET_KEY_PROVIDER = new KeySupplier(SEED);
    public static final Supplier<KeySupplier> PEER_KEY_PROVIDER = () -> new KeySupplier(SEED);

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

    private static final Function<Peer, Maintenance> NO_MAINTENANCE = peer -> null;
    public static final ScheduledExecutorService SCHEDULED_EXECUTOR_SERVICE = Executors.newScheduledThreadPool(300);
    private static final PeerConfiguration.Builder BASE_BUILDER = new PeerConfiguration.Builder().enableApplicationFeedback(false)
            .syntheticDelay(BLUB_UNIFORMLY_DISTRIBUTED_SYNTHETIC_DELAY).executor(() -> SCHEDULED_EXECUTOR_SERVICE);

    private static final int FINGER_TABLE_SIZE = 10;
    private static final int SUCCESSOR_LIST_SIZE = 8;
    private static final int MAINTENANCE_INTERVAL = 3;
    private static final TimeUnit MAINTENANCE_INTERVAL_UNIT = TimeUnit.SECONDS;
    private static final BigInteger INTER_FINGER_RATIO = Key.TWO;

    public static final PeerConfiguration CHORD = new PeerConfiguration.Builder(BASE_BUILDER).peerState(peer -> new ChordPeerState(peer, FINGER_TABLE_SIZE, INTER_FINGER_RATIO, SUCCESSOR_LIST_SIZE))
            .joinStrategy(peer -> new ChordJoinStrategy(peer))
            .nextHopStrategy(peer -> new ChordNextHopStrategy(peer))
            .lookupStrategy(peer -> new ChordLookupStrategy(peer))
            .maintenance(peer -> new ChordMaintenance(peer, MAINTENANCE_INTERVAL, MAINTENANCE_INTERVAL_UNIT))
            .build();

    private static final PeerConfiguration.Builder BASE_TROMBONE_CONFIGURATION = new PeerConfiguration.Builder(BASE_BUILDER).peerState(peer -> new TrombonePeerState(peer))
            .joinStrategy(peer -> new MinimalJoinStrategy(peer))
            .nextHopStrategy(peer -> new TromboneNextHopStrategy(peer))
            .lookupStrategy(peer -> new ChordLookupStrategy(peer));

    private static final Function<Peer, Maintenance> PERIODIC_STABILIZATION = peer -> new StrategicMaintenance(peer, new StrongStabilization(), MAINTENANCE_INTERVAL, MAINTENANCE_INTERVAL_UNIT);
    private static final PFClustClusterer<EvaluatedDisseminationStrategy> CLUSTERER = new PFClustClusterer<>(4121);
    private static final DisseminationStrategyGenerator STRATEGY_GENERATOR = new DisseminationStrategyGenerator(10, 5);
    private static final int POPULATION_SIZE = 10;
    private static final Function<Peer, Maintenance> EVOLUTIONARY_MAINTENANCE = peer -> new EvolutionaryMaintenance(peer, POPULATION_SIZE, 1, MUTATION_PROBABILITY, 2, TimeUnit.MINUTES, CLUSTERER, STRATEGY_GENERATOR);
    private static final Function<Peer, Maintenance> EVOLUTIONARY_MAINTENANCE_STOP_AFTER_2 = peer -> new EvolutionaryMaintenance(peer, POPULATION_SIZE, 1, MUTATION_PROBABILITY, 2, TimeUnit.MINUTES, CLUSTERER, STRATEGY_GENERATOR, TERMINATION_CONDITION_2);
    private static final Function<Peer, Maintenance> EVOLUTIONARY_MAINTENANCE_STOP_AFTER_4 = peer -> new EvolutionaryMaintenance(peer, POPULATION_SIZE, 1, MUTATION_PROBABILITY, 2, TimeUnit.MINUTES, CLUSTERER, STRATEGY_GENERATOR, TERMINATION_CONDITION_4);
    private static final Function<Peer, Maintenance> EVOLUTIONARY_MAINTENANCE_STOP_AFTER_8 = peer -> new EvolutionaryMaintenance(peer, POPULATION_SIZE, 1, MUTATION_PROBABILITY, 2, TimeUnit.MINUTES, CLUSTERER, STRATEGY_GENERATOR, TERMINATION_CONDITION_8);
    private static final Function<Peer, Maintenance> EVOLUTIONARY_MAINTENANCE_STOP_AFTER_16 = peer -> new EvolutionaryMaintenance(peer, POPULATION_SIZE, 1, MUTATION_PROBABILITY, 2, TimeUnit.MINUTES, CLUSTERER, STRATEGY_GENERATOR, TERMINATION_CONDITION_16);

    private static final Function<Peer, Maintenance> RANDOM_MAINTENANCE = peer -> new RandomMaintenance(peer, POPULATION_SIZE, 2, TimeUnit.MINUTES, CLUSTERER, STRATEGY_GENERATOR);
    private static final Function<Peer, Maintenance> RANDOM_MAINTENANCE_STOP_AFTER_2 = peer -> new RandomMaintenance(peer, POPULATION_SIZE, 2, TimeUnit.MINUTES, CLUSTERER, STRATEGY_GENERATOR, TERMINATION_CONDITION_2);
    private static final Function<Peer, Maintenance> RANDOM_MAINTENANCE_STOP_AFTER_4 = peer -> new RandomMaintenance(peer, POPULATION_SIZE, 2, TimeUnit.MINUTES, CLUSTERER, STRATEGY_GENERATOR, TERMINATION_CONDITION_4);
    private static final Function<Peer, Maintenance> RANDOM_MAINTENANCE_STOP_AFTER_8 = peer -> new RandomMaintenance(peer, POPULATION_SIZE, 2, TimeUnit.MINUTES, CLUSTERER, STRATEGY_GENERATOR, TERMINATION_CONDITION_8);
    private static final Function<Peer, Maintenance> RANDOM_MAINTENANCE_STOP_AFTER_16 = peer -> new RandomMaintenance(peer, POPULATION_SIZE, 2, TimeUnit.MINUTES, CLUSTERER, STRATEGY_GENERATOR, TERMINATION_CONDITION_16);


    public static final PeerConfiguration TROMBONE_NO_MAINTENANCE = BASE_TROMBONE_CONFIGURATION.maintenance(NO_MAINTENANCE)
            .build();

    public static final PeerConfiguration TROMBONE_STABILISATION = BASE_TROMBONE_CONFIGURATION.maintenance(PERIODIC_STABILIZATION).build();
    public static final PeerConfiguration TROMBONE_ADAPTIVE_GA = BASE_TROMBONE_CONFIGURATION.maintenance(EVOLUTIONARY_MAINTENANCE).build();
    public static final PeerConfiguration TROMBONE_ADAPTIVE_GA_2 = BASE_TROMBONE_CONFIGURATION.maintenance(EVOLUTIONARY_MAINTENANCE_STOP_AFTER_2).build();
    public static final PeerConfiguration TROMBONE_ADAPTIVE_GA_4 = BASE_TROMBONE_CONFIGURATION.maintenance(EVOLUTIONARY_MAINTENANCE_STOP_AFTER_4).build();
    public static final PeerConfiguration TROMBONE_ADAPTIVE_GA_8 = BASE_TROMBONE_CONFIGURATION.maintenance(EVOLUTIONARY_MAINTENANCE_STOP_AFTER_8).build();
    public static final PeerConfiguration TROMBONE_ADAPTIVE_GA_16 = BASE_TROMBONE_CONFIGURATION.maintenance(EVOLUTIONARY_MAINTENANCE_STOP_AFTER_16).build();

    public static final PeerConfiguration TROMBONE_ADAPTIVE_RANDOM = BASE_TROMBONE_CONFIGURATION.maintenance(RANDOM_MAINTENANCE).build();
    public static final PeerConfiguration TROMBONE_ADAPTIVE_RANDOM_2 = BASE_TROMBONE_CONFIGURATION.maintenance(RANDOM_MAINTENANCE_STOP_AFTER_2).build();
    public static final PeerConfiguration TROMBONE_ADAPTIVE_RANDOM_4 = BASE_TROMBONE_CONFIGURATION.maintenance(RANDOM_MAINTENANCE_STOP_AFTER_4).build();
    public static final PeerConfiguration TROMBONE_ADAPTIVE_RANDOM_8 = BASE_TROMBONE_CONFIGURATION.maintenance(RANDOM_MAINTENANCE_STOP_AFTER_8).build();
    public static final PeerConfiguration TROMBONE_ADAPTIVE_RANDOM_16 = BASE_TROMBONE_CONFIGURATION.maintenance(RANDOM_MAINTENANCE_STOP_AFTER_16).build();

    static final PeerConfiguration[] PEER_CONFIGURATIONS = {
            CHORD, TROMBONE_NO_MAINTENANCE, TROMBONE_STABILISATION, TROMBONE_ADAPTIVE_GA, TROMBONE_ADAPTIVE_RANDOM
    };

    public static final List<PeerConfiguration> ALL = new CopyOnWriteArrayList<>(PEER_CONFIGURATIONS);

}
