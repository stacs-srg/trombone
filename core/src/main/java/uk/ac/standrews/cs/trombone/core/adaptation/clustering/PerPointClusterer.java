package uk.ac.standrews.cs.trombone.core.adaptation.clustering;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.clustering.Clusterer;

/**
 * A clustering algorithm that constructs a {@link CentroidCluster} per each point.
 *
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public final class PerPointClusterer<Point extends Clusterable> extends Clusterer<Point> {

    /**
     * Build a new per point clusterer.
     */
    public PerPointClusterer() {

        super(null);
    }

    @Override
    public List<CentroidCluster<Point>> cluster(final Collection<Point> points) {

        List<CentroidCluster<Point>> clustering = new ArrayList<>(points.size());
        for (Point point : points) {
            final CentroidCluster<Point> cluster = new CentroidCluster<>(point);
            cluster.addPoint(point);
            clustering.add(cluster);
        }
        return clustering;
    }

    @Override
    protected double distance(final Clusterable one, final Clusterable other) {

        throw new UnsupportedOperationException();
    }
}
