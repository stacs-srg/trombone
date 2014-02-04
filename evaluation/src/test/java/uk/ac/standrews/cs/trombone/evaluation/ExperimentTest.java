package uk.ac.standrews.cs.trombone.evaluation;

import java.io.File;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import uk.ac.standrews.cs.test.category.Ignore;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
@Category(Ignore.class)
public class ExperimentTest {

    @Test
    public void testRun() throws Exception {

        final File events_home = new File("results/PlatformJustificationSingleHost", "events.zip");
        Experiment experiment = new Experiment(events_home.getAbsolutePath(), events_home.getParent() + "/repetitions");
        experiment.run();

        //        final EventExecutionJob executionJob = new EventExecutionJob(events_home.getAbsolutePath(), 1, events_home.getParent() + "/PlatformJustificationSingleHost");
        //        executionJob.call();
    }
}
