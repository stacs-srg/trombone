package uk.ac.standrews.cs.trombone.core.adaptation.clustering.pfclust;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.clustering.Clusterer;
import uk.ac.standrews.cs.trombone.core.util.CosineSimilarity;

/**
 * This class implements the main loop and the convergence steps of the algorithm.
 */
public class PFClustClusterer<Point extends Clusterable> extends Clusterer<Point> {

    private static final CosineSimilarity COSINE_SIMILARITY = new CosineSimilarity();
    /** The number of iterations of the main loop. */
    public static final int DEFAULT_MAIN_LOOP = 4;
    /**
     * Represents the number of iterations of the randomization loop
     */
    static int RANDOMIZATION_LOOP = 100;
    /**
     * The cut-off percentage of the selected threshold
     */
    public static double P = 0.85;
    /**
     * Represents the threshold for the Rand index
     */
    public static double RAND_THRESHOLD = 0.99;
    /** The maximum number of iterations to execute the convergence step of the algorithm. */
    public static int maxIteration = 10;
    private final Random random;

    public PFClustClusterer() {

        super(COSINE_SIMILARITY);
        random = new Random();
    }

    @Override
    public List<CentroidCluster<Point>> cluster(final Collection<Point> point) {

        final List<Point> points = new ArrayList<>(point);
        final Array2DRowRealMatrix similarity_matrix = getSimilarityMatrix(points);
        final Clustering clustering = process(similarity_matrix);
        final List<CentroidCluster<Point>> clusters = toCentroidClusters(points, clustering);
        return clusters;
    }

    private List<CentroidCluster<Point>> toCentroidClusters(final List<Point> points, final Clustering clustering) {

        final List<CentroidCluster<Point>> centroid_clusters = new ArrayList<>(clustering.size());
        for (Cluster cluster : clustering.getClusters()) {
            final CentroidCluster<Point> centroidCluster = toCentroidCluster(points, cluster);
            centroid_clusters.add(centroidCluster);
        }
        return centroid_clusters;
    }

    private CentroidCluster<Point> toCentroidCluster(final List<Point> points, final Cluster cluster) {

        final CentroidCluster<Point> centroid_cluster = new CentroidCluster<>(points.get(cluster.getCentroid()));
        for (Integer member_index : cluster.getMembers()) {
            centroid_cluster.addPoint(points.get(member_index));
        }
        return centroid_cluster;
    }

    private Array2DRowRealMatrix getSimilarityMatrix(final List<Point> points) {

        final Array2DRowRealMatrix similarity_matrix = new Array2DRowRealMatrix(points.size(), points.size());
        int row = 0;
        int column = 0;
        for (Point one : points) {
            for (Point other : points) {
                similarity_matrix.setEntry(row, column, distance(one, other));
                column++;
            }
            row++;
            column = 0;
        }
        return similarity_matrix;
    }

    protected Clustering process(final RealMatrix similarity_matrix) {

        ClusteringCollection clusteringCollection = new ClusteringCollection();
        Clustering bestClustering;

        for (int i = 0; i < DEFAULT_MAIN_LOOP; i++) {

            ArrayList<Double> thresholds = Threshold.estimate(random, similarity_matrix);
            Clustering clustering = ClusterConstruction.perform(similarity_matrix, thresholds);
            clusteringCollection.add(clustering);

        }

        double randIndex = clusteringCollection.getRandIndex();

        if (randIndex < RAND_THRESHOLD) {
            converge(random, similarity_matrix, clusteringCollection);
        }

        clusteringCollection.sort();
        bestClustering = clusteringCollection.getBestClustering();

        return bestClustering;
    }

    /**
     * If the Rand index of the clustering collection is less
     * than a specified value, it builds a new clustering
     * and replaces the worst clustering in the list
     * with the new one, if the new clustering is better
     * than the worst clustering in the list.
     * The process continues unless the Rand index of the collection
     * is greater than 0.99
     *
     * @param matrix Matrix matrix
     * @param clusteringCollection clusteringCollection
     */
    protected static void converge(Random random, final RealMatrix matrix, ClusteringCollection clusteringCollection) {

        double randIndex = clusteringCollection.getRandIndex();
        int counter = 0;

        while (randIndex < RAND_THRESHOLD && counter <= maxIteration) {
            clusteringCollection.sort();
            Clustering worstClustering = clusteringCollection.getWorstClustering();
            ArrayList<Double> thresholds = Threshold.estimate(random, matrix);
            Clustering clustering = ClusterConstruction.perform(matrix, thresholds);

            if (clustering.compareTo(worstClustering) > 0) {
                clusteringCollection.remove(worstClustering);
                clusteringCollection.add(clustering);
            }

            randIndex = clusteringCollection.getRandIndex();
            counter++;
        }
    }
}
