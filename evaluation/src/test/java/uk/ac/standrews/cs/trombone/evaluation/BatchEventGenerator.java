package uk.ac.standrews.cs.trombone.evaluation;

import java.io.File;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.trombone.evaluation.scenarios.PlatformJustificationMultipleHost;
import uk.ac.standrews.cs.trombone.evaluation.scenarios.PlatformJustificationSingleHost;
import uk.ac.standrews.cs.trombone.event.EventGenerator;
import uk.ac.standrews.cs.trombone.event.Participant;
import uk.ac.standrews.cs.trombone.event.Scenario;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public final class BatchEventGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchEventGenerator.class);
    private final List<Scenario> scenarios = new ArrayList<>();

    static {
    }

    @Before
    public void setUp() throws Exception {

        scenarios.add(new PlatformJustificationSingleHost());
        scenarios.add(new PlatformJustificationMultipleHost());
    }

    @Test
    public void testGeneration() throws Exception {

        for (Scenario scenario : scenarios) {
            Participant.resetNextId();
            final String scenario_name = scenario.getName();
            LOGGER.info("Generating events of scenario: {}", scenario_name);
            final File scenario_home = new File("results", scenario_name);
            FileUtils.forceMkdir(scenario_home);

            final File events_home = new File(scenario_home, "events.zip");
            FileUtils.deleteQuietly(events_home);
            final URI events_uri = URI.create("jar:" + events_home.toURI());
            final Map<String, String> environment = new HashMap<>();
            environment.put("create", "true");

            try (final FileSystem events_file_system = FileSystems.newFileSystem(events_uri, environment)) {
                final EventGenerator generator = new EventGenerator(scenario, events_file_system.getPath("/"));
                generator.generate();
            }
            LOGGER.info("Finished generating events of scenario: {}", scenario_name);
        }
    }
}
