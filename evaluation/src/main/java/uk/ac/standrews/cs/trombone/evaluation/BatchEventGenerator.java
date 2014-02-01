package uk.ac.standrews.cs.trombone.evaluation;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import uk.ac.standrews.cs.trombone.evaluation.scenarios.PlatformJustificationSingleHost;
import uk.ac.standrews.cs.trombone.event.EventGenerator;
import uk.ac.standrews.cs.trombone.event.Scenario;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class BatchEventGenerator {

    public static final List<Scenario> SCENARIOS = new ArrayList<>();

    static {
        SCENARIOS.add(new PlatformJustificationSingleHost());
    }

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {

        for (Scenario scenario : SCENARIOS) {
            File events_home = new File(BlubBatchExperimentRunner.EVENTS, scenario.getName() + ".zip");
            final URI events_uri = URI.create("jar:" + events_home.toURI());
            final Map<String, String> environment = new HashMap<>();
            environment.put("create", "true");

            try (final FileSystem events_file_system = FileSystems.newFileSystem(events_uri, environment)) {
                final EventGenerator generator = new EventGenerator(scenario, events_file_system.getPath("/"));
                generator.generate();
            }
        }
    }
}
