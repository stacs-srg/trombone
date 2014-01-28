package uk.ac.standrews.cs.trombone.evaluation;

import com.sun.nio.zipfs.ZipFileSystemProvider;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import uk.ac.standrews.cs.shabdiz.job.Job;
import uk.ac.standrews.cs.trombone.event.EventExecutor;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class EventExecutionJob implements Job<String> {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS");
    private static final long serialVersionUID = 2675891974884649473L;
    private static final String BLUB_OBSERVATIONS_HOME = "/state/partition1/trombone/";
    private final String events_path;
    private final int host_index;
    private final String observations_home;

    public EventExecutionJob(String events_path, int host_index) {

        this(events_path, host_index, BLUB_OBSERVATIONS_HOME);
    }

    public EventExecutionJob(String events_path, int host_index, String observations_home) {

        this.events_path = events_path;
        this.host_index = host_index;
        this.observations_home = observations_home;
    }

    @Override
    public String call() throws Exception {

        final Path events = Paths.get(events_path);
        final Path observations_home_path = Files.createDirectories(Paths.get(observations_home));

        final Path observations;
        synchronized (DATE_FORMAT) {
            observations = observations_home_path.resolve(DATE_FORMAT.format(new Date()) + ".zip");
        }

        final ZipFileSystemProvider zip_file_system_provider = new ZipFileSystemProvider();
        final Map<String, String> env = new HashMap<>();
        env.put("create", "true");
        env.put("encoding", "UTF8");

        try (FileSystem events_file_system = FileSystems.newFileSystem(events, null); FileSystem observations_file_system = zip_file_system_provider.newFileSystem(observations, env);) {

            final Path events_root = events_file_system.getPath("/");
            final Path observations_root = observations_file_system.getPath("/");
            final EventExecutor event_executor = new EventExecutor(events_root, host_index, observations_root);

            event_executor.start();
            event_executor.awaitCompletion();
            event_executor.stop();
        }
        return observations.toString();
    }
}
