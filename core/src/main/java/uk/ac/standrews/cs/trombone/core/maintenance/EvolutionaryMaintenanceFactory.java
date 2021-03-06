package uk.ac.standrews.cs.trombone.core.maintenance;

import java.util.concurrent.TimeUnit;
import org.apache.commons.math3.ml.clustering.Clusterer;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.util.Probability;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class EvolutionaryMaintenanceFactory implements MaintenanceFactory {

    private final Builder builder;

    public static class Builder {

        private int population_size;
        private int elite_count;
        private Probability mutation_probability;
        private long evaluation_length;
        private TimeUnit evaluation_length_unit;
        private Clusterer<EvaluatedDisseminationStrategy> clusterer;
        private DisseminationStrategyGenerator generator;
        private EvolutionaryMaintenance.TerminationCondition condition;
        private long interval;
        private TimeUnit interval_unit;

        protected Builder() {

        }

        protected Builder(final Builder base) {

            population_size = base.population_size;
            elite_count = base.elite_count;
            mutation_probability = base.mutation_probability;
            evaluation_length = base.evaluation_length;
            evaluation_length_unit = base.evaluation_length_unit;
            clusterer = base.clusterer;
            generator = base.generator;
            condition = base.condition;
            interval = base.interval;
            interval_unit = base.interval_unit;
        }

        public Builder populationSize(int population_size) {

            this.population_size = population_size;
            return this;
        }

        public Builder eliteCount(int elite_count) {

            this.elite_count = elite_count;
            return this;
        }

        public Builder mutationProbability(Probability mutation_probability) {

            this.mutation_probability = mutation_probability;
            return this;
        }

        public Builder evaluationDuration(long evaluation_length, final TimeUnit evaluation_length_unit) {

            this.evaluation_length = evaluation_length;
            this.evaluation_length_unit = evaluation_length_unit;

            return this;
        }

        public Builder clusterer(final Clusterer<EvaluatedDisseminationStrategy> clusterer) {

            this.clusterer = clusterer;
            return this;
        }

        public Builder disseminationStrategyGenerator(DisseminationStrategyGenerator generator) {

            this.generator = generator;
            return this;
        }

        public Builder terminationCondition(EvolutionaryMaintenance.TerminationCondition condition) {

            this.condition = condition;
            return this;
        }

        public Builder periodicMaintenanceInterval(long interval, TimeUnit interval_unit) {

            this.interval = interval;
            this.interval_unit = interval_unit;
            return this;
        }

        public EvolutionaryMaintenanceFactory build() {

            return new EvolutionaryMaintenanceFactory(this);
        }

    }

    public static Builder builder() {

        return new Builder();
    }

    public static Builder builder(Builder base) {

        return new Builder(base);
    }

    private EvolutionaryMaintenanceFactory(Builder builder) {

        this.builder = builder;
    }

    @Override
    public Maintenance apply(final Peer peer) {

        return new EvolutionaryMaintenance(peer, builder.population_size, builder.elite_count, builder.mutation_probability, builder.evaluation_length, builder.evaluation_length_unit, builder.clusterer, builder.generator, builder.condition, builder.interval, builder.interval_unit);
    }

    public int getPopulationSize() {

        return builder.population_size;
    }

    public int getEliteCount() {

        return builder.elite_count;
    }

    public Probability getMutationProbability() {

        return builder.mutation_probability;
    }

    public long getEvaluationLength() {

        return builder.evaluation_length;
    }

    public TimeUnit getEvaluationLengthUnit() {

        return builder.evaluation_length_unit;
    }

    public Clusterer<EvaluatedDisseminationStrategy> getClusterer() {

        return builder.clusterer;
    }

    public DisseminationStrategyGenerator getDisseminationStrategyGenerator() {

        return builder.generator;
    }

    public EvolutionaryMaintenance.TerminationCondition getTerminationCondition() {

        return builder.condition;
    }

    public long getInterval() {

        return builder.interval;
    }

    public TimeUnit getIntervalUnit() {

        return builder.interval_unit;
    }
}
