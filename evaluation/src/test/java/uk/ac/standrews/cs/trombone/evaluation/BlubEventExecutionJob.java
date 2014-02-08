package uk.ac.standrews.cs.trombone.evaluation;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.shabdiz.job.Job;
import uk.ac.standrews.cs.trombone.evaluation.util.FileSystemUtils;
import uk.ac.standrews.cs.trombone.evaluation.util.ScenarioUtils;
import uk.ac.standrews.cs.trombone.event.EventExecutor;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class BlubEventExecutionJob implements Job<String> {

    private static final long serialVersionUID = 2675891974884649473L;
    private static final Logger LOGGER = LoggerFactory.getLogger(BlubEventExecutionJob.class);
    static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS");
    private final String scenario_name;
    private final int host_index;

    public BlubEventExecutionJob(String scenario_name, int host_index) {

        this.scenario_name = scenario_name;
        this.host_index = host_index;
    }

    @Override
    public String call() throws Exception {

        LOGGER.info("preparing to execute events for scenario {} with host index {}", scenario_name, host_index);
        final Path events_zip = BlubBatchEventUpload.BLUB_NODE_RESULTS_HOME.resolve(ScenarioUtils.getScenarioEventsPath(scenario_name));
        final Path repetitions = BlubBatchEventUpload.BLUB_NODE_RESULTS_HOME.resolve(ScenarioUtils.getScenarioRepetitionsHome(scenario_name));

        LOGGER.info("events file: {}", events_zip);
        LOGGER.info("repetitions directory: {}", repetitions);

        assureRepetitionsDirectoryExists(repetitions);

        final Path observations = newObservationsPath(repetitions);
        LOGGER.info("observations to be stored at: {}", observations);

        try (
                FileSystem events_fs = FileSystemUtils.newZipFileSystem(events_zip, false);
                FileSystem observations_fs = FileSystemUtils.newZipFileSystem(observations, true)
        ) {

            final Path events_root = events_fs.getPath(events_fs.getSeparator());
            final Path observations_root = observations_fs.getPath(observations_fs.getSeparator());
            final EventExecutor event_executor = new EventExecutor(events_root, host_index, observations_root);

            LOGGER.info("starting event executor...");
            event_executor.start();

            LOGGER.info("awaiting event execution completion...");
            event_executor.awaitCompletion();

            LOGGER.info("shutting down the event executor...");
            event_executor.shutdown();
        }

        LOGGER.info("finished executing events of scenario {} with host index {}", scenario_name, host_index);
        LOGGER.info("observations are stored at {}", observations);
        return observations.toString();
    }

    static synchronized Path newObservationsPath(final Path repetitions) {

        return repetitions.resolve(DATE_FORMAT.format(new Date()) + ".zip");
    }

    static void assureRepetitionsDirectoryExists(final Path repetitions) throws IOException {

        if (!Files.isDirectory(repetitions)) {
            Files.createDirectories(repetitions);
        }
    }
}
