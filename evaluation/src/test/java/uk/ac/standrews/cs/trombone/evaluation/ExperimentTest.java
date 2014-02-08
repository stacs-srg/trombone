package uk.ac.standrews.cs.trombone.evaluation;

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

        //        final File events_home = new File("results/PlatformJustificationSingleHost", "events.zip");
        //        Experiment experiment = new Experiment(events_home.getAbsolutePath(), events_home.getParent() + "/repetitions");
        //        experiment.doExperiment();

        final BlubEventExecutionJob executionJob = new BlubEventExecutionJob("PlatformJustificationSingleHost", 1);
        executionJob.call();
    }
}
