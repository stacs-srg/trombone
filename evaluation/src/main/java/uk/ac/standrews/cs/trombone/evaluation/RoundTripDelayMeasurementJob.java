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
 * Measures the round trip delay from this host to a set of given hosts in nanoseconds.
 *
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class RoundTripDelayMeasurementJob implements Job<HashSet<Statistics>> {

    private static final long serialVersionUID = 216423340101673216L;
    private static final Logger LOGGER = LoggerFactory.getLogger(RoundTripDelayMeasurementJob.class);
    private final HashSet<String> host_names;
    private final long ping_interval_millis;
    private final int ping_count;
    private final int ping_timeout_millis;

    public RoundTripDelayMeasurementJob(HashSet<String> host_names, Duration ping_interval, int ping_count, Duration ping_timeout) {

        if (ping_count < 1) { throw new IllegalArgumentException("ping count must be at least 1"); }

        this.host_names = host_names;
        this.ping_count = ping_count;
        ping_interval_millis = ping_interval.getLength(TimeUnit.MILLISECONDS);
        ping_timeout_millis = (int) ping_timeout.getLength(TimeUnit.MILLISECONDS);
    }

    @Override
    public HashSet<Statistics> call() throws Exception {

        final Map<InetAddress, Statistics> host_statistics = initHostStatistics();

        for (int i = 0; i < ping_count; i++) {

            Thread.sleep(ping_interval_millis);
            pingHosts(host_statistics);
        }

        return new HashSet<>(host_statistics.values());
    }

    private void pingHosts(final Map<InetAddress, Statistics> host_statistics) throws IOException {

        for (Map.Entry<InetAddress, Statistics> host_statistics_entry : host_statistics.entrySet()) {

            final InetAddress host = host_statistics_entry.getKey();
            final Statistics statistics = host_statistics_entry.getValue();
            pingHost(host, statistics);
        }
    }

    private void pingHost(final InetAddress host, final Statistics statistics) throws IOException {

        final long now = System.nanoTime();
        if (host.isReachable(ping_timeout_millis)) {
            final long elapsed = System.nanoTime() - now;
            statistics.addSample(elapsed);
        }
        else {
            LOGGER.debug("host {} did not appear to be reachable", host);
        }
    }

    private Map<InetAddress, Statistics> initHostStatistics() throws UnknownHostException {

        final Map<InetAddress, Statistics> host_statistics = new HashMap<>();
        for (String host_name : host_names) {
            host_statistics.put(InetAddress.getByName(host_name), new Statistics());
        }
        return host_statistics;
    }
}
