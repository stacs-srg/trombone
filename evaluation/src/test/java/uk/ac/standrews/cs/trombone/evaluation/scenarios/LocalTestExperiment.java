package uk.ac.standrews.cs.trombone.evaluation.scenarios;

import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.shabdiz.util.Duration;
import uk.ac.standrews.cs.trombone.evaluation.BatchEventGenerator;
import uk.ac.standrews.cs.trombone.evaluation.util.FileSystemUtils;
import uk.ac.standrews.cs.trombone.evaluation.util.ScenarioUtils;
import uk.ac.standrews.cs.trombone.event.EventExecutor;
import uk.ac.standrews.cs.trombone.event.EventGenerator;
import uk.ac.standrews.cs.trombone.event.EventQueue;
import uk.ac.standrews.cs.trombone.event.Scenario;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class LocalTestExperiment {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalTestExperiment.class);
    private static final LocalTestScenario LOCAL_TEST_SCENARIO_500 = new LocalTestScenario(500);
    private static final LocalTestScenario LOCAL_TEST_SCENARIO_1000 = new LocalTestScenario(1000);
    private static final LocalTestScenario LOCAL_TEST_SCENARIO_20 = new LocalTestScenario(20);
    private static final LocalTestScenario LOCAL_TEST_SCENARIO_100 = new LocalTestScenario(100);

    @Test
    public void testAGeneration() throws Exception {

        //        testGeneration(LOCAL_TEST_SCENARIO_100);
        //        testGeneration(LOCAL_TEST_SCENARIO_20);
        testGeneration(LOCAL_TEST_SCENARIO_1000);
        //        testGeneration(LOCAL_TEST_SCENARIO_500);
    }

    private void testGeneration(final Scenario scenario) throws Exception {

        final Path scenarioHome = ScenarioUtils.getScenarioHome(scenario);
        Files.createDirectories(scenarioHome);
        final Path path_to_zip = Paths.get("/Users", "masih", "Desktop", scenario.getName() + ".zip");

        try (FileSystem events_fs = FileSystemUtils.newZipFileSystem(path_to_zip, true)) {

            LOGGER.info("generating events...");
            final EventGenerator generator = new EventGenerator(scenario, events_fs.getPath("/"));
            generator.generate();
            LOGGER.info("Done generating events");
        }
    }

    @Test
    public void testExecution() throws Exception {

        testExecution(BatchEventGenerator.SCENARIOS.get(67));
        //        testExecution(LOCAL_TEST_SCENARIO_500);
        //        testExecution(LOCAL_TEST_SCENARIO_1000);
        //        testExecution(LOCAL_TEST_SCENARIO_20);
        //        testExecution(LOCAL_TEST_SCENARIO_100);
    }

    private void testExecution(final Scenario scenario) throws Exception {

        final Path observations_path = Paths.get("/Users", "masih", "Desktop", String.valueOf(System.currentTimeMillis()));
        Files.createDirectories(observations_path);

        EventExecutor executor = new EventExecutor(new EventQueue(scenario, 1), observations_path);
        executor.start();
        final Duration timeout = executor.getExperimentDuration().add(new Duration(5, TimeUnit.MINUTES));
        executor.awaitCompletion(timeout.getLength(), timeout.getTimeUnit());
        executor.shutdown();

    }
}
