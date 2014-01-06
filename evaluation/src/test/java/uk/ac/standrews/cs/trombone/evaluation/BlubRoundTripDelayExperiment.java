package uk.ac.standrews.cs.trombone.evaluation;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mashti.sina.distribution.statistic.Statistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.shabdiz.ApplicationDescriptor;
import uk.ac.standrews.cs.shabdiz.ApplicationState;
import uk.ac.standrews.cs.shabdiz.host.SSHHost;
import uk.ac.standrews.cs.shabdiz.job.Worker;
import uk.ac.standrews.cs.shabdiz.job.WorkerNetwork;
import uk.ac.standrews.cs.shabdiz.util.Duration;
import uk.ac.standrews.cs.trombone.evaluation.util.BlubHostProvider;
import uk.ac.standrews.cs.trombone.evaluation.util.RoundTripDelaySamplingJob;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class BlubRoundTripDelayExperiment {

    public static final Duration PING_TIMEOUT = new Duration(5, TimeUnit.SECONDS);
    public static final Duration PING_INTERVAL = new Duration(10, TimeUnit.SECONDS);
    public static final HashSet<String> BLUB_NODE_NAMES = new HashSet<>();
    public static final int PING_COUNT = 100;
    private static final Logger LOGGER = LoggerFactory.getLogger(BlubRoundTripDelayExperiment.class);
    private WorkerNetwork network;

    @Before
    public void setup() throws Exception {

        LOGGER.info("Instantiating a worker network across Blub cluster...");
        network = new WorkerNetwork();
        LOGGER.info("\tAdding nodes...");
        for (int i = 0; i < 48; i++) {
            network.add(new SSHHost("compute-0-" + i, BlubHostProvider.SSHJ_AUTH));
        }

        network.addMavenDependency("uk.ac.st-andrews.cs.t3", "evaluation", "1.0-SNAPSHOT", "tests");
        LOGGER.info("\tDeploying worker network...");
        network.deployAll();
        LOGGER.info("\tAwaiting RUNNING state...");
        network.awaitAnyOfStates(ApplicationState.RUNNING);
    }

    @After
    public void tearDown() throws Exception {

        network.shutdown();
    }

    @Test
    protected void run() throws Exception {

        final RoundTripDelaySamplingJob job = new RoundTripDelaySamplingJob(BLUB_NODE_NAMES, PING_INTERVAL, PING_COUNT, PING_TIMEOUT);
        Map<String, Future<HashMap<InetAddress, Statistics>>> future_results = new HashMap<>();

        LOGGER.info("Submitting RTD jobs to workers...");

        for (ApplicationDescriptor descriptor : network) {

            final Worker worker = descriptor.getApplicationReference();
            final String host_name = descriptor.getHost().getName();
            final Future<HashMap<InetAddress, Statistics>> future_ping_result = worker.submit(job);
            future_results.put(host_name, future_ping_result);
            LOGGER.info("\tSubmitted RTD job to {}", host_name);
        }

        LOGGER.info("Awaiting results...");
        for (Map.Entry<String, Future<HashMap<InetAddress, Statistics>>> future_ping_result : future_results.entrySet()) {
            final HashMap<InetAddress, Statistics> result = future_ping_result.getValue().get();
            LOGGER.info("\tJob is done on {} with {} records", future_ping_result.getKey(), result.size());
        }
        LOGGER.info("Done");

    }
}
