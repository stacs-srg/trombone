package uk.ac.standrews.cs.trombone.evaluation.scenarios;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import uk.ac.standrews.cs.shabdiz.util.Duration;
import uk.ac.standrews.cs.trombone.evaluation.util.ScenarioUtils;
import uk.ac.standrews.cs.trombone.event.EventExecutor;
import uk.ac.standrews.cs.trombone.event.EventQueue;
import uk.ac.standrews.cs.trombone.event.Scenario;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class LocalTestExperiment {

    @Test
    public void testExecution() throws Exception {

        final Scenario scenario = Batch1EffectOfChurn.getInstance()
                .get()
                .get(8);

        // 13 evolutionary
        // 10 chord

        testExecution(scenario);
    }

    private static void testExecution(final Scenario scenario) throws Exception {

        final Path observations_path = Paths.get("/Users", "masih", "Desktop", String.valueOf(System.currentTimeMillis()));
        Files.createDirectories(observations_path);
        ScenarioUtils.saveScenarioAsJson(scenario, observations_path);

        final EventExecutor executor = new EventExecutor(new EventQueue(scenario, 1), observations_path);
        executor.start();

        final Duration timeout = executor.getExperimentDuration()
                .add(new Duration(5, TimeUnit.MINUTES));
        executor.awaitCompletion(timeout.getLength(), timeout.getTimeUnit());
        executor.shutdown();

    }
}
