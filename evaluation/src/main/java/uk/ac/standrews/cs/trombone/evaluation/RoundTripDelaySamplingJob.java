package uk.ac.standrews.cs.trombone.evaluation;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.mashti.sina.distribution.statistic.Statistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.shabdiz.job.Job;
import uk.ac.standrews.cs.shabdiz.util.Duration;

/**
 * Samples the round trip delay from this host to a set of given host names in nanoseconds using {@link InetAddress#isReachable(int)} for a number of times.
 *
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class RoundTripDelaySamplingJob implements Job<HashMap<InetAddress, Statistics>> {

    private static final long serialVersionUID = 4397208965390000447L;
    private static final Logger LOGGER = LoggerFactory.getLogger(RoundTripDelaySamplingJob.class);
    private final HashSet<String> host_names;
    private final long ping_interval_millis;
    private final int ping_count;
    private final int ping_timeout_millis;

    public RoundTripDelaySamplingJob(HashSet<String> host_names, Duration ping_interval, int ping_count, Duration ping_timeout) {

        if (ping_count < 1) { throw new IllegalArgumentException("ping count must be at least 1"); }

        this.host_names = host_names;
        this.ping_count = ping_count;
        ping_interval_millis = ping_interval.getLength(TimeUnit.MILLISECONDS);
        ping_timeout_millis = (int) ping_timeout.getLength(TimeUnit.MILLISECONDS);
    }

    @Override
    public HashMap<InetAddress, Statistics> call() throws InterruptedException, IOException {

        final HashMap<InetAddress, Statistics> host_statistics = initHostStatistics();

        for (int i = 0; i < ping_count; i++) {

            Thread.sleep(ping_interval_millis);
            updateRoundTripDelaySamples(host_statistics);
        }

        return host_statistics;
    }

    private HashMap<InetAddress, Statistics> initHostStatistics() throws UnknownHostException {

        final HashMap<InetAddress, Statistics> host_statistics = new HashMap<>();
        for (String host_name : host_names) {
            host_statistics.put(InetAddress.getByName(host_name), new Statistics());
        }
        return host_statistics;
    }

    private void updateRoundTripDelaySamples(final Map<InetAddress, Statistics> host_statistics) throws IOException {

        for (Map.Entry<InetAddress, Statistics> host_statistics_entry : host_statistics.entrySet()) {

            final InetAddress host = host_statistics_entry.getKey();
            final Statistics statistics = host_statistics_entry.getValue();
            updateRoundTripDelaySample(host, statistics);
        }
    }

    private void updateRoundTripDelaySample(final InetAddress host, final Statistics statistics) throws IOException {

        final long now = System.nanoTime();
        if (host.isReachable(ping_timeout_millis)) {
            final long elapsed = System.nanoTime() - now;
            statistics.addSample(elapsed);
        }
        else {
            LOGGER.debug("host {} does not appear to be reachable. Skipped from delay samples", host);
        }
    }
}
