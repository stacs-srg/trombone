package uk.ac.standrews.cs.trombone.core.adaptation;

import java.util.concurrent.TimeUnit;
import org.apache.commons.math3.ml.clustering.Clusterable;
import uk.ac.standrews.cs.trombone.core.PeerMetric;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class EnvironmentSnapshot implements Clusterable {

    private static final long MAX_LOOKUP_DELAY_MILLIS = TimeUnit.MILLISECONDS.convert(10, TimeUnit.SECONDS);
    private static final double MAX_SENT_BYTES_PER_SECOND = 100 * 5;
    private static final double MAX_LOOKUP_RATE_PER_SECOND = 1;
    private static final double MAX_LOOKUP_FAILURE_RATE_PER_SECOND = MAX_LOOKUP_RATE_PER_SECOND;
    private final double lookup_failure_rate;
    private final long mean_lookup_success_delay_millis;
    private final double sent_bytes_per_second;
    private final double lookup_execution_rate_per_second;
    private final double served_next_hop_rate_per_second;
    private final double rpc_error_rate_per_second;

    public EnvironmentSnapshot(PeerMetric metric) {

        lookup_failure_rate = metric.getLookupFailureRate();
        mean_lookup_success_delay_millis = metric.getMeanLookupSuccessDelay(TimeUnit.MILLISECONDS);
        sent_bytes_per_second = metric.getSentBytesRatePerSecond();
        lookup_execution_rate_per_second = metric.getLookupExecutionRatePerSecond();
        served_next_hop_rate_per_second = metric.getServedNextHopRatePerSecond();
        rpc_error_rate_per_second = metric.getRPCErrorRatePerSecond();
    }

    public double getNormalizedMeanLookupSuccessDelayMillis() {

        return (double) mean_lookup_success_delay_millis / MAX_LOOKUP_DELAY_MILLIS;
    }

    public double getNormalizedSentBytesRatePerSecond() {

        return sent_bytes_per_second / MAX_SENT_BYTES_PER_SECOND;
    }

    public double getNormalizedLookupFailureRate() {

        return lookup_failure_rate / MAX_LOOKUP_FAILURE_RATE_PER_SECOND;
    }

    public double getUnreachableCount() {

        return 0;
    }

    public double getReachableCount() {

        return 0;
    }

    public double getSentBytesPerSecond() {

        return sent_bytes_per_second;
    }

    public double getLookupRatePerSecond() {

        return lookup_execution_rate_per_second;
    }

    public double getServedNextHopRatePerSecond() {

        return served_next_hop_rate_per_second;
    }

    @Override
    public double[] getPoint() {

        return new double[] {lookup_execution_rate_per_second, served_next_hop_rate_per_second, rpc_error_rate_per_second};
    }
}
