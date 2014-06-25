package uk.ac.standrews.cs.trombone.core.adaptation;

import java.util.concurrent.TimeUnit;
import org.apache.commons.math3.ml.clustering.Clusterer;
import org.mashti.gauge.Sampler;
import org.uncommons.maths.random.Probability;
import uk.ac.standrews.cs.trombone.core.MaintenanceFactory;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerMaintainer;
import uk.ac.standrews.cs.trombone.core.util.Named;
import uk.ac.standrews.cs.trombone.core.util.NamingUtils;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class EvolutionaryMaintenanceFactory extends MaintenanceFactory {

    public static final Sampler CLUSTER_COUNT_SAMPLER = new Sampler();
    public static final Sampler CLUSTER_SIZE_SAMPLER = new Sampler();
    public static final Sampler FITNESS_SAMPLER = new Sampler();
    public static final Sampler NORMALIZED_FITNESS_SAMPLER = new Sampler();
    public static final Sampler WEIGHTED_FITNESS_SAMPLER = new Sampler();
    public static final Sampler STRATEGY_ACTION_SIZE_SAMPLER = new Sampler();

    private static final long serialVersionUID = -3613808902480933796L;
    protected final int population_size;
    protected final Clusterer<EvaluatedDisseminationStrategy> clusterer;
    private final int elite_count;
    protected final Probability mutation_probability;
    protected final long evolution_cycle_length;
    protected final TimeUnit evolution_cycle_unit;
    protected final DisseminationStrategyGenerator strategy_generator;
    protected TerminationCondition termination_condition;

    public EvolutionaryMaintenanceFactory(int population_size, int elite_count, Probability mutation_probability, long evolution_cycle_length, TimeUnit evolution_cycle_unit, Clusterer<EvaluatedDisseminationStrategy> clusterer, int max_action_size, int max_selection_size) {

        this.population_size = population_size;
        this.elite_count = elite_count;
        this.mutation_probability = mutation_probability;
        this.evolution_cycle_length = evolution_cycle_length;
        this.evolution_cycle_unit = evolution_cycle_unit;
        this.clusterer = clusterer;
        strategy_generator = new DisseminationStrategyGenerator(max_action_size, max_selection_size);
    }

    public DisseminationStrategyGenerator getDisseminationStrategyGenerator() {

        return strategy_generator;
    }

    public int getPopulationSize() {

        return population_size;
    }

    public int getEliteCount() {

        return elite_count;
    }

    public double getMutationProbability() {

        return mutation_probability.doubleValue();
    }

    public long getEvolutionCycleLength() {

        return evolution_cycle_length;
    }

    public TimeUnit getEvolutionCycleLengthUnit() {

        return evolution_cycle_unit;
    }

    public Clusterer<EvaluatedDisseminationStrategy> getClusterer() {

        return clusterer;
    }

    public String getClustererName() {

        return NamingUtils.name(clusterer);
    }

    public TerminationCondition getTerminationCondition() {

        return termination_condition;
    }

    public void setTerminationCondition(final TerminationCondition termination_condition) {

        this.termination_condition = termination_condition;
    }

    @Override
    protected PeerMaintainer maintain(Peer peer) {

        final PeerMaintainer listener = new EvolutionaryPeerMaintainer(peer, SCHEDULER, population_size, elite_count, mutation_probability, evolution_cycle_length, evolution_cycle_unit, clusterer, strategy_generator);
        peer.addExposureChangeListener(listener);
        return listener;
    }

    public interface TerminationCondition {

        boolean terminate(EvolutionaryPeerMaintainer maintainer);
    }

    public static class ElapsedTimeTerminationCondition implements TerminationCondition, Named {

        private final long elapsed_time_millis;
        private final long duration;
        private final TimeUnit duration_unit;

        public ElapsedTimeTerminationCondition(long duration, TimeUnit duration_unit) {

            this.duration = duration;
            this.duration_unit = duration_unit;

            elapsed_time_millis = TimeUnit.MILLISECONDS.convert(duration, duration_unit);
        }

        @Override
        public boolean terminate(final EvolutionaryPeerMaintainer maintainer) {

            return maintainer.getElapsedMillisecondsSinceFirstStart() >= elapsed_time_millis;
        }

        @Override
        public String getName() {

            return NamingUtils.name(this);
        }

        public long getDuration() {

            return duration;
        }

        public TimeUnit getDurationUnit() {

            return duration_unit;
        }
    }

}
