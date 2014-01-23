package uk.ac.standrews.cs.trombone.evaluation;

import java.io.File;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import uk.ac.standrews.cs.test.category.Ignore;
import uk.ac.standrews.cs.trombone.evaluation.scenarios.Scenario1;
import uk.ac.standrews.cs.trombone.event.EventGenerator;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
@Category(Ignore.class)
public class ExperimentTest {

    @Test
    public void testRun() throws Exception {

        final Scenario1 scenario = new Scenario1();
        final File events_home = new File("/Users/masih/Desktop", scenario.getName() + ".zip");
        final URI path = URI.create("jar:" + events_home.toURI());

        final Map<String, String> environment = new HashMap<>();
        environment.put("create", "true");

        try (final FileSystem events_file_system = FileSystems.newFileSystem(path, environment)) {
            final EventGenerator generator = new EventGenerator(scenario, events_file_system.getPath("/"));
            generator.generate();
        }

        //        Experiment experiment = new Experiment("/Users/masih/Desktop/test.zip", "/Users/masih/Desktop/test.zip");
        //        experiment.run();

        EventExecutionJob executionJob = new EventExecutionJob("/Users/masih/Desktop/scenario_1.zip", 1, "/Users/masih/Desktop/scenario_1.zip");
        executionJob.call();

    }
}
