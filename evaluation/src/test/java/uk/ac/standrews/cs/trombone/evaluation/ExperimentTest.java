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
import uk.ac.standrews.cs.trombone.evaluation.scenarios.PlatformJustificationSingleHost;
import uk.ac.standrews.cs.trombone.event.EventGenerator;
import uk.ac.standrews.cs.trombone.event.Scenario;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
@Category(Ignore.class)
public class ExperimentTest {

    @Test
    public void testRun() throws Exception {

        final File events_home = new File("results/PlatformJustificationSingleHost", "events.zip");
        Experiment experiment = new Experiment(events_home.getAbsolutePath(), events_home.getParent() + "/PlatformJustificationSingleHost");
        experiment.run();

        //        final EventExecutionJob executionJob = new EventExecutionJob(events_home.getAbsolutePath(), 1, events_home.getParent() + "/PlatformJustificationSingleHost");
        //        executionJob.call();
    }

    @Test
    public void testGenerate() throws Exception {

        final Scenario scenario = new PlatformJustificationSingleHost();
        final File events_home = new File("/Users/masih/Desktop", "PlatformJustificationSingleHost.zip");
        //        final File events_home = new File("/Users/masih/Desktop", "PlatformJustificationSingleHost_churn.zip");
        final URI events_uri = URI.create("jar:" + events_home.toURI());

        final Map<String, String> environment = new HashMap<>();
        environment.put("create", "true");
        environment.put("encoding", "UTF8");

        try (final FileSystem events_file_system = FileSystems.newFileSystem(events_uri, environment)) {
            final EventGenerator generator = new EventGenerator(scenario, events_file_system.getPath("/"));
            generator.generate();
        }
    }
}
