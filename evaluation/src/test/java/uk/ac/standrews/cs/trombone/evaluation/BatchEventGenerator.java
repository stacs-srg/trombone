package uk.ac.standrews.cs.trombone.evaluation;

import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.shabdiz.util.Combinations;
import uk.ac.standrews.cs.trombone.evaluation.scenarios.PlatformJustificationMultipleHost;
import uk.ac.standrews.cs.trombone.evaluation.scenarios.PlatformJustificationSingleHost;
import uk.ac.standrews.cs.trombone.evaluation.util.FileSystemUtils;
import uk.ac.standrews.cs.trombone.evaluation.util.ScenarioUtils;
import uk.ac.standrews.cs.trombone.event.EventGenerator;
import uk.ac.standrews.cs.trombone.event.Participant;
import uk.ac.standrews.cs.trombone.event.Scenario;

import static org.junit.Assume.assumeTrue;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
@RunWith(Parameterized.class)
public final class BatchEventGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchEventGenerator.class);
    private final Scenario scenario;
    private String scenario_name;
    private Path scenario_home;
    private Path scenario_events;

    private static final Scenario[] SCENARIOS = {
            new PlatformJustificationSingleHost(), new PlatformJustificationMultipleHost()
    };

    public BatchEventGenerator(Scenario scenario) {

        this.scenario = scenario;
    }

    @Parameterized.Parameters(name = "{index} scenario: {0}")
    public static Collection<Object[]> parameters() {

        return Combinations.generateArgumentCombinations(new Object[][] {SCENARIOS});
    }

    @Before
    public void setUp() throws Exception {

        Participant.resetNextId();
        scenario_name = scenario.getName();
        scenario_home = ScenarioUtils.getScenarioHome(scenario_name);
        Files.createDirectories(scenario_home);

        scenario_events = ScenarioUtils.getScenarioEventsPath(scenario_name);
        if (Files.exists(scenario_events)) {
            LOGGER.warn("scenario events already exist: {}", scenario_events);
            LOGGER.info("deleting existing events...");
            Files.delete(scenario_events);
        }
    }

    @Test
    public void testGeneration() throws Exception {

        assumeTrue("events already exist", !Files.exists(scenario_events));

        LOGGER.info("Generating events of scenario: {}", scenario_name);
        try (final FileSystem events_file_system = FileSystemUtils.newZipFileSystem(scenario_events, true)) {
            final Path root = events_file_system.getPath(events_file_system.getSeparator());
            final EventGenerator generator = new EventGenerator(scenario, root);
            generator.generate();
        }
        LOGGER.info("Finished generating events of scenario: {}", scenario_name);
    }
}
