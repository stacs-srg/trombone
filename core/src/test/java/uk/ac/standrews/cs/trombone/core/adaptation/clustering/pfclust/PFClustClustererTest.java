package uk.ac.standrews.cs.trombone.core.adaptation.clustering.pfclust;

import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.trombone.core.util.CosineSimilarity;
import uk.ac.standrews.cs.trombone.core.util.MatrixReader;

import static org.junit.Assert.assertEquals;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class PFClustClustererTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(PFClustClustererTest.class);
    private static final Random RANDOM = new Random(8965);
    private static final int POINTS_COUNT = 100;
    private static final int POINT_DIMENSION = 10;
    private static final CosineSimilarity COSINE_SIMILARITY = new CosineSimilarity();
    private static String[] TEST_FILE_NAMES = {
            "cath_12.tsv", "random_14.tsv", "s_2.tsv", "s_3.tsv"
    };

    @Test
    @Ignore
    public void testSpeed() throws Exception {

        final PFClustClusterer<DoublePoint> clusterer = new PFClustClusterer<DoublePoint>();
        System.out.println("'point_count', 'cluster_size', 'clustering_time_ms'");
        for (int i = 500; i <= 500; i++) {

            final int count = i;
            final List<DoublePoint> points = generateRandomPoints(count, 10);
            long now = System.currentTimeMillis();
            final List<CentroidCluster<DoublePoint>> clustering = clusterer.cluster(points);
            long elapsed = System.currentTimeMillis() - now;

            int cluster_count = 0;
            for (CentroidCluster<DoublePoint> cluster : clustering) {
                System.out.print(cluster_count + ":\t (center: " + points.indexOf(cluster.getCenter()) + ") ");
                for (DoublePoint point : cluster.getPoints()) {
                    System.out.print(points.indexOf(point) + " ");
                }
                System.out.println();
                cluster_count++;
            }

            System.out.println();
            System.out.println(count + ", " + clustering.size() + " , " + elapsed);
        }

    }

    private List<DoublePoint> generateRandomPoints(int count, int dimensions) {

        List<DoublePoint> points = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            final double[] p = new double[dimensions];
            for (int j = 0; j < dimensions; j++) {
                p[j] = RANDOM.nextDouble();
            }
            points.add(new DoublePoint(p));
        }
        return points;
    }

    @Test
    public void testCluster() throws Exception {

        final PFClustClusterer clusterer = new PFClustClusterer();
        for (String test_file_name : TEST_FILE_NAMES) {
            LOGGER.info("testing {}", test_file_name);
            int expected_clusters = Integer.parseInt(FilenameUtils.getBaseName(test_file_name.substring(test_file_name.lastIndexOf("_") + 1)));
            final URL resource = getClass().getResource("/uk/ac/standrews/cs/trombone/core/adaptation/clustering/pfclust/" + test_file_name);
            final RealMatrix realMatrix = MatrixReader.toMatrix(Paths.get(resource.toURI()));
            final Clustering process = clusterer.process(realMatrix);
            assertEquals(expected_clusters, process.size());
        }
    }
}
