package uk.ac.standrews.cs.trombone.core.adaptation;

import com.google.common.util.concurrent.AtomicDouble;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncommons.maths.random.MersenneTwisterRNG;
import org.uncommons.maths.random.Probability;
import uk.ac.standrews.cs.trombone.core.DisseminationStrategy;
import uk.ac.standrews.cs.trombone.core.Maintenance;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerMetric;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class EvolutionaryMaintenance extends Maintenance {

    private static final Logger LOGGER = LoggerFactory.getLogger(EvolutionaryMaintenance.class);
    private static final Probability MUTATION_PROBABILITY = new Probability(0.1d);
    private static final int DISSEMINATION_STRATEGY_LIST_SIZE = 5;
    private static final DisseminationStrategyGenerator STRATEGY_GENERATOR = new DisseminationStrategyGenerator(DISSEMINATION_STRATEGY_LIST_SIZE);
    private static final int POPULATION_SIZE = 10;
    private static final int ELITE_COUNT = 2;
    private final MersenneTwisterRNG random;
    private final AtomicInteger current_candidate_index = new AtomicInteger();
    private final PeerMetric metric;
    private final List<DisseminationStrategy> population;
    private final TreeSet<EvaluatedDisseminationStrategy> evaluated_strategies;
    private final int population_size;
    private final int elite_count;
    private final Probability mutation_probability;
    private final AtomicDouble total_fitness = new AtomicDouble();
    private ScheduledFuture<?> evolution;

    public EvolutionaryMaintenance(final Peer local) {

        this(local, POPULATION_SIZE, ELITE_COUNT, MUTATION_PROBABILITY);
    }

    public EvolutionaryMaintenance(final Peer local, int population_size, int elite_count, Probability mutation_probability) {

        super(local);
        this.population_size = population_size;
        this.elite_count = elite_count;
        this.mutation_probability = mutation_probability;
        random = new MersenneTwisterRNG(local.getKey().getValue());
        metric = local.getPeerMetric();

        population = generateInitialPopulation();
        evaluated_strategies = new TreeSet<>();
    }

    @Override
    public synchronized boolean isStarted() {

        return evolution != null && !evolution.isDone() && super.isStarted();
    }

    @Override
    protected synchronized void start() {

        if (isStarted()) {
            return;
        }
        evolution = SCHEDULER.scheduleWithFixedDelay(new Runnable() {

            @Override
            public void run() {

                int index = current_candidate_index.getAndIncrement();
                if (index < population_size) {

                    final DisseminationStrategy new_candidate = population.get(index);
                    final EnvironmentSnapshot environment_snapshot = metric.getSnapshot();
                    final DisseminationStrategy old_candidate = getAndSet(new_candidate);

                    if (index > 0) {
                        final EvaluatedDisseminationStrategy evaluated_strategy = new EvaluatedDisseminationStrategy(old_candidate, environment_snapshot);
                        evaluated_strategies.add(evaluated_strategy);
                        total_fitness.addAndGet(evaluated_strategy.getFitness());
                    }
                }
                else {

                    population.clear();

                    final Iterator<EvaluatedDisseminationStrategy> iterator = evaluated_strategies.iterator();
                    while (iterator.hasNext() && population.size() < ELITE_COUNT) {
                        final EvaluatedDisseminationStrategy next_fittest = iterator.next();
                        population.add(next_fittest.getStrategy());
                    }

                    final TreeMap<Double, EvaluatedDisseminationStrategy> cumulative_evaluated_strategies = getCumulativeFitness();

                    for (int i = 0; i < population_size - elite_count; i++) {

                        final DisseminationStrategy one = select(cumulative_evaluated_strategies);
                        final DisseminationStrategy other = select(cumulative_evaluated_strategies);
                        final DisseminationStrategy offspring = STRATEGY_GENERATOR.mate(one, other, random);

                        if (mutation_probability.nextEvent(random)) {
                            STRATEGY_GENERATOR.mutate(offspring, random);
                        }
                        population.add(offspring);
                    }
                    current_candidate_index.set(0);
                    total_fitness.set(0);
                }
            }
        }, 0, 5, TimeUnit.MINUTES);

        super.start();
    }

    @Override
    protected synchronized void stop() {

        if (isStarted()) {
            evolution.cancel(true);
        }
        super.stop();
    }

    private List<DisseminationStrategy> generateInitialPopulation() {

        List<DisseminationStrategy> initial_population = new ArrayList<>();
        for (int i = 0; i < population_size; i++) {
            initial_population.add(STRATEGY_GENERATOR.generate(random));
        }

        return initial_population;
    }

    private DisseminationStrategy select(final TreeMap<Double, EvaluatedDisseminationStrategy> normalized_evaluated_strategies) {

        final double dice = random.nextDouble();
        final EvaluatedDisseminationStrategy selected = normalized_evaluated_strategies.ceilingEntry(dice).getValue();
        return selected.getStrategy();
    }

    private TreeMap<Double, EvaluatedDisseminationStrategy> getCumulativeFitness() {

        final TreeMap<Double, EvaluatedDisseminationStrategy> cumulative_evaluated_strategies = new TreeMap<>();

        final double total_fitness = this.total_fitness.get();
        double cumulative_normalized_fitness = 0;
        for (final EvaluatedDisseminationStrategy evaluated_strategy : evaluated_strategies) {
            final double fitness = evaluated_strategy.getFitness();
            final double normalized_fitness = fitness / total_fitness;
            cumulative_normalized_fitness += normalized_fitness;
            cumulative_evaluated_strategies.put(cumulative_normalized_fitness, evaluated_strategy);
        }
        return cumulative_evaluated_strategies;
    }
}
