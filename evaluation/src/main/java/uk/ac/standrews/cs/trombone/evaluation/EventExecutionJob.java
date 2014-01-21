package uk.ac.standrews.cs.trombone.evaluation;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import uk.ac.standrews.cs.shabdiz.job.Job;
import uk.ac.standrews.cs.shabdiz.job.util.SerializableVoid;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class EventExecutionJob implements Job<SerializableVoid> {

    private static final long serialVersionUID = 2675891974884649473L;
    private final String events_path;
    private final int host_index;
    private final String observations_path;

    public EventExecutionJob(String events_path, int host_index, String observations_path) {

        this.events_path = events_path;
        this.host_index = host_index;
        this.observations_path = observations_path;
    }

    @Override
    public SerializableVoid call() throws Exception {

        final Path events = Paths.get(events_path);
        final Path observations = Paths.get(observations_path);
        final FileSystem events_file_system = FileSystems.newFileSystem(events, null);
        final FileSystem observations_file_system = FileSystems.newFileSystem(observations, null);

        final EventExecutor event_executor = new EventExecutor(events_file_system, host_index, observations_file_system);
        event_executor.start();
        event_executor.awaitCompletion();
        event_executor.stop();

        return null; // void task
    }
}
