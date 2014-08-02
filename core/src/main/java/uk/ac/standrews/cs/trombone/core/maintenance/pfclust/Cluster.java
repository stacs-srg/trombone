package uk.ac.standrews.cs.trombone.core.maintenance.pfclust;

import java.util.ArrayList;
import org.apache.commons.math3.linear.RealMatrix;

/**
 * Represents a cluster defined by a list of elements.
 * Provides methods to calculate cluster similarity, centroid and radius
 * along with other elementary list operations.
 */

public class Cluster {

    private ArrayList<Integer> members;

    /** The similarity matrix. */
    private RealMatrix matrix;

    /**
     * Represents cluster similarity
     */
    private double similarity;

    /**
     * Initializes the list of elements
     * Initializes the matrix object
     *
     * @param matrix
     */
    public Cluster(final RealMatrix matrix) {

        members = new ArrayList<Integer>();
        this.matrix = matrix;
    }

    /**
     * Adds an element to the list of elements
     *
     * @param element
     */
    public void addElement(final int element) {

        members.add(element);
    }

    /**
     * Retrieves the element at the specified index
     *
     * @param index
     * @return an element at the specified index.
     */
    public int getElementAt(final int index) {

        return members.get(index);
    }

    /**
     * Returns true if a cluster contains the specified element
     *
     * @param element
     * @return true if a cluster contains the specified element
     */
    public boolean isMember(final int element) {

        for (int i = 0; i < size(); ++i) {
            if (members.get(i) == element) {
                return true;
            }
        }
        return false;
    }
    /**
     * Calculates cluster similarity
     */
    public void calculateSimilarity() {

        int n = size();

        if (n < 3) {
            similarity = -1;
        }

        else {
            double sumOfSimilarities = calculateSum();
            similarity = 2d / ((n - 1) * n) * sumOfSimilarities;
        }
    }

    /**
     * The similarity of a given element to the cluster
     * is calculated as the sum of similarities between the element
     * and each member of the cluster.
     *
     * @param element
     * @return the similarity of a given element to the cluster.
     */
    public double calculateElementToClusterSimilarity(int element) {

        double sumOfSimilarity = 0;
        for (Integer member : members) {
            if (element == member) {continue;}
            sumOfSimilarity += matrix.getEntry(element, member);
        }
        return sumOfSimilarity;
    }

    public double calculateElementToCentroidSimilarity(int element) {

        return matrix.getEntry(element, getCentroid());
    }

    /**
     * Computes the average value of the sum of similarities between
     * the given element and the cluster.
     *
     * @param element
     * @return the average value of element to cluster similarity.
     */
    public double calculateElementToClusterAverageSimilarity(int element) {

        double sumOfSimilarity = calculateElementToClusterSimilarity(element);
        return sumOfSimilarity / (size() - 1);
    }

    /**
     * Retrieves the similarity of the cluster
     *
     * @return
     */
    public double getSimilarity() {

        return similarity;
    }

    /**
     * Updates the similarity of the cluster
     *
     * @param similarity
     */
    public void setSimilarity(final double similarity) {

        this.similarity = similarity;
    }

    /**
     * @return a list of cluster members
     */
    public ArrayList<Integer> getMembers() {

        return members;
    }

    /**
     * @return the number of elements in the cluster
     */
    public int size() {

        return members.size();
    }

    /**
     * Removes the element from the list of elements at the specified position
     *
     * @param member
     */
    public void removeMember(int member) {

        members.remove((Integer) member);
    }

    /**
     * The radius of the cluster is calculated as the average of the distance between
     * the cluster centroid and its elements.
     *
     * @return averaged distance between the cluster centroid and cluster elements
     */
    public double getClusterRadius() {

        int centroid = getCentroid();
        double radius = 0;

        for (int i = 0; i < size(); ++i) {

            int element = getElementAt(i);
            if (centroid == element) {continue;}
            radius += 1 - matrix.getEntry(element, centroid);

        }

        radius = radius / (size() - 1);
        return radius;
    }

    /**
     * The centroid of the cluster is the element with the highest similarity to its cluster.
     *
     * @return an element with the highest similarity to its cluster
     */
    public int getCentroid() {

        double maxSimilarity = -1;
        int centroid = -1;

        for (int i = 0; i < size(); ++i) {
            int element = getElementAt(i);
            double similarity = calculateElementToClusterSimilarity(element);

            if (maxSimilarity < similarity) {
                maxSimilarity = similarity;
                centroid = element;
            }
        }

        return centroid;
    }

    /**
     * Calculates the sum of pair-wise similarities between cluster members
     *
     * @return sum of pair-wise similarities between cluster members
     */
    public double calculateSum() {


            double sumOfSimilarities = 0;
            for (int i = 1; i < size(); i++) {
                for (int k = 0; k < i; k++) {

                    final Integer row = members.get(i);
                    final Integer column = members.get(k);
                    sumOfSimilarities += matrix.getEntry(row, column);
                }
            }
        return sumOfSimilarities;
    }

    /**
     * Represents the string representation of the cluster
     */
    public String toString() {

        String output = "";

        for (int j = 0; j < members.size(); ++j) {
            output += members.get(j);
            if (j != members.size() - 1) {
                output += ",";
            }

        }
        return output;
    }
}