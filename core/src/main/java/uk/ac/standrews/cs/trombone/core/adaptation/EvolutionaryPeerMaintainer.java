package uk.ac.standrews.cs.trombone.core.adaptation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.DoubleAdder;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.Clusterer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncommons.maths.random.MersenneTwisterRNG;
import org.uncommons.maths.random.Probability;
import uk.ac.standrews.cs.trombone.core.DisseminationStrategy;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerMaintainer;
import uk.ac.standrews.cs.trombone.core.PeerMetric;
import uk.ac.standrews.cs.trombone.core.util.CosineSimilarity;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class EvolutionaryPeerMaintainer extends PeerMaintainer {

    private static final Logger LOGGER = LoggerFactory.getLogger(EvolutionaryPeerMaintainer.class);
    protected final MersenneTwisterRNG random;
    protected final PeerMetric metric;
    protected final List<EvaluatedDisseminationStrategy> evaluated_strategies;
    protected final DoubleAdder total_fitness = new DoubleAdder();
    private final int population_size;
    private final int elite_count;
    private final Probability mutation_probability;
    private final long evolution_cycle_length;
    private final TimeUnit evolution_cycle_unit;
    private final Clusterer<EvaluatedDisseminationStrategy> clusterer;
    protected final DisseminationStrategyGenerator strategy_generator;
    private ScheduledFuture<?> evolution;
    private long first_start_millis;
    /** The order of fitness ins unnatural: smaller fitness value implies a fitter strategy. Therefore, reverse order comparator is used. */
    private static final Comparator<EvaluatedDisseminationStrategy> ASCENDING_FITNESS_COMPARATOR = Comparator.reverseOrder();
    private EvolutionaryMaintenanceFactory.TerminationCondition termination_condition;

    EvolutionaryPeerMaintainer(final Peer peer, ScheduledExecutorService scheduler,int population_size, int elite_count, Probability mutation_probability, long evolution_cycle_length, TimeUnit evolution_cycle_unit, Clusterer<EvaluatedDisseminationStrategy> clusterer, DisseminationStrategyGenerator strategy_generator) {

        super(peer, null, scheduler);
        this.population_size = population_size;
        this.elite_count = elite_count;
        this.mutation_probability = mutation_probability;
        this.evolution_cycle_length = evolution_cycle_length;
        this.evolution_cycle_unit = evolution_cycle_unit;
        this.clusterer = clusterer;
        this.strategy_generator = strategy_generator;
        random = peer.getRandom();
        metric = peer.getPeerMetric();
        evaluated_strategies = new ArrayList<>();
    }

    public List<EvaluatedDisseminationStrategy> getEvaluatedStrategies() {

        return new CopyOnWriteArrayList<>(evaluated_strategies);
    }

    @Override
    protected synchronized void start() {

        if (!isStarted()) {

            if (!hasStartedBefore()) {
                updateFirstStartTime();
            }

            evolution = scheduler.scheduleWithFixedDelay(new Runnable() {

                @Override
                public void run() {

                    try {
                        final EnvironmentSnapshot environment_snapshot = metric.getSnapshot();
                        final DisseminationStrategy previous_strategy = getDisseminationStrategy();
                        final DisseminationStrategy next_strategy = getNextStrategy(environment_snapshot, previous_strategy);
                        setDisseminationStrategy(next_strategy);
                        EvolutionaryMaintenanceFactory.STRATEGY_ACTION_SIZE_SAMPLER.update(next_strategy.size());
                    }
                    catch (Exception e) {
                        LOGGER.error("failed to perform adaptation cycle", e);
                    }
                }
            }, 0, evolution_cycle_length, evolution_cycle_unit);

            super.start();
        }
    }

    private boolean hasStartedBefore() {

        return first_start_millis > 0;
    }

    private void updateFirstStartTime() {

        first_start_millis = System.currentTimeMillis();
    }

    private synchronized DisseminationStrategy getNextStrategy(final EnvironmentSnapshot environment_snapshot, final DisseminationStrategy previous_strategy) {

        final DisseminationStrategy next_strategy;
        final boolean termination_condition_met = isTerminationConditionMet();

        EvaluatedDisseminationStrategy last_evaluated_strategy = null;
        if (previous_strategy != null && (!termination_condition_met || evaluated_strategies.size() < population_size)) {
            last_evaluated_strategy = addToEvaluatedStrategies(environment_snapshot, previous_strategy);
            EvolutionaryMaintenanceFactory.FITNESS_SAMPLER.update(last_evaluated_strategy.getFitness());
        }

        final int evaluated_strategies_size = evaluated_strategies.size();
        if (evaluated_strategies_size < population_size) {
            if (termination_condition_met) {
                LOGGER.warn("termination condition has been met but there are not enough evaluated strategies, population size: {}, evaluated size: {}", population_size, evaluated_strategies_size);
            }
            next_strategy = strategy_generator.generate(random);
        }
        else if (last_evaluated_strategy == null) {

            next_strategy = mostFit(evaluated_strategies).getStrategy();
        }
        else {
            assert last_evaluated_strategy != null;

            final Cluster<EvaluatedDisseminationStrategy> current_cluster = getCurrentEnvironmentCluster(last_evaluated_strategy);
            final List<EvaluatedDisseminationStrategy> current_cluster_points = current_cluster.getPoints();
            if (termination_condition_met) {
                next_strategy = mostFit(current_cluster_points).getStrategy();
            }
            else {
                next_strategy = generateNextStrategy(current_cluster);
            }

            if (evaluated_strategies.size() > population_size) {
                removeLeastFitFromCurrentCluster(current_cluster_points);
            }
        }
        return next_strategy;
    }

    private boolean isTerminationConditionMet() {

        return termination_condition != null ? termination_condition.terminate(this) : false;
    }

    protected EvaluatedDisseminationStrategy addToEvaluatedStrategies(final EnvironmentSnapshot environment_snapshot, final DisseminationStrategy previous_strategy) {

        final EvaluatedDisseminationStrategy evaluated_strategy = new EvaluatedDisseminationStrategy(previous_strategy, environment_snapshot);
        evaluated_strategies.add(evaluated_strategy);
        total_fitness.add(evaluated_strategy.getFitness());
        return evaluated_strategy;
    }

    protected Cluster<EvaluatedDisseminationStrategy> getCurrentEnvironmentCluster(final EvaluatedDisseminationStrategy last_evaluated_strategy) {

        final List<? extends Cluster<EvaluatedDisseminationStrategy>> clustering = clusterer.cluster(evaluated_strategies);
        EvolutionaryMaintenanceFactory.CLUSTER_COUNT_SAMPLER.update(clustering.size());
        Cluster<EvaluatedDisseminationStrategy> current_cluster = null;
        for (Cluster<EvaluatedDisseminationStrategy> cluster : clustering) {
            final List<EvaluatedDisseminationStrategy> cluster_points = cluster.getPoints();
            EvolutionaryMaintenanceFactory.CLUSTER_SIZE_SAMPLER.update(cluster_points.size());

            if (current_cluster == null && cluster_points.contains(last_evaluated_strategy)) {
                current_cluster = cluster;
            }
        }
        assert current_cluster != null;
        return current_cluster;
    }

    protected EvaluatedDisseminationStrategy mostFit(final List<EvaluatedDisseminationStrategy> current_cluster_points) {

        final Optional<EvaluatedDisseminationStrategy> max_fitness = current_cluster_points.stream().max(ASCENDING_FITNESS_COMPARATOR);
        return !max_fitness.isPresent() ? null : max_fitness.get();
    }

    protected DisseminationStrategy generateNextStrategy(final Cluster<EvaluatedDisseminationStrategy> current_cluster) {

        final List<EvaluatedDisseminationStrategy> current_cluster_points = current_cluster.getPoints();
        final double total_fitness = this.total_fitness.sum();
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

            EvolutionaryMaintenanceFactory.NORMALIZED_FITNESS_SAMPLER.update(normalized_fitness);
            EvolutionaryMaintenanceFactory.WEIGHTED_FITNESS_SAMPLER.update(weighted_fitness);
            index++;
        }

        final TreeMap<Double, EvaluatedDisseminationStrategy> cumulative_evaluated_strategies = getCumulativeWeightedFitness(total_weighted_fitness);
        final DisseminationStrategy one = select(cumulative_evaluated_strategies);
        final DisseminationStrategy other = select(cumulative_evaluated_strategies);
        final DisseminationStrategy offspring = strategy_generator.mate(one, other, random);

        strategy_generator.mutate(offspring, random, mutation_probability);
        return offspring;
    }

    protected void removeLeastFitFromCurrentCluster(final List<EvaluatedDisseminationStrategy> current_cluster_points) {

        final int evaluated_strategies_size = evaluated_strategies.size();
        if (evaluated_strategies_size > population_size) {
            final EvaluatedDisseminationStrategy least_fit = leastFit(current_cluster_points);
            removeFromEvaluatedStrategies(least_fit);
        }
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

    private DisseminationStrategy select(final TreeMap<Double, EvaluatedDisseminationStrategy> normalized_evaluated_strategies) {

        final double dice = random.nextDouble();
        final EvaluatedDisseminationStrategy selected = normalized_evaluated_strategies.ceilingEntry(dice).getValue();
        assert selected != null;
        return selected.getStrategy();
    }

    protected EvaluatedDisseminationStrategy leastFit(final List<EvaluatedDisseminationStrategy> current_cluster_points) {

        final Optional<EvaluatedDisseminationStrategy> min_fitness = current_cluster_points.stream().min(ASCENDING_FITNESS_COMPARATOR);
        return !min_fitness.isPresent() ? null : min_fitness.get();
    }

    protected boolean removeFromEvaluatedStrategies(final EvaluatedDisseminationStrategy evaluated_strategy) {

        final boolean removed = evaluated_strategies.remove(evaluated_strategy);
        if (removed) {
            total_fitness.add(-evaluated_strategy.getFitness());
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

    protected long getElapsedMillisecondsSinceFirstStart() {

        return hasStartedBefore() ? System.currentTimeMillis() - first_start_millis : 0;
    }
}
