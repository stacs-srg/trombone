package uk.ac.standrews.cs.trombone.evaluation;

import java.io.File;
import java.io.FileFilter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class BlubBatchExperimentRunner {

    static final File EVENTS = new File("results");

    public static void main(String[] args) throws Exception {

        FileUtils.forceMkdir(EVENTS);

        final File[] zip_events = EVENTS.listFiles(new FileFilter() {

            @Override
            public boolean accept(final File file) {

                return file.isFile() && file.getName().endsWith(".zip");
            }
        });

        for (File zip_event : zip_events) {
            Experiment experiment = new Experiment(zip_event.getAbsolutePath(), "target/results/" + FilenameUtils.getBaseName(zip_event.getName()));
            experiment.run();
        }
    }
}
