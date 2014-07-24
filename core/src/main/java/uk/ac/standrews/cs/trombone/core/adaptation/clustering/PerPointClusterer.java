package uk.ac.standrews.cs.trombone.core.adaptation.clustering;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.clustering.Clusterer;

/**
 * Clusters each given point into a {@link CentroidCluster} with the point as its center.
 *
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public final class PerPointClusterer<Point extends Clusterable> extends Clusterer<Point> {

    /** Constructs a new per point clusterer. */
    public PerPointClusterer() {

        super(null);
    }

    @Override
    public List<CentroidCluster<Point>> cluster(final Collection<Point> points) {

        return points.stream().map(this :: toCentroidCluster).collect(Collectors.toList());
    }

    @Override
    protected double distance(final Clusterable one, final Clusterable other) {

        throw new UnsupportedOperationException();
    }

    private CentroidCluster<Point> toCentroidCluster(final Point point) {

        final CentroidCluster<Point> cluster = new CentroidCluster<>(point);
        cluster.addPoint(point);
        return cluster;
    }
}
