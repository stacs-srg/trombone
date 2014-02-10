package uk.ac.standrews.cs.trombone.evaluation;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
@RunWith(Suite.class)
@Suite.SuiteClasses( {
        BatchEventGenerator.class, BlubBatchEventUpload.class, BlubExperiment.class
})
public class BlubExperimentation {}