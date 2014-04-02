package uk.ac.standrews.cs.trombone.core.util;

import java.util.Arrays;
import java.util.List;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.apache.commons.math3.util.FastMath;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public final class CosineSimilarity implements DistanceMeasure {

    private static final long serialVersionUID = 2491554770050529309L;

    public static <Point extends Clusterable> Double getSimilarity(final Point point, final Cluster<Point> cluster) {

        final List<Point> clustered_points = cluster.getPoints();
        final int max_sum_similarity = clustered_points.size();

        double sum_similarity = 0;
        for (Point clustered_point : clustered_points) {
            sum_similarity += cosineSimilarity(point.getPoint(), clustered_point.getPoint());
        }

        return max_sum_similarity == 0 ? 0 : sum_similarity / max_sum_similarity;
    }

    public static double cosineSimilarity(double[] one, double[] other) {

        if (one.length != other.length) { throw new IllegalArgumentException("the length of vectors must be equal"); }
        if (Arrays.equals(one, other)) { return 1; }

        final double dot_product = dotProduct(one, other);
        final double magnitude_one = magnitude(one);
        final double magnitude_other = magnitude(other);
        return dot_product / (magnitude_one * magnitude_other);
    }

    @Override
    public double compute(final double[] one, final double[] other) {

        return cosineSimilarity(one, other);
    }

    private static double dotProduct(double[] one, double[] other) {

        assert one.length == other.length;

        final int length = one.length;
        double sum = 0;
        for (int index = 0; index < length; index++) {
            sum += one[index] * other[index];
        }

        return sum;
    }

    private static double magnitude(double[] vector) {

        double sum_pow_two = 0;
        for (final double entry : vector) {
            sum_pow_two += FastMath.pow(entry, 2);
        }
        return FastMath.sqrt(sum_pow_two);
    }
}
