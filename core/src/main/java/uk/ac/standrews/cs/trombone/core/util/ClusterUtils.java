package uk.ac.standrews.cs.trombone.core.util;

import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.Clusterable;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public final class ClusterUtils {

    private ClusterUtils() {

    }

    public static <Point extends Clusterable> boolean contains(Cluster<Point> cluster, Point point) {

        return cluster.getPoints().contains(point);
    }
}
