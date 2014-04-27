package uk.ac.standrews.cs.trombone.core.adaptation;

import com.google.common.collect.Ordering;
import com.google.common.util.concurrent.AtomicDouble;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.Clusterer;
import org.mashti.gauge.Sampler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncommons.maths.random.MersenneTwisterRNG;
import org.uncommons.maths.random.Probability;
import uk.ac.standrews.cs.trombone.core.DisseminationStrategy;
import uk.ac.standrews.cs.trombone.core.Maintenance;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerMetric;
import uk.ac.standrews.cs.trombone.core.util.CosineSimilarity;
import uk.ac.standrews.cs.trombone.core.util.Named;
import uk.ac.standrews.cs.trombone.core.util.NamingUtils;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class EvolutionaryMaintenance extends Maintenance {

    public static final Sampler CLUSTER_COUNT_SAMPLER = new Sampler();
    public static final Sampler CLUSTER_SIZE_SAMPLER = new Sampler();
    public static final Sampler FITNESS_SAMPLER = new Sampler();
    public static final Sampler NORMALIZED_FITNESS_SAMPLER = new Sampler();
    public static final Sampler WEIGHTED_FITNESS_SAMPLER = new Sampler();
    public static final Sampler STRATEGY_ACTION_SIZE_SAMPLER = new Sampler();

    private static final long serialVersionUID = -3613808902480933796L;
    private static final int DISSEMINATION_STRATEGY_LIST_SIZE = 5;
    protected static final DisseminationStrategyGenerator STRATEGY_GENERATOR = new DisseminationStrategyGenerator(DISSEMINATION_STRATEGY_LIST_SIZE);
    private static final Ordering<EvaluatedDisseminationStrategy> EVALUATED_DISSEMINATION_STRATEGY_ORDERING = Ordering.natural();
    private static final Logger LOGGER = LoggerFactory.getLogger(EvolutionaryMaintenance.class);

    protected final int population_size;
    private final int elite_count;
    private final Probability mutation_probability;
    private final long evolution_cycle_length;
    private final TimeUnit evolution_cycle_unit;
    protected final Clusterer<EvaluatedDisseminationStrategy> clusterer;
    private TerminationCondition termination_condition;

    @Override
    protected PeerMaintainer maintain(Peer peer) {

        final PeerMaintainer listener = new EvolutionaryPeerMaintainer(peer);
        peer.addExposureChangeListener(listener);
        return listener;
    }

    public EvolutionaryMaintenance(int population_size, int elite_count, Probability mutation_probability, long evolution_cycle_length, TimeUnit evolution_cycle_unit, Clusterer<EvaluatedDisseminationStrategy> clusterer) {

        this.population_size = population_size;
        this.elite_count = elite_count;
        this.mutation_probability = mutation_probability;
        this.evolution_cycle_length = evolution_cycle_length;
        this.evolution_cycle_unit = evolution_cycle_unit;
        this.clusterer = clusterer;
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

    public class EvolutionaryPeerMaintainer extends PeerMaintainer {

        protected final MersenneTwisterRNG random;
        protected final PeerMetric metric;
        protected final List<EvaluatedDisseminationStrategy> evaluated_strategies;
        protected final AtomicDouble total_fitness = new AtomicDouble();
        private ScheduledFuture<?> evolution;
        private long first_start_millis;

        protected EvolutionaryPeerMaintainer(final Peer peer) {

            super(peer, null);
            random = peer.getRandom();
            metric = peer.getPeerMetric();
            evaluated_strategies = new ArrayList<>();
        }

        public List<EvaluatedDisseminationStrategy> getEvaluated_strategies() {

            return Collections.unmodifiableList(evaluated_strategies);
        }

        @Override
        protected synchronized void start() {

            if (!isStarted()) {

                if (!hasStartedBefore()) {
                    updateFirstStartTime();
                }

                evolution = SCHEDULER.scheduleWithFixedDelay(new Runnable() {

                    @Override
                    public void run() {

                        try {
                            final EnvironmentSnapshot environment_snapshot = metric.getSnapshot();
                            final DisseminationStrategy previous_strategy = getDisseminationStrategy();
                            final DisseminationStrategy next_strategy = getNextStrategy(environment_snapshot, previous_strategy);
                            setDisseminationStrategy(next_strategy);
                            STRATEGY_ACTION_SIZE_SAMPLER.update(next_strategy.size());
                        }
                        catch (Exception e) {
                            LOGGER.error("failed to perform adaptation cycle", e);
                        }
                    }
                }, 0, evolution_cycle_length, evolution_cycle_unit);

                super.start();
            }
        }

        protected long getElapsedMillisecondsSinceFirstStart() {

            return hasStartedBefore() ? System.currentTimeMillis() - first_start_millis : 0;
        }

        private void updateFirstStartTime() {

            first_start_millis = System.currentTimeMillis();
        }

        private boolean hasStartedBefore() {

            return first_start_millis > 0;
        }

        private synchronized DisseminationStrategy getNextStrategy(final EnvironmentSnapshot environment_snapshot, final DisseminationStrategy previous_strategy) {

            final DisseminationStrategy next_strategy;
            final boolean termination_condition_met = isTerminationConditionMet();

            EvaluatedDisseminationStrategy last_evaluated_strategy = null;
            if (previous_strategy != null && (!termination_condition_met || evaluated_strategies.size() < population_size)) {
                last_evaluated_strategy = addToEvaluatedStrategies(environment_snapshot, previous_strategy);
            }

            final int evaluated_strategies_size = evaluated_strategies.size();
            if (evaluated_strategies_size < population_size) {
                if (termination_condition_met) {
                    LOGGER.warn("termination condition has been met but there are not enough evaluated strategies, population size: {}, evaluated size: {}", population_size, evaluated_strategies_size);
                }
                next_strategy = STRATEGY_GENERATOR.generate(random);
            }
            else if (last_evaluated_strategy == null) {
                Collections.sort(evaluated_strategies, EVALUATED_DISSEMINATION_STRATEGY_ORDERING);
                next_strategy = evaluated_strategies.get(0).getStrategy();
            }
            else {
                assert last_evaluated_strategy != null;
                Collections.sort(evaluated_strategies, EVALUATED_DISSEMINATION_STRATEGY_ORDERING);

                final Cluster<EvaluatedDisseminationStrategy> current_cluster = getCurrentEnvironmentCluster(last_evaluated_strategy);
                final List<EvaluatedDisseminationStrategy> current_cluster_points = current_cluster.getPoints();
                if (termination_condition_met) {
                    next_strategy = mostFit(current_cluster_points).getStrategy();
                }
                else {
                    next_strategy = generateNextStrategy(current_cluster);
                }

                if (evaluated_strategies.size() < population_size) {
                    removeLeastFitFromCurrentCluster(current_cluster_points);
                }
            }
            return next_strategy;
        }

        private boolean isTerminationConditionMet() {

            return termination_condition != null ? termination_condition.terminate(this) : false;
        }

        protected DisseminationStrategy generateNextStrategy(final Cluster<EvaluatedDisseminationStrategy> current_cluster) {

            final List<EvaluatedDisseminationStrategy> current_cluster_points = current_cluster.getPoints();
            final double total_fitness = this.total_fitness.get();
            int index = 0;
            double total_weighted_fitness = 0;

            for (EvaluatedDisseminationStrategy evaluated_strategy : evaluated_strategies) {

                final double normalized_fitness = evaluated_strategy.getNormalizedFitness(total_fitness);
                final double weighted_fitness;

                if (index < elite_count || current_cluster_points.contains(evaluated_strategy)) {
                    weighted_fitness = normalized_fitness;
                }
                else {
                    final double similarity = CosineSimilarity.getSimilarity(evaluated_strategy, current_cluster);
                    weighted_fitness = Double.isNaN(similarity) ? normalized_fitness : normalized_fitness * similarity;
                }

                total_weighted_fitness += weighted_fitness;
                evaluated_strategy.setWeightedFitness(weighted_fitness);

                FITNESS_SAMPLER.update(evaluated_strategy.getFitness());
                NORMALIZED_FITNESS_SAMPLER.update(normalized_fitness);
                WEIGHTED_FITNESS_SAMPLER.update(weighted_fitness);
                index++;
            }

            final TreeMap<Double, EvaluatedDisseminationStrategy> cumulative_evaluated_strategies = getCumulativeWeightedFitness(total_weighted_fitness);
            final DisseminationStrategy one = select(cumulative_evaluated_strategies);
            final DisseminationStrategy other = select(cumulative_evaluated_strategies);
            final DisseminationStrategy offspring = STRATEGY_GENERATOR.mate(one, other, random);

            STRATEGY_GENERATOR.mutate(offspring, random, mutation_probability);
            return offspring;
        }

        protected Cluster<EvaluatedDisseminationStrategy> getCurrentEnvironmentCluster(final EvaluatedDisseminationStrategy last_evaluated_strategy) {

            final List<? extends Cluster<EvaluatedDisseminationStrategy>> clustering = clusterer.cluster(evaluated_strategies);
            CLUSTER_COUNT_SAMPLER.update(clustering.size());
            Cluster<EvaluatedDisseminationStrategy> current_cluster = null;
            for (Cluster<EvaluatedDisseminationStrategy> cluster : clustering) {
                final List<EvaluatedDisseminationStrategy> cluster_points = cluster.getPoints();
                CLUSTER_SIZE_SAMPLER.update(cluster_points.size());

                if (current_cluster == null && cluster_points.contains(last_evaluated_strategy)) {
                    current_cluster = cluster;
                }
            }
            assert current_cluster != null;
            return current_cluster;
        }

        protected EvaluatedDisseminationStrategy leastFit(final List<EvaluatedDisseminationStrategy> current_cluster_points) {

            return current_cluster_points.isEmpty() ? null : EVALUATED_DISSEMINATION_STRATEGY_ORDERING.greatestOf(current_cluster_points, 1).get(0);
        }

        protected EvaluatedDisseminationStrategy mostFit(final List<EvaluatedDisseminationStrategy> current_cluster_points) {

            return current_cluster_points.isEmpty() ? null : EVALUATED_DISSEMINATION_STRATEGY_ORDERING.leastOf(current_cluster_points, 1).get(0);
        }

        protected EvaluatedDisseminationStrategy addToEvaluatedStrategies(final EnvironmentSnapshot environment_snapshot, final DisseminationStrategy previous_strategy) {

            final EvaluatedDisseminationStrategy evaluated_strategy = new EvaluatedDisseminationStrategy(previous_strategy, environment_snapshot);
            evaluated_strategies.add(evaluated_strategy);
            total_fitness.addAndGet(evaluated_strategy.getFitness());
            return evaluated_strategy;
        }

        protected boolean removeFromEvaluatedStrategies(final EvaluatedDisseminationStrategy evaluated_strategy) {

            final boolean removed = evaluated_strategies.remove(evaluated_strategy);
            if (removed) {
                total_fitness.addAndGet(-evaluated_strategy.getFitness());
            }
            return removed;
        }

        @Override
        protected synchronized void stop() {

            if (isStarted()) {
                evolution.cancel(true);
                final EnvironmentSnapshot environment_snapshot = metric.getSnapshot();
                final DisseminationStrategy previous_strategy = getDisseminationStrategy();
                getNextStrategy(environment_snapshot, previous_strategy);
                setDisseminationStrategy(null);
                super.stop();
            }
        }

        protected void removeLeastFitFromCurrentCluster(final List<EvaluatedDisseminationStrategy> current_cluster_points) {

            final int evaluated_strategies_size = evaluated_strategies.size();
            if (evaluated_strategies_size > population_size) {
                final EvaluatedDisseminationStrategy least_fit = leastFit(current_cluster_points);
                removeFromEvaluatedStrategies(least_fit);
            }
        }

        private DisseminationStrategy select(final TreeMap<Double, EvaluatedDisseminationStrategy> normalized_evaluated_strategies) {

            final double dice = random.nextDouble();
            final EvaluatedDisseminationStrategy selected = normalized_evaluated_strategies.ceilingEntry(dice).getValue();
            assert selected != null;
            return selected.getStrategy();
        }

        private TreeMap<Double, EvaluatedDisseminationStrategy> getCumulativeWeightedFitness(final double total_weighted_fitness) {

            final TreeMap<Double, EvaluatedDisseminationStrategy> cumulative_weighted_fitness = new TreeMap<>();
            double cumulative_normalized_fitness = 0;
            for (final EvaluatedDisseminationStrategy evaluated_strategy : evaluated_strategies) {
                final double weighted_fitness = evaluated_strategy.getWeightedFitness();
                final double normalized_fitness = weighted_fitness / total_weighted_fitness;
                cumulative_normalized_fitness += normalized_fitness;
                cumulative_weighted_fitness.put(cumulative_normalized_fitness, evaluated_strategy);
            }
            return cumulative_weighted_fitness;
        }
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
