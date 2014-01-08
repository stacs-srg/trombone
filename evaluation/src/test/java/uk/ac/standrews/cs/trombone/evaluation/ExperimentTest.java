package uk.ac.standrews.cs.trombone.evaluation;

import java.io.File;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import uk.ac.standrews.cs.test.category.Ignore;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
@Category(Ignore.class)
public class ExperimentTest {

    private EventExecutor executor;

    @Before
    public void setUp() throws Exception {

        File f = new File("s.zip");
        final FileSystem fileSystem = FileSystems.newFileSystem(URI.create("jar:" + f.toURI()), new HashMap<String, Object>());
        executor = new EventExecutor(fileSystem, 1);
    }

    @After
    public void tearDown() throws Exception {

        executor.start();
        Thread.sleep(TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES));
    }

    @Test
    public void testRun() throws Exception {

        executor.stop();
    }
}
