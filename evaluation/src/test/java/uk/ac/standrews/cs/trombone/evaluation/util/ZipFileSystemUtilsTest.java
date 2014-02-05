package uk.ac.standrews.cs.trombone.evaluation.util;

import java.nio.file.FileSystem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class ZipFileSystemUtilsTest {

    private FileSystem zip_file_system;

    @Before
    public void setUp() throws Exception {

        zip_file_system = ZipFileSystemUtils.newZipFileSystem("test.zip", true);
    }

    @After
    public void tearDown() throws Exception {

        zip_file_system.close();
    }

    @Test
    public void testCopyRecursively() throws Exception {

        final FileSystem existing_zip = ZipFileSystemUtils.newZipFileSystem("/Users/masih/Documents/PhD/Code/t3/evaluation/results/PlatformJustificationMultipleHost/Archive.zip", false);
        ZipFileSystemUtils.copyRecursively(existing_zip.getPath("/"), zip_file_system.getPath("/"));
    }
}
