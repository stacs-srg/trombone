package uk.ac.standrews.cs.trombone.core.maintenance;

import java.util.concurrent.TimeUnit;
import org.apache.commons.math3.ml.clustering.Clusterer;
import uk.ac.standrews.cs.trombone.core.Peer;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class RandomMaintenanceFactory implements MaintenanceFactory {

    private final Builder builder;

    public static class Builder {

        private int population_size;
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

        public RandomMaintenanceFactory build() {

            return new RandomMaintenanceFactory(this);
        }

    }

    private RandomMaintenanceFactory(Builder builder) {

        this.builder = builder;
    }

    public static Builder builder() {

        return new Builder();
    }

    public static Builder builder(Builder base) {

        return new Builder(base);
    }

    @Override
    public RandomMaintenance apply(final Peer peer) {

        return new RandomMaintenance(peer, builder.population_size, builder.evaluation_length, builder.evaluation_length_unit, builder.clusterer, builder.generator, builder.condition, builder.interval, builder.interval_unit);
    }
}
