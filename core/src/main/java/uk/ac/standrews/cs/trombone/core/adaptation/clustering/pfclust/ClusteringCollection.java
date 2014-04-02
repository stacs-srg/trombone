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

package uk.ac.standrews.cs.trombone.core.adaptation.clustering.pfclust;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 * An instance of this class contains Clustering objects.
 * Provides the functionality to sort the list of clusterings and calculate the average Rand Index of the list.
 */

public class ClusteringCollection implements Iterable<Clustering> {

    private ArrayList<Clustering> clusteringList;

    /**
     * Instantiates a ClusteringCollection object
     */
    public ClusteringCollection() {

        clusteringList = new ArrayList<Clustering>();
    }

    /**
     * Adds the specified Clustering object to the list
     *
     * @param clustering
     */
    public void add(final Clustering clustering) {

        clusteringList.add(clustering);
    }

    /**
     * @return a list of Clustering objects
     */
    public ArrayList<Clustering> getClusteringList() {

        return clusteringList;
    }

    /**
     * Updates the list of Clustering objects
     *
     * @param clusteringList
     */
    public void setClusteringList(ArrayList<Clustering> clusteringList) {

        this.clusteringList = clusteringList;
    }

    /**
     * Sorts the list of Clustering objects in ascending order
     */
    public void sort() {

        Collections.sort(clusteringList);
    }

    /**
     * Removes the specified clustering from the list
     *
     * @param clustering
     */
    public void remove(Clustering clustering) {

        clusteringList.remove(clustering);
    }

    /**
     * Removes the Clustering object at the specified position from the list
     *
     * @param index
     */
    public void remove(final int index) {

        clusteringList.remove(index);
    }

    /**
     * Retrieves the best clustering.
     * In the sorted list of clusterings (ascending order) this is the last element.
     *
     * @return the best clustering
     */
    public Clustering getBestClustering() {

        return clusteringList.get(size() - 1);
    }

    /**
     * @return the number of clusterings in the list
     */
    public int size() {

        return clusteringList.size();
    }

    /**
     * Retrieves the worst clustering.
     * In the sorted list of clusterings (ascending order) this is the first element.
     *
     * @return the worst clustering
     */
    public Clustering getWorstClustering() {

        return clusteringList.get(0);
    }

    /**
     * Calculate the average value of Rand index of the list of clusterings.
     *
     * @return the average Rand index of the list
     */
    public double getRandIndex() {

        double randIndex = 0;
        double size = size();

        for (int i = 0; i < size() - 1; ++i) {
            Clustering clustering = get(i);
            for (int m = i + 1; m < size(); ++m) {
                Clustering otherClustering = get(m);
                randIndex += clustering.getRandIndex(otherClustering);
            }
        }

        randIndex /= size * (size - 1) / 2;
        return randIndex;
    }

    /**
     * @return returns the clustering at the specified index
     */
    public Clustering get(final int index) {

        return clusteringList.get(index);
    }

    @Override
    public Iterator<Clustering> iterator() {

        return clusteringList.iterator();
    }
}
