package uk.ac.standrews.cs.trombone.evaluation;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.shabdiz.job.Job;
import uk.ac.standrews.cs.shabdiz.util.Duration;
import uk.ac.standrews.cs.trombone.evaluation.util.ScenarioUtils;
import uk.ac.standrews.cs.trombone.event.EventExecutor;
import uk.ac.standrews.cs.trombone.event.EventQueue;
import uk.ac.standrews.cs.trombone.event.Scenario;

import static uk.ac.standrews.cs.trombone.evaluation.BlubBatchEventUpload.BLUB_NODE_RESULTS_HOME;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class BlubUnzipEventExecutionJob implements Job<String> {

    private static final long serialVersionUID = 2675891974884649473L;
    private static final Logger LOGGER = LoggerFactory.getLogger(BlubUnzipEventExecutionJob.class);
    static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS");
    private final String scenario_name;
    private final int host_index;
    private final HashMap<Integer, String> host_indices;
    private final String results_home_path;

    public BlubUnzipEventExecutionJob(String scenario_name, int host_index) {

        this(scenario_name, host_index, null);
    }

    public BlubUnzipEventExecutionJob(String scenario_name, int host_index, HashMap<Integer, String> host_indices) {

        this(scenario_name, host_index, host_indices, BLUB_NODE_RESULTS_HOME);
    }

    public BlubUnzipEventExecutionJob(String scenario_name, int host_index, HashMap<Integer, String> host_indices, Path results_home) {

        this.scenario_name = scenario_name;
        this.host_index = host_index;
        this.host_indices = host_indices;
        results_home_path = results_home.toAbsolutePath().toString();
    }

    @Override
    public String call() throws Exception {

        final Path results_home = Paths.get(results_home_path);
        LOGGER.info("results home is set to {}", results_home);
        LOGGER.info("preparing to execute events for scenario {} with host index {}", scenario_name, host_index);

        final Path repetitions = results_home.resolve(ScenarioUtils.getScenarioRepetitionsHome(scenario_name));
        assureDirectoryExists(repetitions);
        LOGGER.info("repetitions directory: {}", repetitions);

        final Path observations = newObservationsPath(repetitions);
        assureDirectoryExists(observations);
        LOGGER.info("observations to be stored at: {}", observations);

        final Scenario scenario = getScenarioByName(scenario_name);
        ScenarioUtils.saveScenarioAsJson(scenario, observations);

        final EventQueue event_queue = new EventQueue(scenario, host_index, host_indices);
        final EventExecutor event_executor = new EventExecutor(event_queue, observations);
        boolean failed = false;
        try {
            LOGGER.info("starting event executor...");
            event_executor.start();

            final Duration experiment_duration = event_executor.getExperimentDuration();
            final Duration await_timeout = experiment_duration.convertTo(TimeUnit.MINUTES).add(new Duration(20, TimeUnit.MINUTES));

            LOGGER.info("awaiting event execution completion with timeout {}...", await_timeout);
            event_executor.awaitCompletion(await_timeout.getLength(), await_timeout.getTimeUnit());
        }
        catch (Throwable e) {
            LOGGER.error("experiment with scenario {} on host {} failed due to {}", scenario_name, InetAddress.getLocalHost().getHostName(), e);
            LOGGER.error("experiment failure", e);
            failed = true;
        }
        finally {
            LOGGER.info("shutting down the event executor...");
            event_executor.shutdown();
        }

        LOGGER.info("finished executing events of scenario {} with host index {}", scenario_name, host_index);
        LOGGER.info("observations are stored at {}", observations);

        copyLog(observations);

        LOGGER.info("compressing observations...");
        final Path compressed_observations = getCompressedObservationsPath(failed, observations);
        ScenarioUtils.compressDirectoryRecursively(observations, compressed_observations);
        LOGGER.info("compressed observations at {}", compressed_observations.toAbsolutePath());
        return compressed_observations.toString();
    }

    private static Path getCompressedObservationsPath(final boolean failed, Path observations) {

        final String zip_name = observations.getFileName() + (failed ? "_FAILED" : "") + ".zip";
        return observations.getParent().resolve(zip_name);
    }

    private static void copyLog(final Path observations) throws IOException {

        final Path log = Paths.get("results", "experiments.log");
        if (Files.isRegularFile(log)) {
            Files.copy(log, observations.resolve("experiments.log"));
        }
    }

    private static Scenario getScenarioByName(final String scenario_name) {

        for (Scenario scenario : BatchEventGenerator.SCENARIOS) {
            if (scenario.getName().equals(scenario_name)) {
                return scenario;
            }
        }
        throw new NoSuchElementException("cannot find scenario with name " + scenario_name);
    }

    static synchronized Path newObservationsPath(final Path repetitions) {

        return repetitions.resolve(DATE_FORMAT.format(new Date()));
    }

    static void assureDirectoryExists(final Path repetitions) throws IOException {

        if (!Files.isDirectory(repetitions)) {
            Files.createDirectories(repetitions);
        }
    }
}
