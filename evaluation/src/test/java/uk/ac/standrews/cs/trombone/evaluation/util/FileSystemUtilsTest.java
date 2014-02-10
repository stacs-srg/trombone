package uk.ac.standrews.cs.trombone.evaluation.util;

import java.nio.file.FileSystem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import uk.ac.standrews.cs.test.category.Ignore;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
@Category(Ignore.class)
public class FileSystemUtilsTest {

    private FileSystem zip_file_system;

    @Before
    public void setUp() throws Exception {

        zip_file_system = FileSystemUtils.newZipFileSystem("test.zip", true);
    }

    @After
    public void tearDown() throws Exception {

        zip_file_system.close();
    }

    @Test
    public void testCopyRecursively() throws Exception {

        final FileSystem existing_zip = FileSystemUtils.newZipFileSystem("/Users/masih/Documents/PhD/Code/t3/evaluation/results/PlatformJustificationMultipleHost48/Archive.zip", false);
        FileSystemUtils.copyRecursively(existing_zip.getPath("/"), zip_file_system.getPath("/"));
    }
}
