package uk.ac.standrews.cs.trombone.evaluation.scenarios;

import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.trombone.evaluation.util.FileSystemUtils;
import uk.ac.standrews.cs.trombone.evaluation.util.ScenarioUtils;
import uk.ac.standrews.cs.trombone.event.EventExecutor;
import uk.ac.standrews.cs.trombone.event.EventGenerator;
import uk.ac.standrews.cs.trombone.event.Scenario;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class LocalTestExperiment {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalTestExperiment.class);
    private static final LocalTestScenario LOCAL_TEST_SCENARIO_500 = new LocalTestScenario(500);
    private static final LocalTestScenario LOCAL_TEST_SCENARIO_1000 = new LocalTestScenario(1000);

    @Test
    public void testGeneration() throws Exception {

        testGeneration(LOCAL_TEST_SCENARIO_1000);
        testGeneration(LOCAL_TEST_SCENARIO_500);
    }

    private void testGeneration(final Scenario scenario) throws Exception {

        final Path scenarioHome = ScenarioUtils.getScenarioHome(scenario);
        Files.createDirectories(scenarioHome);

        try (FileSystem events_fs = FileSystemUtils.newZipFileSystem(ScenarioUtils.getScenarioEventsPath(scenario.getName()), true)) {

            LOGGER.info("generating events...");
            final EventGenerator generator = new EventGenerator(scenario, events_fs.getPath("/"));
            generator.generate();
            LOGGER.info("Done generating events");
        }
    }

    @Test
    public void testExecution() throws Exception {
//        testExecution(LOCAL_TEST_SCENARIO_500);
        testExecution(LOCAL_TEST_SCENARIO_1000);
    }
    private void testExecution(final Scenario scenario) throws Exception {

        final Path events_path = ScenarioUtils.getScenarioEventsPath(scenario.getName());
        final Path observations_path = Paths.get("/Users", "masih", "Desktop", String.valueOf(System.currentTimeMillis()));
        Files.createDirectories(observations_path);

        try (FileSystem fileSystem = FileSystemUtils.newZipFileSystem(events_path, false)) {
            EventExecutor executor = new EventExecutor(fileSystem.getPath("/"), 1, observations_path);
            executor.start();
            executor.awaitCompletion();
            executor.shutdown();
        }

    }
}
