package uk.ac.standrews.cs.trombone.evaluation.scenarios;

import java.util.concurrent.TimeUnit;
import org.mashti.sina.distribution.ExponentialDistribution;
import uk.ac.standrews.cs.shabdiz.util.Duration;
import uk.ac.standrews.cs.trombone.core.Maintenance;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerConfiguration;
import uk.ac.standrews.cs.trombone.core.key.RandomKeyProvider;
import uk.ac.standrews.cs.trombone.core.key.ZipfKeyProvider;
import uk.ac.standrews.cs.trombone.event.provider.ConstantRateUncorrelatedChurnProvider;
import uk.ac.standrews.cs.trombone.event.provider.ConstantRateWorkloadProvider;
import uk.ac.standrews.cs.trombone.event.provider.NoChurnProvider;
import uk.ac.standrews.cs.trombone.event.provider.NoWorkloadProvider;
import uk.ac.standrews.cs.trombone.event.provider.RandomSeedProvider;
import uk.ac.standrews.cs.trombone.event.provider.SequentialPortNumberProvider;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public final class Constants {

    private Constants() {

    }

    public static final int LOOKUP_RETRY_COUNT = 5;
    // Master random seed provider
    private static final RandomSeedProvider SEED_PROVIDER = new RandomSeedProvider(13194894195L);
    public static final SequentialPortNumberProvider PORT_NUMBER_PROVIDER = new SequentialPortNumberProvider(45000);
    public static final Duration EXPERIMENT_DURATION = new Duration(30, TimeUnit.MINUTES);
    public static final Duration OBSERVATION_INTERVAL = new Duration(10, TimeUnit.SECONDS);
    public static final int KEY_LENGTH_IN_BITS = Integer.SIZE;
    public static final ZipfKeyProvider TARGET_KEY_PROVIDER = new ZipfKeyProvider(20_000, 1, KEY_LENGTH_IN_BITS, SEED_PROVIDER.get());
    public static final RandomKeyProvider PEER_KEY_PROVIDER = new RandomKeyProvider(SEED_PROVIDER.get(), KEY_LENGTH_IN_BITS);
    // Distributions
    public static final ExponentialDistribution EXP_500_MS = ExponentialDistribution.byMean(500, TimeUnit.MILLISECONDS);
    public static final ExponentialDistribution EXP_10_S = ExponentialDistribution.byMean(10, TimeUnit.SECONDS);
    public static final ExponentialDistribution EXP_30_S = ExponentialDistribution.byMean(30, TimeUnit.SECONDS);
    public static final ExponentialDistribution EXP_30_MIN = ExponentialDistribution.byMean(30, TimeUnit.MINUTES);
    // Churn patterns
    public static final NoChurnProvider NO_CHURN = NoChurnProvider.getInstance();
    public static final ConstantRateUncorrelatedChurnProvider CHURN_1 = new ConstantRateUncorrelatedChurnProvider(EXP_30_S, EXP_30_S, SEED_PROVIDER.clone());
    public static final ConstantRateUncorrelatedChurnProvider CHURN_2 = new ConstantRateUncorrelatedChurnProvider(EXP_30_S, EXP_30_MIN, SEED_PROVIDER.clone());
    public static final ConstantRateUncorrelatedChurnProvider CHURN_3 = new ConstantRateUncorrelatedChurnProvider(EXP_30_MIN, EXP_30_S, SEED_PROVIDER.clone());
    public static final ConstantRateUncorrelatedChurnProvider CHURN_4 = new ConstantRateUncorrelatedChurnProvider(EXP_30_MIN, EXP_30_MIN, SEED_PROVIDER.clone());
    // Workload patterns
    public static final NoWorkloadProvider NO_WORKLOAD = NoWorkloadProvider.getInstance();
    public static final ConstantRateWorkloadProvider WORKLOAD_1 = new ConstantRateWorkloadProvider(EXP_500_MS, TARGET_KEY_PROVIDER, SEED_PROVIDER.clone());
    public static final ConstantRateWorkloadProvider WORKLOAD_2 = new ConstantRateWorkloadProvider(EXP_10_S, TARGET_KEY_PROVIDER, SEED_PROVIDER.clone());
    public static final NoMaintenance NO_MAINTENANCE = new NoMaintenance();
    public static final long SCENARIO_MASTER_SEED = 85245674;

    public static class NoMaintenance implements PeerConfiguration {

        private static final long serialVersionUID = -8467288034404453952L;

        @Override
        public Maintenance getMaintenance(final Peer peer) {

            return new Maintenance(peer);
        }
    }

}
