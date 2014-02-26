package uk.ac.standrews.cs.trombone.evaluation;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.io.FilenameUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.shabdiz.ApplicationDescriptor;
import uk.ac.standrews.cs.shabdiz.host.Host;
import uk.ac.standrews.cs.shabdiz.host.SSHHost;
import uk.ac.standrews.cs.shabdiz.job.Worker;
import uk.ac.standrews.cs.shabdiz.job.WorkerManager;
import uk.ac.standrews.cs.shabdiz.job.WorkerNetwork;
import uk.ac.standrews.cs.shabdiz.testing.junit.Retry;
import uk.ac.standrews.cs.shabdiz.util.Combinations;
import uk.ac.standrews.cs.shabdiz.util.Duration;
import uk.ac.standrews.cs.trombone.evaluation.scenarios.Constants;
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
public class BlubExperiment {

    private static final LinkedBlockingQueue<String> AVAILABLE_HOSTS = new LinkedBlockingQueue<>(BlubCluster.getNodeNames());
    private static WorkerNetwork network;
    private HashMap<Integer, String> host_indices;
    private static final Logger LOGGER = LoggerFactory.getLogger(BlubExperiment.class);
    private final Path events_zip;
    private final String scenario_name;
    private static final ReentrantLock lock = new ReentrantLock(true);
    private static final Semaphore semaphore = new Semaphore(48);

    private final List<ApplicationDescriptor> workers = new ArrayList<>();
    private final WorkerManager manager;

    @Rule
    public ExperimentWatcher watcher = new ExperimentWatcher();
    @Rule
    public Retry retry = new Retry(3);

    @Parameterized.Parameters(name = "{index} scenario: {0}")
    public static Collection<Object[]> data() {

        final String[] scenarios = ScenarioUtils.getResultsHome().toFile().list(new FilenameFilter() {

            @Override
            public boolean accept(final File dir, final String name) {

                return dir.isDirectory() && !name.startsWith(".");
            }
        });
        final List<String> scenarios_with_repetitions = new ArrayList<>();

        for (int i = 0; i < Constants.NUMBER_OF_REPETITIONS; i++) {
            for (int j = 0; j < scenarios.length; j++) {
                String directory = scenarios[j];
                scenarios_with_repetitions.add(directory);
            }
        }

        return Combinations.generateArgumentCombinations(new Object[][] {
                scenarios_with_repetitions.toArray()
        });
    }

    public BlubExperiment(String scenario_name) {

        this.scenario_name = scenario_name;
        events_zip = ScenarioUtils.getScenarioEventsPath(scenario_name);
        manager = network.getWorkerManager();
        assumeTrue("events of scenario " + scenario_name + " do not exists at " + events_zip, Files.isRegularFile(events_zip));

    }

    @BeforeClass
    public static void setUp() throws Exception {

        network = new WorkerNetwork();
        final WorkerManager manager = network.getWorkerManager();
        manager.setWorkerJVMArguments("-Xmx2G");
        manager.setWorkerDeploymentTimeout(new Duration(5, TimeUnit.MINUTES));

        network.addMavenDependency("uk.ac.standrews.cs.t3", "evaluation", "1.0-SNAPSHOT", "tests");
        //        network.addMavenDependency("uk.ac.standrews.cs.t3", "evaluation", "1.0-SNAPSHOT", null);
        //        network.addMavenDependency("ch.qos.logback", "logback-core", "1.1.1", null);
        //        network.addCurrentJVMClasspath();
        network.setAutoDeployEnabled(false);

    }

    @Before
    public void setup() throws Exception {

        semaphore.acquire();

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
        LOGGER.info("constructing workers across {} hosts", host_indices.size());

        for (String host_name : host_indices.values()) {
            final SSHHost host = new SSHHost(host_name, BlubCluster.getAuthMethod());

            final ApplicationDescriptor worker = new ApplicationDescriptor(host, manager);
            LOGGER.info("deploying worker on host {}, scenario {}", host_name, scenario_name);
            network.deploy(worker);
            workers.add(worker);
        }
    }

    @Test
    public void experiment() throws Exception {

        LOGGER.info("executing Experiment {}...", scenario_name);

        final Map<Host, Future<String>> host_event_executions = new HashMap<>();

        for (ApplicationDescriptor descriptor : workers) {

            final Worker worker = descriptor.getApplicationReference();
            final Host host = descriptor.getHost();
            final int host_index = getHostIndexByName(host.getName());

            LOGGER.info("submitting job to {} indexed as {}", host, host_index);
            final Future<String> future_event_execution = worker.submit(new BlubEventExecutionJob(scenario_name, host_index, host_indices));
            host_event_executions.put(host, future_event_execution);
        }

        final Path repetitions = ScenarioUtils.getScenarioRepetitionsHome(scenario_name);
        BlubEventExecutionJob.assureRepetitionsDirectoryExists(repetitions);
        final Path observations = BlubEventExecutionJob.newObservationsPath(repetitions);
        LOGGER.info("collected observations will be stored at {}", observations.toAbsolutePath());

        Exception error = null;

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
                    final Throwable cause = e.getCause();
                    LOGGER.error("Event execution on host {} failed due to {}", host, cause != null ? cause : e);
                    LOGGER.error("Failure details", cause != null ? cause : e);
                    error = e;
                    break;
                }
            }
        }
        if (error != null) {
            Files.deleteIfExists(observations);
            throw error;
        }
    }

    @After
    public void tearDown() throws Exception {

        LOGGER.info("tearing down...");
        killAllJavaProcessesOnHosts();
        killAndRemoveWorkers();
        returnHosts();

        LOGGER.info("shutting down worker network");
        LOGGER.info("Done");
        semaphore.release();
    }

    private void returnHosts() throws InterruptedException {

        for (String host_name : host_indices.values()) {
            AVAILABLE_HOSTS.put(host_name);
        }
    }

    private void killAndRemoveWorkers() {

        for (ApplicationDescriptor worker : workers) {

            try {
                network.kill(worker);
            }
            catch (Exception e) {
                LOGGER.trace("failed to kill worker on {} used in scenario {} due to ", worker.getHost().getName(), scenario_name, e);
            }
            finally {
                network.remove(worker);
            }
        }
    }

    private void killAllJavaProcessesOnHosts() {

        for (ApplicationDescriptor descriptor : network) {
            final Host host = descriptor.getHost();
            try {
                final Process killall_java = host.execute("killall java");
                killall_java.waitFor();
                killall_java.destroy();
            }
            catch (IOException | InterruptedException e) {
                LOGGER.trace("failed to kill all java processes on host" + host, e);
            }
        }
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
