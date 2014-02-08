package uk.ac.standrews.cs.trombone.evaluation;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.shabdiz.host.Host;
import uk.ac.standrews.cs.shabdiz.host.LocalHost;
import uk.ac.standrews.cs.shabdiz.util.Combinations;
import uk.ac.standrews.cs.trombone.evaluation.util.FileSystemUtils;
import uk.ac.standrews.cs.trombone.evaluation.util.ScenarioUtils;

import static org.junit.Assume.assumeTrue;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
@RunWith(Parameterized.class)
public class BlubBatchEventUpload {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlubBatchEventUpload.class);

    static final Path BLUB_NODE_RESULTS_HOME = Paths.get("/state", "partition1", "t3", "evaluation");
    public static final String BLUB_HEAD_HOST_NAME = "blub-cs.st-andrews.ac.uk";

    private final Host host;
    private final Path destination;
    private static List<Path> events_zips;
    public static final Path LOCAL_RESULTS_HOME = ScenarioUtils.getResultsHome();

    public BlubBatchEventUpload(Host host, Path destination) {

        this.host = host;
        this.destination = destination;
    }

    @Parameterized.Parameters(name = "{index}, host: {0}, destination: {1}")
    public static Collection<Object[]> data() throws IOException {

        return Combinations.generateArgumentCombinations(new Object[][] {
                {new LocalHost()}, {BLUB_NODE_RESULTS_HOME}
        });
    }

    @BeforeClass
    public static void setUp() throws Exception {

        assumeTrue("This test is to be run on blub head node", InetAddress.getLocalHost().getHostName().equals(BLUB_HEAD_HOST_NAME));
        final PathMatcher events_zip_path_matcher = LOCAL_RESULTS_HOME.getFileSystem().getPathMatcher("glob:*/*/events.zip");

        LOGGER.info("searching local results folder for events.zip: {}", LOCAL_RESULTS_HOME);
        events_zips = FileSystemUtils.getMatchingFiles(LOCAL_RESULTS_HOME, events_zip_path_matcher);

        assumeTrue("No events.zip fount at " + LOCAL_RESULTS_HOME, !events_zips.isEmpty());
        LOGGER.info("found {} events to be uploaded", events_zips.size());
    }

    @Test
    public void uploadEvents() throws Exception {

        LOGGER.info("preparing to upload events to {}", host);
        LOGGER.info("destination on host is set to {}", destination);
        for (Path events_zip : events_zips) {

            final Path events_destination = destination.resolve(events_zip);
            LOGGER.info("uploading {}", events_zip);
            host.upload(events_zip.toFile(), events_destination.toString());
            LOGGER.info("uploaded to {}", events_destination);
        }
    }

    @After
    public void tearDown() throws Exception {

        LOGGER.info("closing host {}", host);
        host.close();
    }
}
