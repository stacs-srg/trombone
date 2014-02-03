package uk.ac.standrews.cs.trombone.core.adaptation;

import java.util.Arrays;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.trombone.core.DisseminationStrategy;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class EvaluatedDisseminationStrategy implements Comparable<EvaluatedDisseminationStrategy> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EvaluatedDisseminationStrategy.class);
    private static final double[] ORIGIN = {0, 0, 0};
    private final DisseminationStrategy strategy;
    private final double fitness;
    private final double[] environment;
    private final int hashcode;

    public EvaluatedDisseminationStrategy(final DisseminationStrategy strategy, EnvironmentSnapshot environment_snapshot) {

        this.strategy = strategy;
        fitness = calculateFitness(environment_snapshot);
        environment = getEnvironmentalValues(environment_snapshot);
        hashcode = new HashCodeBuilder(79, 31).append(fitness).append(strategy).append(environment).toHashCode();
    }

    public double getFitness() {

        return fitness;
    }

    public double[] getEnvironment() {

        return environment;
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
        return Double.compare(that.fitness, fitness) == 0 && strategy.equals(that.strategy) && Arrays.equals(environment, that.environment);
    }

    @Override
    public int hashCode() {

        return hashcode;
    }

    @Override
    public String toString() {

        final StringBuilder sb = new StringBuilder("EvaluatedDisseminationStrategy{");
        sb.append("strategy=").append(strategy);
        sb.append(", fitness=").append(fitness);
        sb.append(", environment=").append(Arrays.toString(environment));
        sb.append('}');
        return sb.toString();
    }

    public DisseminationStrategy getStrategy() {

        return strategy;
    }

    private static double[] getEnvironmentalValues(final EnvironmentSnapshot snapshot) {

        return new double[] {snapshot.getSentBytesPerSecond(), snapshot.getLookupCount(), snapshot.getNextHopCount(), snapshot.getReachableCount(), snapshot.getUnreachableCount()};
    }

    private double calculateFitness(final EnvironmentSnapshot environment_snapshot) {

        final double mean_lookup_success_delay_millis = environment_snapshot.getNormalizedMeanLookupSuccessDelayMillis();
        final double sent_bytes_rate_per_second = environment_snapshot.getNormalizedSentBytesRatePerSecond();
        final double lookup_failure_rate = environment_snapshot.getNormalizedLookupFailureRate();

        final double inverse_fitness = euclideanDistance(new double[] {mean_lookup_success_delay_millis, sent_bytes_rate_per_second, lookup_failure_rate}, ORIGIN);
        return inverse_fitness;
        //        if (inverse_fitness == 0) {
        //            LOGGER.warn("zero inverse fitness! have we found a perfect solution? {}", strategy);
        //            return Double.MAX_VALUE;
        //        }
        //
        //        return 1 / inverse_fitness;
    }

    private static double euclideanDistance(final double[] first_point, final double[] second_point) {

        assert first_point.length == second_point.length;
        double sum = 0;
        for (int i = 0; i < first_point.length; i++) {
            final double dp = first_point[i] - second_point[i];
            sum += dp * dp;
        }
        return Math.sqrt(sum);
    }
}
