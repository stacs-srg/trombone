package uk.ac.standrews.cs.trombone.evaluation;

import java.io.File;
import java.nio.file.FileSystem;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
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

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
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

    protected Experiment(String events_path, String observations_path) {

        this.events_path = events_path;
        this.observations_path = observations_path;
        events = new File(events_path);
    }

    public final void run() throws Exception {

        LOGGER.info("Setting up to execute events at {}", events_path);
        LOGGER.info("Observations to be stored at {}", observations_path);
        System.out.println(events.exists());
        setup();

        try {
            LOGGER.info("Executing Experiment...");
            execute();
        }
        finally {
            tearDown();
        }
    }

    protected void setup() throws Exception {

        events_file_system = ZipFileSystemUtils.newZipFileSystem(events_path, false);
        host_indices = EventReader.readHostNames(events_file_system.getPath("hosts.csv"));
        scenario_properties = EventReader.readScenarioProperties(events_file_system.getPath("/"));
        scenario_name = scenario_properties.getProperty("scenario.name");
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

    protected void tearDown() throws Exception {

        events_file_system.close();
        worker_network.shutdown();
    }

    private static boolean isLocalhost(final String host_name) {

        return host_name.equals(LOCALHOST);
    }

    private int getHostIndexByName(final String host_name) {

        return host_indices.get(host_name);
    }

    private void execute() throws Exception {

        final Map<Host, Future<String>> host_event_executions = new HashMap<>();

        for (ApplicationDescriptor descriptor : worker_network) {

            final Worker worker = descriptor.getApplicationReference();
            final Host host = descriptor.getHost();
            final int host_index = getHostIndexByName(host.getName());
            final Future<String> future_event_execution = worker.submit(new EventExecutionJob(events_path, host_index, observations_path));
            host_event_executions.put(host, future_event_execution);
        }

        for (Map.Entry<Host, Future<String>> host_event_entry : host_event_executions.entrySet()) {

            final Host host = host_event_entry.getKey();
            final Future<String> future_event_execution = host_event_entry.getValue();

            try {
                final String results_path = future_event_execution.get();
                LOGGER.info("successfully finished executing events on host {} - {}", host, results_path);
                final File destination = new File(events.getParentFile(), host.getName());
                destination.mkdirs();
                host.download(observations_path, destination);
                LOGGER.info("downloaded observations from host {} to {}", host.getName(), destination);
            }
            catch (InterruptedException | ExecutionException e) {
                LOGGER.error("Event execution on host {} failed due to {}", host, e);
                LOGGER.error("Failure details", e);
            }
        }
    }
}
