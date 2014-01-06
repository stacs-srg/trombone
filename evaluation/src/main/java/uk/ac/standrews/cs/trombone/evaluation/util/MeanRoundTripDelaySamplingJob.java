package uk.ac.standrews.cs.trombone.evaluation.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.TreeSet;
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
public class MeanRoundTripDelaySamplingJob implements Job<Double> {

    private static final long serialVersionUID = -3428156215304274121L;
    private static final Logger LOGGER = LoggerFactory.getLogger(MeanRoundTripDelaySamplingJob.class);
    private final TreeSet<String> host_names;
    private final long ping_interval_millis;
    private final int ping_count;
    private final int ping_timeout_millis;

    public MeanRoundTripDelaySamplingJob(TreeSet<String> host_names, Duration ping_interval, int ping_count, Duration ping_timeout) {

        if (ping_count < 1) { throw new IllegalArgumentException("ping count must be at least 1"); }

        this.host_names = host_names;
        this.ping_count = ping_count;
        ping_interval_millis = ping_interval.getLength(TimeUnit.MILLISECONDS);
        ping_timeout_millis = (int) ping_timeout.getLength(TimeUnit.MILLISECONDS);
    }

    @Override
    public Double call() throws InterruptedException, IOException {

        final TreeSet<InetAddress> addresses = initHostAddresses();
        final Statistics statistics = new Statistics();
        for (int i = 0; i < ping_count; i++) {

            Thread.sleep(ping_interval_millis);
            updateRoundTripDelaySamples(addresses, statistics);
        }

        return statistics.getMean().doubleValue();
    }

    private TreeSet<InetAddress> initHostAddresses() throws UnknownHostException {

        final TreeSet<InetAddress> addresses = new TreeSet<>();
        for (String host_name : host_names) {
            addresses.add(InetAddress.getByName(host_name));
        }
        return addresses;
    }

    private void updateRoundTripDelaySamples(final TreeSet<InetAddress> hosts, Statistics statistics) throws IOException {

        for (InetAddress address : hosts) {
            updateRoundTripDelaySample(address, statistics);
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
