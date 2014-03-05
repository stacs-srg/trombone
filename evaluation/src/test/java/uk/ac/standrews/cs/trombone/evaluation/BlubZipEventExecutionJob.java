package uk.ac.standrews.cs.trombone.evaluation;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.OutputStreamAppender;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.shabdiz.job.Job;
import uk.ac.standrews.cs.shabdiz.util.Duration;
import uk.ac.standrews.cs.trombone.evaluation.util.FileSystemUtils;
import uk.ac.standrews.cs.trombone.evaluation.util.ScenarioUtils;
import uk.ac.standrews.cs.trombone.event.EventExecutor;

import static uk.ac.standrews.cs.trombone.evaluation.BlubBatchEventUpload.BLUB_NODE_RESULTS_HOME;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class BlubZipEventExecutionJob implements Job<String> {

    private static final long serialVersionUID = 2675891974884649473L;
    private static final Logger LOGGER = LoggerFactory.getLogger(BlubZipEventExecutionJob.class);
    static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS");
    private final String scenario_name;
    private final int host_index;
    private final HashMap<Integer, String> host_indices;
    private final String results_home_path;

    public BlubZipEventExecutionJob(String scenario_name, int host_index) {

        this(scenario_name, host_index, null);
    }

    public BlubZipEventExecutionJob(String scenario_name, int host_index, HashMap<Integer, String> host_indices) {

        this(scenario_name, host_index, host_indices, BLUB_NODE_RESULTS_HOME);
    }

    public BlubZipEventExecutionJob(String scenario_name, int host_index, HashMap<Integer, String> host_indices, Path results_home) {

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
        final Path events_zip = results_home.resolve(ScenarioUtils.getScenarioEventsPath(scenario_name));
        final Path repetitions = results_home.resolve(ScenarioUtils.getScenarioRepetitionsHome(scenario_name));

        LOGGER.info("events file: {}", events_zip);
        LOGGER.info("repetitions directory: {}", repetitions);

        assureRepetitionsDirectoryExists(repetitions);

        final Path observations = newObservationsPath(repetitions);
        LOGGER.info("observations to be stored at: {}", observations);

        try (
                FileSystem events_fs = FileSystemUtils.newZipFileSystem(events_zip, false);
                FileSystem observations_fs = FileSystemUtils.newZipFileSystem(observations, true)
        ) {
            final OutputStreamAppender<ILoggingEvent> log_appender = initAppender(observations_fs.getPath("experiment.log"));
            log_appender.start();

            try {
                final Path events_root = events_fs.getPath(events_fs.getSeparator());
                final Path observations_root = observations_fs.getPath(observations_fs.getSeparator());
                final EventExecutor event_executor = new EventExecutor(events_root, host_index, observations_root, host_indices);

                try {
                    LOGGER.info("starting event executor...");
                    event_executor.start();

                    final Duration experiment_duration = event_executor.getExperimentDuration();
                    final Duration await_timeout = experiment_duration.add(new Duration(10, TimeUnit.MINUTES));

                    LOGGER.info("awaiting event execution completion...");
                    event_executor.awaitCompletion(await_timeout.getLength(), await_timeout.getTimeUnit());
                }
                finally {
                    LOGGER.info("shutting down the event executor...");
                    event_executor.shutdown();
                }
            }
            finally {
                log_appender.stop();
            }
        }

        LOGGER.info("finished executing events of scenario {} with host index {}", scenario_name, host_index);
        LOGGER.info("observations are stored at {}", observations);
        return observations.toString();
    }

    static OutputStreamAppender<ILoggingEvent> initAppender(final Path log_path) throws IOException {

        final ILoggerFactory logger_factory = LoggerFactory.getILoggerFactory();

        final LoggerContext logger_context = (LoggerContext) logger_factory;
        final ch.qos.logback.classic.Logger root_logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        final OutputStreamAppender<ILoggingEvent> stream_appender = new OutputStreamAppender<>();

        PatternLayout layout = new PatternLayout();
        layout.setContext(logger_context);
        layout.setPattern("%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n");
        layout.start();
        stream_appender.setContext(logger_context);
        stream_appender.setLayout(layout);
        stream_appender.setOutputStream(Files.newOutputStream(log_path, StandardOpenOption.CREATE, StandardOpenOption.APPEND, StandardOpenOption.WRITE, StandardOpenOption.SYNC, StandardOpenOption.DSYNC));
        stream_appender.setName("evaluation_appender");
        root_logger.addAppender(stream_appender);
        return stream_appender;
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
