/**
 * Copyright (C) 2013  Khadija Musayeva, Lazaros Mavridis
 * University of St Andrews
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.standrews.cs.trombone.core.maintenance.pfclust;

import java.util.ArrayList;
import org.apache.commons.math3.linear.RealMatrix;

/**
 * Provides the functionality of parallel computation of the clustering procedure.
 */

public class ClusterConstruction {

    /**
     * Creates a number of Thread objects equal to the number of thresholds.
     * Invokes these threads and stores the obtained clustering from each thread in the collection.
     * Retrieves the best clustering from this collection.
     *
     * @param matrix
     * @param thresholds
     * @return the best clustering in the collection of clusterings
     */
    public static Clustering perform(final RealMatrix matrix, final ArrayList<Double> thresholds) {

        ClusteringCollection clusteringCollection = new ClusteringCollection();

        for (Double threshold : thresholds) {
            clusteringCollection.add(buildClustering(matrix, threshold));
        }

        if (clusteringCollection.size() > 1) {clusteringCollection.sort();}
        return clusteringCollection.getBestClustering();

    }

    /**
     * Builds clusters according to the specified threshold.
     *
     * @return a clustering built from the given threshold
     */
    public static Clustering buildClustering(final RealMatrix matrix, final double threshold) {

        final Clustering clustering = new Clustering(matrix);

        final ArrayList<Integer> unClusteredElements = new ArrayList<Integer>();
        for (int j = 0; j < matrix.getRowDimension(); ++j) {
            unClusteredElements.add(j);
        }

        //Finds the two most similar elements i and j from a similarity matrix
        int[] elements = findMostSimilarElements(matrix, unClusteredElements, threshold);

        //If the similarity value is greater than the given threshold then creates a cluster holding these elements.
        while (elements != null) {

            Cluster cluster = new Cluster(matrix);
            cluster.addElement(elements[0]);
            cluster.addElement(elements[1]);

            //Removes clustered elements from the list of not clustered elements.
            unClusteredElements.remove((Integer) elements[0]);
            unClusteredElements.remove((Integer) elements[1]);

            //Adds the most similar to these cluster elements.
            addClosestPointsToCluster(cluster, threshold, unClusteredElements);

            clustering.addCluster(cluster);

            elements = findMostSimilarElements(matrix, unClusteredElements, threshold);
        }

        //Assigns singletons to their clusters.
        if (!unClusteredElements.isEmpty()) {
            int numberOfSingletons = 0;

            for (Integer unClusteredElement : unClusteredElements) {
                Cluster cluster = new Cluster(matrix);
                cluster.addElement(unClusteredElement);
                clustering.addCluster(cluster);
                numberOfSingletons += 1;
            }
            clustering.setNumberOfSingletons(numberOfSingletons);
        }

        clustering.refine(threshold);
        clustering.setThreshold(threshold);
        clustering.calculateSilhouette();
        clustering.calculateDunnIndex();

        return clustering;

    }

    /**
     * Finds the two most similar unclustered elements from the provided list of elements.
     * Return null if their similarity is less than the given threshold.
     *
     * @param matrix
     * @param unClusteredElements
     * @param threshold
     * @return an array containing the two most similar elements, null if their similarity is less
     * than the given threshold
     */
    private static int[] findMostSimilarElements(final RealMatrix matrix, ArrayList<Integer> unClusteredElements, double threshold) {

        double value = 0;
        int[] elements = new int[2];

        final int unclustered_size = unClusteredElements.size();

        for (int k = 0; k < unclustered_size; k++) {

            int kElement = unClusteredElements.get(k);

            for (int j = k + 1; j < unclustered_size; ++j) {

                int jElement = unClusteredElements.get(j);
                if (value < matrix.getEntry(kElement, jElement)) {
                    value = matrix.getEntry(kElement, jElement);
                    elements[0] = kElement;
                    elements[1] = jElement;
                }
            }
        }

        return value >= threshold ? elements : null;

    }

    /**
     * Finds the most similar element to a given cluster according to the sum
     * of similarities between the selected element and each member of the cluster.
     * If this value is greater than the specified threshold
     * and if the mean similarity of a given cluster including the selected element is greater than P% cutoff of
     * the threshold, then adds this element to the given cluster.
     *
     * @param cluster
     * @param threshold
     * @param unClusteredElements
     */
    private static void addClosestPointsToCluster(final Cluster cluster, final double threshold, ArrayList<Integer> unClusteredElements) {

        double maxSimilarity = -1;
        int maxSimilarElement = 0;

        for (Integer unClusteredElement : unClusteredElements) {

            int element = unClusteredElement;
            double elementClusterSimilarity = cluster.calculateElementToClusterSimilarity(element);

            if (maxSimilarity < elementClusterSimilarity) {
                maxSimilarity = elementClusterSimilarity;
                maxSimilarElement = element;
            }
        }

        if (Double.compare(maxSimilarity, -1) != 0) {
            final int cluster_size = cluster.size();
            int denominator = cluster_size * (cluster_size + 1) / 2;
            double meanClusterSimilarity = (cluster.calculateSum() + maxSimilarity) / denominator;
            if (maxSimilarity >= cluster_size * PFClustClusterer.P * threshold && meanClusterSimilarity >= threshold) {
                cluster.addElement(maxSimilarElement);
                cluster.setSimilarity(meanClusterSimilarity);
                unClusteredElements.remove((Integer) maxSimilarElement);
                addClosestPointsToCluster(cluster, threshold, unClusteredElements);
            }
        }

    }

}
