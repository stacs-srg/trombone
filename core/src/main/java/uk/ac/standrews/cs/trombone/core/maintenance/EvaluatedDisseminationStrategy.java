package uk.ac.standrews.cs.trombone.core.maintenance;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.distance.EuclideanDistance;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class EvaluatedDisseminationStrategy implements Comparable<EvaluatedDisseminationStrategy>, Clusterable {

    private static final double[] ORIGIN = {0, 0, 0};
    private static final EuclideanDistance EUCLIDEAN_DISTANCE = new EuclideanDistance();
    private final DisseminationStrategy strategy;
    private final double fitness;
    private final EnvironmentSnapshot environment;
    private final int hashcode;
    private double weighted_fitness;
    private final double[] fitness_point;

    public EvaluatedDisseminationStrategy(final DisseminationStrategy strategy, EnvironmentSnapshot environment_snapshot) {

        this.strategy = strategy;
        environment = environment_snapshot;
        fitness_point = getFitnessPoint(environment_snapshot);
        fitness = calculateFitness(fitness_point);
        hashcode = new HashCodeBuilder(79, 31).append(fitness).append(strategy).append(environment).append(strategy).toHashCode();
    }

    public double getFitness() {

        return fitness;
    }

    double getNormalizedFitness(double total) {

        return total != 0 ? 1 - fitness / total : 1;
    }

    @Override
    public int compareTo(final EvaluatedDisseminationStrategy other) {

        return Double.compare(fitness, other.fitness);
    }

    @Override
    public boolean equals(final Object other) {

        if (this == other) { return true; }
        if (!(other instanceof EvaluatedDisseminationStrategy)) { return false; }
        final EvaluatedDisseminationStrategy that = (EvaluatedDisseminationStrategy) other;
        return Double.compare(that.fitness, fitness) == 0 && strategy.equals(that.strategy) && environment.equals(that.environment);
    }

    @Override
    public int hashCode() {

        return hashcode;
    }

    @Override
    public String toString() {

        return "EvaluatedDisseminationStrategy{" + "strategy=" + strategy + ", fitness=" + fitness + '}';
    }

    public DisseminationStrategy getStrategy() {

        return strategy;
    }

    @Override
    public double[] getPoint() {

        return environment.getPoint();
    }

    public double[] getFitnessPoint() {

        return fitness_point;
    }

    void setWeightedFitness(final double weighted_fitness) {

        this.weighted_fitness = weighted_fitness;
    }

    double getWeightedFitness() {

        return weighted_fitness;
    }

    private static double[] getFitnessPoint(final EnvironmentSnapshot environment_snapshot) {

        final double mean_lookup_success_delay_millis = environment_snapshot.getNormalizedMeanLookupSuccessDelayMillis();
        final double sent_bytes_rate_per_second = environment_snapshot.getNormalizedSentBytesRatePerSecond();
        final double lookup_failure_rate = environment_snapshot.getNormalizedLookupFailureRate();
        return new double[] {mean_lookup_success_delay_millis, sent_bytes_rate_per_second, lookup_failure_rate};
    }

    private static double calculateFitness(final double[] fitness_point) {

        return EUCLIDEAN_DISTANCE.compute(fitness_point, ORIGIN);
    }
}
