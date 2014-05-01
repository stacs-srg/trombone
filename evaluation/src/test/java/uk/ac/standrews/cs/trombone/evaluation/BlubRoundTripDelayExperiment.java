package uk.ac.standrews.cs.trombone.evaluation;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
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
import uk.ac.standrews.cs.shabdiz.host.Host;
import uk.ac.standrews.cs.shabdiz.job.Worker;
import uk.ac.standrews.cs.shabdiz.job.WorkerNetwork;
import uk.ac.standrews.cs.shabdiz.util.Duration;
import uk.ac.standrews.cs.trombone.evaluation.util.BlubCluster;
import uk.ac.standrews.cs.trombone.evaluation.util.MeanRoundTripDelaySamplingJob;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class BlubRoundTripDelayExperiment {

    public static final Duration PING_TIMEOUT = new Duration(5, TimeUnit.SECONDS);
    public static final Duration PING_INTERVAL = new Duration(10, TimeUnit.SECONDS);

    public static final int PING_COUNT = 200;
    private static final Logger LOGGER = LoggerFactory.getLogger(BlubRoundTripDelayExperiment.class);
    private WorkerNetwork network;

    @Before
    public void setup() throws Exception {

        LOGGER.info("Instantiating a worker network across Blub cluster...");
        network = new WorkerNetwork();
        LOGGER.info("Adding nodes...");
        for (Host host : BlubCluster.getHosts()) {
            LOGGER.info("Added {}", host);
        }

        network.addMavenDependency("uk.ac.standrews.cs.t3", "evaluation", "1.0-SNAPSHOT", "tests");
        network.addMavenDependency("uk.ac.standrews.cs.t3", "evaluation", "1.0-SNAPSHOT", null);

        network.getWorkerManager().setWorkerDeploymentTimeout(new Duration(1, TimeUnit.MINUTES));
        LOGGER.info("Deploying worker network...");

        network.deployAll();

        LOGGER.info("Awaiting RUNNING state...");
        network.awaitAnyOfStates(ApplicationState.RUNNING);
    }

    @Test
    public void testRTD() throws Exception {

        final Map<String, Future<Double>> future_results = new HashMap<>();
        LOGGER.info("Submitting RTD jobs to workers...");
        for (ApplicationDescriptor descriptor : network) {

            final TreeSet<String> host_names = new TreeSet<>(BlubCluster.getNodeNames());
            final String host_name = descriptor.getHost().getName();
            host_names.remove(host_name);
            final MeanRoundTripDelaySamplingJob job = new MeanRoundTripDelaySamplingJob(host_names, PING_INTERVAL, PING_COUNT, PING_TIMEOUT);
            final Worker worker = descriptor.getApplicationReference();
            final Future<Double> future_ping_result = worker.submit(job);
            future_results.put(host_name, future_ping_result);
            LOGGER.info("Submitted RTD job to {}", host_name);
        }

        LOGGER.info("Awaiting results...");
        Statistics statistics = new Statistics();
        for (Map.Entry<String, Future<Double>> future_ping_result : future_results.entrySet()) {
            final Double mean = future_ping_result.getValue().get();
            LOGGER.info("Job is done on {} with {} records", future_ping_result.getKey(), mean);

            if (!mean.isNaN()) {
                statistics.addSample(mean);
                System.out.println(mean);
            }
            else {
                LOGGER.warn("NaN mean");
            }
        }
        LOGGER.info("Round trip delay statistics: {}", statistics);
        LOGGER.info("Done");
    }

    @After
    public void tearDown() throws Exception {

        network.shutdown();
    }
}
