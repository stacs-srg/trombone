package uk.ac.standrews.cs.trombone.evaluation.scenarios;

import java.util.concurrent.TimeUnit;
import org.apache.commons.codec.digest.DigestUtils;
import org.uncommons.maths.binary.BinaryUtils;
import org.uncommons.maths.random.Probability;
import uk.ac.standrews.cs.shabdiz.util.Duration;
import uk.ac.standrews.cs.trombone.core.Maintenance;
import uk.ac.standrews.cs.trombone.core.PeerConfiguration;
import uk.ac.standrews.cs.trombone.core.adaptation.EvolutionaryMaintenance;
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
    public static final Probability MUTATION_PROBABILITY = new Probability(0.1);
    public static final Maintenance NO_MAINTENANCE = new Maintenance(null);
    public static final Maintenance SUCCESSOR_MAINTENANCE = new Maintenance(new SuccessorMaintenance());
    public static final Maintenance SUCCESSOR_LIST_MAINTENANCE_5 = new Maintenance(new SuccessorListMaintenance(5));
    public static final Maintenance RANDOM_MAINTENANCE_2 = new Maintenance(new RandomMaintenance(3, 3));
    public static final Maintenance EVOLUTIONARY_MAINTENANCE = new EvolutionaryMaintenance(10, 2, MUTATION_PROBABILITY, 2, TimeUnit.MINUTES);

    private Constants() {

    }

    private static final byte[] SEED = BinaryUtils.convertHexStringToBytes("2ABFEAE2AB6A2C60109803310D9254DF");
    public static final UniformSyntheticDelay BLUB_UNIFORMLY_DISTRIBUTED_SYNTHETIC_DELAY = new UniformSyntheticDelay(233763, 866279, DigestUtils.md5("masih"));
    public static final int ACTIVE_MAINTENANCE_INTERVAL_MILLIS = 1_500;
    public static final int LOOKUP_RETRY_COUNT = 5;

    // Master random seed provider
    public static final SequentialPortNumberProvider PORT_NUMBER_PROVIDER = new SequentialPortNumberProvider(64000);
    public static final Duration EXPERIMENT_DURATION = new Duration(4, TimeUnit.HOURS);
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
