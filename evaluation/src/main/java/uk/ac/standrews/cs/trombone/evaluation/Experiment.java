package uk.ac.standrews.cs.trombone.evaluation;

import java.io.File;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.shabdiz.ApplicationDescriptor;
import uk.ac.standrews.cs.shabdiz.ApplicationState;
import uk.ac.standrews.cs.shabdiz.host.Host;
import uk.ac.standrews.cs.shabdiz.host.LocalHost;
import uk.ac.standrews.cs.shabdiz.host.SSHHost;
import uk.ac.standrews.cs.shabdiz.job.Worker;
import uk.ac.standrews.cs.shabdiz.job.WorkerNetwork;
import uk.ac.standrews.cs.shabdiz.job.util.SerializableVoid;
import uk.ac.standrews.cs.trombone.evaluation.util.BlubHostProvider;

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

    protected Experiment(String events_path, String observations_path) {

        this.events_path = events_path;
        this.observations_path = observations_path;
        events = new File(events_path);
    }

    public final void run() throws Exception {

        LOGGER.info("Setting up to execute events at {}", events_path);
        LOGGER.info("Observations to be stored at {}", observations_path);

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
        
        final FileSystem events_file_system = FileSystems.newFileSystem(Paths.get(events.getAbsolutePath()), null);
        host_indices = EventReader.readHostNames(events_file_system.getPath("hosts.csv"));
        worker_network = new WorkerNetwork();

        for (String host_name : host_indices.keySet()) {
            final Host host = isLocalhost(host_name) ? new LocalHost() : new SSHHost(host_name, BlubHostProvider.SSHJ_AUTH);
            worker_network.add(host);
            host.upload(events, "/Users/masih/Desktop/state/partition1/trombone/experiments/events/");
        }

        worker_network.addCurrentJVMClasspath();
        worker_network.deployAll();
        worker_network.awaitAnyOfStates(ApplicationState.RUNNING);
    }

    protected void tearDown() throws Exception {

        worker_network.shutdown();
    }

    private static boolean isLocalhost(final String host_name) {

        return host_name.equals(LOCALHOST);
    }

    private int getHostIndexByName(final String host_name) {

        return host_indices.get(host_name);
    }

    private void execute() throws Exception {

        final Map<Host, Future<SerializableVoid>> host_event_executions = new HashMap<>();

        for (ApplicationDescriptor descriptor : worker_network) {

            final Worker worker = descriptor.getApplicationReference();
            final Host host = descriptor.getHost();
            final int host_index = 1;//getHostIndexByName(host.getName());
            final Future<SerializableVoid> future_event_execution = worker.submit(new EventExecutionJob(events_path, host_index, observations_path));
            host_event_executions.put(host, future_event_execution);
        }

        for (Map.Entry<Host, Future<SerializableVoid>> host_event_entry : host_event_executions.entrySet()) {

            final Host host = host_event_entry.getKey();
            final Future<SerializableVoid> future_event_execution = host_event_entry.getValue();

            try {
                future_event_execution.get();
                LOGGER.info("successfully finished executing events on host {}", host);
            }
            catch (InterruptedException | ExecutionException e) {
                LOGGER.error("Event execution on host {} failed due to {}", host, e);
                LOGGER.error("Failure details", e);
            }
        }
    }
}
