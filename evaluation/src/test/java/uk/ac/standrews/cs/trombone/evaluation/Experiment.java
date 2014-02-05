package uk.ac.standrews.cs.trombone.evaluation;

import java.io.File;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.commons.io.FilenameUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.shabdiz.ApplicationDescriptor;
import uk.ac.standrews.cs.shabdiz.ApplicationState;
import uk.ac.standrews.cs.shabdiz.host.Host;
import uk.ac.standrews.cs.shabdiz.host.LocalHost;
import uk.ac.standrews.cs.shabdiz.host.SSHHost;
import uk.ac.standrews.cs.shabdiz.host.exec.Commands;
import uk.ac.standrews.cs.shabdiz.job.Worker;
import uk.ac.standrews.cs.shabdiz.job.WorkerNetwork;
import uk.ac.standrews.cs.trombone.evaluation.util.BlubHostProvider;
import uk.ac.standrews.cs.trombone.evaluation.util.ZipFileSystemUtils;
import uk.ac.standrews.cs.trombone.event.EventReader;

import static org.junit.Assert.assertTrue;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
@Category(Experiment.class)
public class Experiment {

    private static final Logger LOGGER = LoggerFactory.getLogger(Experiment.class);
    private static final String LOCALHOST = "localhost";
    private final String events_path;
    private final String observations_path;
    private final File events;
    private WorkerNetwork worker_network;
    private Map<String, Integer> host_indices;
    private Properties scenario_properties;
    private String scenario_name;
    private FileSystem events_file_system;
    private ExecutorService executor;

    protected Experiment(String events_path, String observations_path) {

        this.events_path = events_path;
        this.observations_path = observations_path;
        events = new File(events_path);
    }

    @Before
    public void setup() throws Exception {

        assertTrue(events.isFile());

        LOGGER.info("Setting up to execute events at {}", events_path);
        executor = Executors.newFixedThreadPool(100);
        events_file_system = ZipFileSystemUtils.newZipFileSystem(events_path, false);
        host_indices = EventReader.readHostNames(events_file_system.getPath("hosts.csv"));
        scenario_properties = EventReader.readScenarioProperties(events_file_system.getPath("/"));
        scenario_name = scenario_properties.getProperty("scenario.name");
        LOGGER.info("scenario: {}", scenario_name);

        LOGGER.info("constructing worker network across {} hosts", host_indices.size());
        worker_network = new WorkerNetwork();

        for (String host_name : host_indices.keySet()) {
            final Host host = isLocalhost(host_name) ? new LocalHost() : new SSHHost(host_name, BlubHostProvider.SSHJ_AUTH);
            worker_network.add(host);
            LOGGER.info("uploading events to {}", host_name);
            final String parent = "/state/partition1/trombone/" + scenario_name;
            final Process mkdir = host.execute(Commands.MAKE_DIRECTORIES.get(host.getPlatform(), parent));
            mkdir.waitFor();
            mkdir.destroy();
            host.upload(events, parent);
        }
        worker_network.getWorkerManager().setWorkerJVMArguments("-Xmx1G");
        worker_network.addCurrentJVMClasspath();
        worker_network.deployAll();
        worker_network.awaitAnyOfStates(ApplicationState.RUNNING);
    }

    @Test
    public void doExperiment() throws Exception {

        LOGGER.info("Executing Experiment...");
        final Map<Host, Future<String>> host_event_executions = new HashMap<>();

        for (ApplicationDescriptor descriptor : worker_network) {

            final Worker worker = descriptor.getApplicationReference();
            final Host host = descriptor.getHost();
            final int host_index = getHostIndexByName(host.getName());
            final Future<String> future_event_execution = worker.submit(new EventExecutionJob("/state/partition1/trombone/" + scenario_name + "/events.zip", host_index, "/state/partition1/trombone/" + scenario_name + "/repetitions"));
            host_event_executions.put(host, future_event_execution);
        }

        final String observations_file_name = EventExecutionJob.DATE_FORMAT.format(new Date());

        try (FileSystem observations = ZipFileSystemUtils.newZipFileSystem("results/" + scenario_name + "/repetitions/" + observations_file_name + ".zip", true)) {

            final Path root_observations = observations.getPath("/");

            for (Map.Entry<Host, Future<String>> host_event_entry : host_event_executions.entrySet()) {

                final Host host = host_event_entry.getKey();
                final Future<String> future_event_execution = host_event_entry.getValue();

                try {
                    final String results_path = future_event_execution.get();
                    LOGGER.info("successfully finished executing events on host {} - {}", host, results_path);
                    final File destination = new File(events.getParentFile(), host.getName());
                    destination.mkdirs();
                    host.download(results_path, destination);
                    LOGGER.info("downloaded observations from host {} to {}", host.getName(), destination);
                    File zip = new File(destination, FilenameUtils.getName(results_path));

                    final int host_index = getHostIndexByName(host.getName());

                    final Path local_observations = root_observations.resolve(String.valueOf(host_index));
                    Files.createDirectories(local_observations);

                    try (FileSystem fileSystem = ZipFileSystemUtils.newZipFileSystem(zip.getAbsolutePath(), false)) {
                        ZipFileSystemUtils.copyRecursively(fileSystem.getPath("/"), local_observations);
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

        LOGGER.info("shutting down executor service");
        executor.shutdownNow();
        LOGGER.info("closing events file system");
        events_file_system.close();
        LOGGER.info("shutting down worker network");
        worker_network.shutdown();
        LOGGER.info("Done");
    }

    private static boolean isLocalhost(final String host_name) {

        return host_name.equals(LOCALHOST);
    }

    private int getHostIndexByName(final String host_name) {

        return host_indices.get(host_name);
    }
}
