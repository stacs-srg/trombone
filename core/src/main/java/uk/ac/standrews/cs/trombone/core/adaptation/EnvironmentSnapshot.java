package uk.ac.standrews.cs.trombone.core.adaptation;

import java.util.concurrent.TimeUnit;
import uk.ac.standrews.cs.trombone.core.PeerMetric;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class EnvironmentSnapshot {

    private static final long MAX_LOOKUP_DELAY_MILLIS = TimeUnit.MILLISECONDS.convert(1, TimeUnit.MINUTES);
    private static final double MAX_SENT_BYTES_PER_SECOND = 1000 * 5;
    private static final double MAX_LOOKUP_FAILURE_PER_SECOND = 2000;
    private final double lookup_failure_rate;
    private final long mean_lookup_success_delay_millis;
    private final double sent_bytes_per_second;
    private double number_of_lookups_executed;
    private double number_of_nexthop_answered;

    public EnvironmentSnapshot(PeerMetric metric) {

        lookup_failure_rate = metric.getLookupFailureRate();
        mean_lookup_success_delay_millis = metric.getMeanLookupSuccessDelay(TimeUnit.MILLISECONDS);
        sent_bytes_per_second = metric.getSentBytesRatePerSecond();
        number_of_lookups_executed = metric.getNumberOfExecutedLookups();
        number_of_nexthop_answered = metric.getNumberOfServedNextHops();
    }

    public double getNormalizedMeanLookupSuccessDelayMillis() {

        return (double) mean_lookup_success_delay_millis / MAX_LOOKUP_DELAY_MILLIS;
    }

    public double getNormalizedSentBytesRatePerSecond() {

        return sent_bytes_per_second / MAX_SENT_BYTES_PER_SECOND;
    }

    public double getNormalizedLookupFailureRate() {

        return lookup_failure_rate / MAX_LOOKUP_FAILURE_PER_SECOND;
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

    public double getLookupCount() {

        return number_of_lookups_executed;
    }

    public double getNextHopCount() {

        return number_of_nexthop_answered;
    }
}
