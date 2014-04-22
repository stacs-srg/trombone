package uk.ac.standrews.cs.trombone.evaluation.scenarios;

import java.util.concurrent.TimeUnit;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.apache.commons.math3.random.MersenneTwister;
import org.uncommons.maths.binary.BinaryUtils;
import org.uncommons.maths.random.Probability;
import uk.ac.standrews.cs.shabdiz.util.Duration;
import uk.ac.standrews.cs.trombone.core.Maintenance;
import uk.ac.standrews.cs.trombone.core.PeerConfiguration;
import uk.ac.standrews.cs.trombone.core.adaptation.EvaluatedDisseminationStrategy;
import uk.ac.standrews.cs.trombone.core.adaptation.EvolutionaryMaintenance;
import uk.ac.standrews.cs.trombone.core.adaptation.RandomMaintenance;
import uk.ac.standrews.cs.trombone.core.adaptation.clustering.PerPointClusterer;
import uk.ac.standrews.cs.trombone.core.adaptation.clustering.pfclust.PFClustClusterer;
import uk.ac.standrews.cs.trombone.core.key.KeyProvider;
import uk.ac.standrews.cs.trombone.core.key.ZipfKeyProvider;
import uk.ac.standrews.cs.trombone.event.churn.Churn;
import uk.ac.standrews.cs.trombone.event.churn.FixedExponentialInterval;
import uk.ac.standrews.cs.trombone.event.churn.IntervalGenerator;
import uk.ac.standrews.cs.trombone.event.churn.OscillatingExponentialInterval;
import uk.ac.standrews.cs.trombone.event.churn.Workload;
import uk.ac.standrews.cs.trombone.event.provider.SequentialPortNumberProvider;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public final class Constants {

    public static final int NUMBER_OF_REPETITIONS = 5;
    public static final int NETWORK_SIZE = 1_000;
    public static final PFClustClusterer<EvaluatedDisseminationStrategy> PF_CLUST_CLUSTERER = new PFClustClusterer<>();

    public static final Duration EXPERIMENT_DURATION_14 = new Duration(14, TimeUnit.HOURS);
    public static final Duration EXPERIMENT_DURATION_12 = new Duration(12, TimeUnit.HOURS);
    public static final Duration EXPERIMENT_DURATION_10 = new Duration(10, TimeUnit.HOURS);
    public static final Duration EXPERIMENT_DURATION_8 = new Duration(8, TimeUnit.HOURS);
    public static final Duration EXPERIMENT_DURATION_6 = new Duration(6, TimeUnit.HOURS);
    public static final Duration EXPERIMENT_DURATION_4 = new Duration(4, TimeUnit.HOURS);

    public static final Probability MUTATION_PROBABILITY = new Probability(0.1);
    public static final Maintenance NO_MAINTENANCE = new Maintenance(null);
    public static final Maintenance SUCCESSOR_MAINTENANCE = new Maintenance(new SuccessorMaintenance());
    public static final Maintenance SUCCESSOR_LIST_MAINTENANCE_5 = new Maintenance(new SuccessorListMaintenance(5));
    public static final Maintenance RANDOM_SELECTOR_MAINTENANCE_3 = new Maintenance(new RandomSelectorMaintenance(3, 3));
    public static final Maintenance MOST_RECENTLY_SEEN_3 = new Maintenance(new MostRecentlySeenMaintenance(3, 3));
    public static final Maintenance RANDOM_MAINTENANCE = new RandomMaintenance(10, 2, TimeUnit.MINUTES, new PerPointClusterer<EvaluatedDisseminationStrategy>());

    public static final EvolutionaryMaintenance.ElapsedTimeTerminationCondition TERMINATION_CONDITION_10 = new EvolutionaryMaintenance.ElapsedTimeTerminationCondition(10, TimeUnit.HOURS);
    public static final EvolutionaryMaintenance.ElapsedTimeTerminationCondition TERMINATION_CONDITION_8 = new EvolutionaryMaintenance.ElapsedTimeTerminationCondition(8, TimeUnit.HOURS);
    public static final EvolutionaryMaintenance.ElapsedTimeTerminationCondition TERMINATION_CONDITION_6 = new EvolutionaryMaintenance.ElapsedTimeTerminationCondition(6, TimeUnit.HOURS);
    public static final EvolutionaryMaintenance.ElapsedTimeTerminationCondition TERMINATION_CONDITION_4 = new EvolutionaryMaintenance.ElapsedTimeTerminationCondition(4, TimeUnit.HOURS);
    public static final EvolutionaryMaintenance.ElapsedTimeTerminationCondition TERMINATION_CONDITION_2 = new EvolutionaryMaintenance.ElapsedTimeTerminationCondition(2, TimeUnit.HOURS);

    public static final EvolutionaryMaintenance EVOLUTIONARY_MAINTENANCE_PFCLUST_10 = new EvolutionaryMaintenance(10, 2, MUTATION_PROBABILITY, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER);

    public static final EvolutionaryMaintenance EVOLUTIONARY_MAINTENANCE_PFCLUST_10_STOP_AFTER_10_HOURS = new EvolutionaryMaintenance(10, 2, MUTATION_PROBABILITY, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER);
    public static final EvolutionaryMaintenance EVOLUTIONARY_MAINTENANCE_PFCLUST_10_STOP_AFTER_8_HOURS = new EvolutionaryMaintenance(10, 2, MUTATION_PROBABILITY, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER);
    public static final EvolutionaryMaintenance EVOLUTIONARY_MAINTENANCE_PFCLUST_10_STOP_AFTER_6_HOURS = new EvolutionaryMaintenance(10, 2, MUTATION_PROBABILITY, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER);
    public static final EvolutionaryMaintenance EVOLUTIONARY_MAINTENANCE_PFCLUST_10_STOP_AFTER_4_HOURS = new EvolutionaryMaintenance(10, 2, MUTATION_PROBABILITY, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER);
    public static final EvolutionaryMaintenance EVOLUTIONARY_MAINTENANCE_PFCLUST_10_STOP_AFTER_2_HOURS = new EvolutionaryMaintenance(10, 2, MUTATION_PROBABILITY, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER);

    static {
        EVOLUTIONARY_MAINTENANCE_PFCLUST_10_STOP_AFTER_10_HOURS.setTerminationCondition(TERMINATION_CONDITION_10);
        EVOLUTIONARY_MAINTENANCE_PFCLUST_10_STOP_AFTER_8_HOURS.setTerminationCondition(TERMINATION_CONDITION_8);
        EVOLUTIONARY_MAINTENANCE_PFCLUST_10_STOP_AFTER_6_HOURS.setTerminationCondition(TERMINATION_CONDITION_6);
        EVOLUTIONARY_MAINTENANCE_PFCLUST_10_STOP_AFTER_4_HOURS.setTerminationCondition(TERMINATION_CONDITION_4);
        EVOLUTIONARY_MAINTENANCE_PFCLUST_10_STOP_AFTER_2_HOURS.setTerminationCondition(TERMINATION_CONDITION_2);
    }

    public static final EvolutionaryMaintenance EVOLUTIONARY_MAINTENANCE_PFCLUST_20 = new EvolutionaryMaintenance(20, 2, MUTATION_PROBABILITY, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER);
    public static final EvolutionaryMaintenance EVOLUTIONARY_MAINTENANCE_PFCLUST_30 = new EvolutionaryMaintenance(30, 2, MUTATION_PROBABILITY, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER);
    public static final EvolutionaryMaintenance EVOLUTIONARY_MAINTENANCE_PFCLUST_40 = new EvolutionaryMaintenance(40, 2, MUTATION_PROBABILITY, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER);
    public static final EvolutionaryMaintenance EVOLUTIONARY_MAINTENANCE_PFCLUST_50 = new EvolutionaryMaintenance(50, 2, MUTATION_PROBABILITY, 2, TimeUnit.MINUTES, PF_CLUST_CLUSTERER);
    public static final EvolutionaryMaintenance EVOLUTIONARY_MAINTENANCE_KMEAN_PLUS_PLUS_10 = new EvolutionaryMaintenance(10, 2, MUTATION_PROBABILITY, 2, TimeUnit.MINUTES, new KMeansPlusPlusClusterer<EvaluatedDisseminationStrategy>(5, 100, new EuclideanDistance(), new MersenneTwister(852)));
    public static final EvolutionaryMaintenance EVOLUTIONARY_MAINTENANCE_PER_POINT_CLUSTER_10 = new EvolutionaryMaintenance(10, 2, MUTATION_PROBABILITY, 2, TimeUnit.MINUTES, new PerPointClusterer<EvaluatedDisseminationStrategy>());

    private Constants() {

    }

    private static final byte[] SEED = BinaryUtils.convertHexStringToBytes("2ABFEAE2AB6A2C60109803310D9254DF");
    public static final UniformSyntheticDelay BLUB_UNIFORMLY_DISTRIBUTED_SYNTHETIC_DELAY = new UniformSyntheticDelay(233763, 866279, DigestUtils.md5("masih"));
    public static final int ACTIVE_MAINTENANCE_INTERVAL_MILLIS = 1_500;
    public static final int LOOKUP_RETRY_COUNT = 5;

    // Master random seed provider
    public static final SequentialPortNumberProvider PORT_NUMBER_PROVIDER = new SequentialPortNumberProvider(64000);
    public static final Duration OBSERVATION_INTERVAL = new Duration(10, TimeUnit.SECONDS);
    public static final int KEY_LENGTH_IN_BITS = Integer.SIZE;
    private static final ZipfKeyProvider TARGET_KEY_PROVIDER = new ZipfKeyProvider(20_000, 1, KEY_LENGTH_IN_BITS, SEED);
    public static final KeyProvider PEER_KEY_PROVIDER = new KeyProvider(KEY_LENGTH_IN_BITS, SEED);

    public static final Duration DURATION_500_MS = new Duration(1000, TimeUnit.MILLISECONDS);
    public static final Duration DURATION_10_S = new Duration(10, TimeUnit.SECONDS);
    public static final Duration DURATION_30_S = new Duration(30, TimeUnit.SECONDS);
    public static final Duration DURATION_30_MIN = new Duration(30, TimeUnit.MINUTES);
    public static final Duration DURATION_1_HOUR = new Duration(1, TimeUnit.HOURS);

    // Distributions
    public static final IntervalGenerator FIXED_EXP_500_MS = new FixedExponentialInterval(DURATION_500_MS, SEED);
    public static final IntervalGenerator FIXED_EXP_10_S = new FixedExponentialInterval(DURATION_10_S, SEED);
    public static final IntervalGenerator FIXED_EXP_30_S = new FixedExponentialInterval(DURATION_30_S, SEED);
    public static final IntervalGenerator FIXED_EXP_30_MIN = new FixedExponentialInterval(DURATION_30_MIN, SEED);
    public static final IntervalGenerator OSCILLATING_EXP_10_S_TO_500_MS = new OscillatingExponentialInterval(DURATION_10_S, DURATION_500_MS, DURATION_30_MIN, SEED);
    public static final IntervalGenerator OSCILLATING_EXP_30_S_TO_30_MIN = new OscillatingExponentialInterval(DURATION_30_MIN, DURATION_30_S, DURATION_1_HOUR, SEED);
    public static final IntervalGenerator OSCILLATING_EXP_30_S_TO_30_MIN_INVERSE = new OscillatingExponentialInterval(DURATION_30_S, DURATION_30_MIN, DURATION_1_HOUR, SEED);

    // Churn patterns
    public static final Churn NO_CHURN = Churn.NONE;
    public static final Churn CHURN_1 = new Churn(FIXED_EXP_30_S, FIXED_EXP_30_S);
    public static final Churn CHURN_2 = new Churn(FIXED_EXP_30_S, FIXED_EXP_30_MIN);
    public static final Churn CHURN_3 = new Churn(FIXED_EXP_30_MIN, FIXED_EXP_30_S);
    public static final Churn CHURN_4 = new Churn(FIXED_EXP_30_MIN, FIXED_EXP_30_MIN);
    public static final Churn CHURN_5 = new Churn(OSCILLATING_EXP_30_S_TO_30_MIN, OSCILLATING_EXP_30_S_TO_30_MIN);
    public static final Churn CHURN_6 = new Churn(OSCILLATING_EXP_30_S_TO_30_MIN, OSCILLATING_EXP_30_S_TO_30_MIN_INVERSE);

    // Workload patterns
    public static final Workload NO_WORKLOAD = Workload.NONE;
    public static final Workload WORKLOAD_1 = new Workload(TARGET_KEY_PROVIDER, FIXED_EXP_500_MS);
    public static final Workload WORKLOAD_2 = new Workload(TARGET_KEY_PROVIDER, FIXED_EXP_10_S);
    public static final Workload WORKLOAD_3 = new Workload(TARGET_KEY_PROVIDER, OSCILLATING_EXP_10_S_TO_500_MS);

    public static final PeerConfiguration NO_MAINTENANCE_CONFIGURATION = new PeerConfiguration(NO_MAINTENANCE, BLUB_UNIFORMLY_DISTRIBUTED_SYNTHETIC_DELAY);
    public static final byte[] SCENARIO_MASTER_SEED = BinaryUtils.convertHexStringToBytes("2AAFEAE2AB6A2C60109803310D9254DF");

}
