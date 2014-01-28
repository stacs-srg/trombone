package uk.ac.standrews.cs.trombone.evaluation.scenarios;

import java.util.concurrent.TimeUnit;
import javax.inject.Provider;
import org.mashti.sina.distribution.ExponentialDistribution;
import uk.ac.standrews.cs.trombone.core.key.ZipfKeyProvider;
import uk.ac.standrews.cs.trombone.event.churn.Churn;
import uk.ac.standrews.cs.trombone.event.provider.ConstantRateUncorrelatedUniformChurnProvider;
import uk.ac.standrews.cs.trombone.event.provider.ConstantRateWorkloadProvider;
import uk.ac.standrews.cs.trombone.event.provider.NoChurnProvider;
import uk.ac.standrews.cs.trombone.event.provider.NoWorkloadProvider;
import uk.ac.standrews.cs.trombone.event.provider.RandomSeedProvider;
import uk.ac.standrews.cs.trombone.event.workload.Workload;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public final class Constants {

    public static final int KEY_LENGTH_IN_BITS = Integer.SIZE;
    // Master random seed provider
    public static final RandomSeedProvider SEED_PROVIDER = new RandomSeedProvider(0b001100_010010_011110_100001_101101_110011L);
    // Distributions
    public static final ExponentialDistribution EXP_500_MS = ExponentialDistribution.byMean(500, TimeUnit.MILLISECONDS);
    public static final ExponentialDistribution EXP_10_S = ExponentialDistribution.byMean(10, TimeUnit.SECONDS);
    public static final ExponentialDistribution EXP_30_S = ExponentialDistribution.byMean(30, TimeUnit.SECONDS);
    public static final ExponentialDistribution EXP_30_MIN = ExponentialDistribution.byMean(30, TimeUnit.MINUTES);
    // Churn patterns
    public static final Provider<Churn> NO_CHURN = NoChurnProvider.getInstance();
    public static final Provider<Churn> CHURN_1 = new ConstantRateUncorrelatedUniformChurnProvider(EXP_30_S, EXP_30_S, SEED_PROVIDER);
    public static final Provider<Churn> CHURN_2 = new ConstantRateUncorrelatedUniformChurnProvider(EXP_30_S, EXP_30_MIN, SEED_PROVIDER);
    public static final Provider<Churn> CHURN_3 = new ConstantRateUncorrelatedUniformChurnProvider(EXP_30_MIN, EXP_30_S, SEED_PROVIDER);
    public static final Provider<Churn> CHURN_4 = new ConstantRateUncorrelatedUniformChurnProvider(EXP_30_MIN, EXP_30_MIN, SEED_PROVIDER);
    // Workload patterns
    public static final Provider<Workload> NO_WORKLOAD = NoWorkloadProvider.getInstance();
    public static final Provider<Workload> WORKLOAD_1 = new ConstantRateWorkloadProvider(EXP_500_MS, new ZipfKeyProvider(20_000, 1, KEY_LENGTH_IN_BITS, SEED_PROVIDER.get()), SEED_PROVIDER);
    public static final Provider<Workload> WORKLOAD_2 = new ConstantRateWorkloadProvider(EXP_10_S, new ZipfKeyProvider(20_000, 1, KEY_LENGTH_IN_BITS, SEED_PROVIDER.get()), SEED_PROVIDER);

}
