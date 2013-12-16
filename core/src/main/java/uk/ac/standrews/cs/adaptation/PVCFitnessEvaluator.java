package uk.ac.standrews.cs.adaptation;

import java.util.List;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncommons.watchmaker.framework.FitnessEvaluator;
import uk.ac.standrews.cs.trombone.core.DisseminationStrategy;
import uk.ac.standrews.cs.trombone.core.Maintenance;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerMetric;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class PVCFitnessEvaluator implements FitnessEvaluator<List<DisseminationStrategy>> {

    public static final long STRATEGY_EVALUATION_TIME = TimeUnit.MILLISECONDS.convert(1, TimeUnit.MINUTES);
    private static final Logger LOGGER = LoggerFactory.getLogger(PVCFitnessEvaluator.class);
    private static final double[] ORIGIN = {0, 0, 0};
    private final PeerMetric metric;
    private final Maintenance maintenance;

    public PVCFitnessEvaluator(final Peer local) {

        metric = local.getPeerMetric();

        maintenance = local.getMaintenance();

    }

    @Override
    public synchronized double getFitness(final List<DisseminationStrategy> candidate, final List<? extends List<DisseminationStrategy>> population) {

        maintenance.reset();
        maintenance.addAll(candidate);
        try {
            Thread.sleep(STRATEGY_EVALUATION_TIME);
        }
        catch (InterruptedException e) {
            LOGGER.error("interrupted while evaluating candidate", e);
        }

        final long mean_lookup_success_delay_millis = metric.getMeanLookupSuccessDelay(TimeUnit.MILLISECONDS);
        final long sent_bytes = metric.getSentBytes();
        final double lookup_failure_rate = metric.getLookupFailureRate();

        //TODO clustering of the environment

        return euclideanDistance(new double[] {mean_lookup_success_delay_millis, sent_bytes, lookup_failure_rate}, ORIGIN);
    }

    @Override
    public boolean isNatural() {

        return false; // means less is better
    }

    private static double euclideanDistance(final double[] p1, final double[] p2) {

        assert p1.length == p2.length;

        double sum = 0;
        for (int i = 0; i < p1.length; i++) {
            final double dp = p1[i] - p2[i];
            sum += dp * dp;
        }
        return Math.sqrt(sum);
    }
}
