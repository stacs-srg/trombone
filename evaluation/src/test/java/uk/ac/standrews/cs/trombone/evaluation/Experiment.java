package uk.ac.standrews.cs.trombone.evaluation;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.io.FilenameUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.shabdiz.ApplicationDescriptor;
import uk.ac.standrews.cs.shabdiz.ApplicationState;
import uk.ac.standrews.cs.shabdiz.host.Host;
import uk.ac.standrews.cs.shabdiz.host.SSHHost;
import uk.ac.standrews.cs.shabdiz.job.Worker;
import uk.ac.standrews.cs.shabdiz.job.WorkerNetwork;
import uk.ac.standrews.cs.shabdiz.util.Combinations;
import uk.ac.standrews.cs.trombone.evaluation.util.BlubCluster;
import uk.ac.standrews.cs.trombone.evaluation.util.ExperimentWatcher;
import uk.ac.standrews.cs.trombone.evaluation.util.FileSystemUtils;
import uk.ac.standrews.cs.trombone.evaluation.util.ScenarioUtils;
import uk.ac.standrews.cs.trombone.event.EventReader;

import static org.junit.Assume.assumeTrue;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
@RunWith(Parallelized.class)
public class Experiment {

    private static final LinkedBlockingQueue<String> AVAILABLE_HOSTS = new LinkedBlockingQueue<>(BlubCluster.getBlubNodeHostNames());
    private WorkerNetwork worker_network;
    private Map<Integer, String> host_indices;
    private static final Logger LOGGER = LoggerFactory.getLogger(Experiment.class);
    private final Path events_zip;
    private final String scenario_name;
    private static final ReentrantLock lock = new ReentrantLock(true);

    @Rule
    public ExperimentWatcher watcher = new ExperimentWatcher();

    @Parameterized.Parameters(name = "{index} scenario: {0}")
    public static Collection<Object[]> data() {

        return Combinations.generateArgumentCombinations(new Object[][] {
                ScenarioUtils.getResultsHome().toFile().list(new FilenameFilter() {

                    @Override
                    public boolean accept(final File dir, final String name) {

                        return dir.isDirectory() && !name.startsWith(".");
                    }
                })
        });
    }

    public Experiment(String scenario_name) {

        this.scenario_name = scenario_name;
        events_zip = ScenarioUtils.getScenarioEventsPath(scenario_name);
        assumeTrue("events of scenario " + scenario_name + " do not exists at " + events_zip, Files.isRegularFile(events_zip));
    }

    @Before
    public void setup() throws Exception {

        lock.lock();
        try {
            LOGGER.info("Setting up to execute events of scenario {}", scenario_name);
            retrieveHostIndices();
            adjustHostNamesByAvailability();
        }
        finally {
            lock.unlock();
        }

        LOGGER.info("preparing to execute scenario {}", scenario_name);
        LOGGER.info("constructing worker network across {} hosts", host_indices.size());

        worker_network = new WorkerNetwork();
        for (String host_name : host_indices.values()) {
            worker_network.add(new SSHHost(host_name, BlubCluster.getAuthMethod()));
        }
        worker_network.getWorkerManager().setWorkerJVMArguments("-Xmx1G");
        worker_network.addCurrentJVMClasspath();

        LOGGER.info("deploying worker network...");
        worker_network.deployAll();

        LOGGER.info("awaiting RUNNING state...");
        worker_network.awaitAnyOfStates(ApplicationState.RUNNING);
    }

    @Test
    public void experiment() throws Exception {

        LOGGER.info("executing Experiment...");

        final Map<Host, Future<String>> host_event_executions = new HashMap<>();

        for (ApplicationDescriptor descriptor : worker_network) {

            final Worker worker = descriptor.getApplicationReference();
            final Host host = descriptor.getHost();
            final int host_index = getHostIndexByName(host.getName());

            LOGGER.info("submitting job to {} indexed as {}", host, host_index);
            final Future<String> future_event_execution = worker.submit(new BlubEventExecutionJob(scenario_name, host_index));
            host_event_executions.put(host, future_event_execution);
        }

        final Path repetitions = ScenarioUtils.getScenarioRepetitionsHome(scenario_name);
        BlubEventExecutionJob.assureRepetitionsDirectoryExists(repetitions);
        final Path observations = BlubEventExecutionJob.newObservationsPath(repetitions);
        LOGGER.info("collected observations will be stored at {}", observations.toAbsolutePath());

        try (FileSystem observations_fs = FileSystemUtils.newZipFileSystem(observations, true)) {

            final Path root_observations = observations_fs.getPath(observations_fs.getSeparator());
            for (Map.Entry<Host, Future<String>> host_event_entry : host_event_executions.entrySet()) {

                final Host host = host_event_entry.getKey();
                final Future<String> future_event_execution = host_event_entry.getValue();

                try {

                    final String results_path = future_event_execution.get();
                    LOGGER.info("successfully finished executing events on host {} - {}", host, results_path);

                    final Path destination = Files.createTempDirectory(host.getName());
                    host.download(results_path, destination.toFile());

                    LOGGER.info("downloaded observations from host {} to {}", host.getName(), destination);

                    final File zip = new File(destination.toFile(), FilenameUtils.getName(results_path));
                    final int host_index = getHostIndexByName(host.getName());
                    final Path local_observations = root_observations.resolve(String.valueOf(host_index));

                    Files.createDirectories(local_observations);

                    try (final FileSystem fileSystem = FileSystemUtils.newZipFileSystem(zip.getAbsolutePath(), false)) {
                        FileSystemUtils.copyRecursively(fileSystem.getPath("/"), local_observations);
                    }
                }
                catch (InterruptedException | ExecutionException e) {
                    LOGGER.error("Event execution on host {} failed due to {}", host, e);
                    LOGGER.error("Failure details", e);
                }
            }
        }
    }

    @After
    public void tearDown() throws Exception {

        LOGGER.info("tearing down...");

        for (String host_name : host_indices.values()) {
            AVAILABLE_HOSTS.put(host_name);
        }
        LOGGER.info("shutting down worker network");
        worker_network.shutdown();
        LOGGER.info("Done");
    }

    private void adjustHostNamesByAvailability() throws InterruptedException {

        LOGGER.info("require {} available hosts to proceed", host_indices.size());

        for (final Map.Entry<Integer, String> entry : host_indices.entrySet()) {
            final String available_host_name = AVAILABLE_HOSTS.take();
            final String actual_host_name = entry.setValue(available_host_name);
            LOGGER.info("substituted host {}  for {} indexed as {}", available_host_name, actual_host_name, entry.getKey());
        }
    }

    private void retrieveHostIndices() throws IOException {

        LOGGER.info("retrieving host indices from {}", events_zip.toAbsolutePath());
        try (FileSystem events_file_system = FileSystemUtils.newZipFileSystem(events_zip, false)) {
            host_indices = EventReader.readHostIndices(events_file_system.getPath("hosts.csv"));
        }
    }

    private Integer getHostIndexByName(final String host_name) {

        for (Map.Entry<Integer, String> entry : host_indices.entrySet()) {
            if (entry.getValue().equals(host_name)) {
                return entry.getKey();
            }
        }
        return null;
    }
}
