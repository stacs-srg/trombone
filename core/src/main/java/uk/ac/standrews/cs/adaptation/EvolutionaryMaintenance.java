package uk.ac.standrews.cs.adaptation;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncommons.maths.random.MersenneTwisterRNG;
import org.uncommons.maths.random.Probability;
import org.uncommons.watchmaker.framework.AbstractEvolutionEngine;
import org.uncommons.watchmaker.framework.GenerationalEvolutionEngine;
import org.uncommons.watchmaker.framework.SelectionStrategy;
import org.uncommons.watchmaker.framework.TerminationCondition;
import org.uncommons.watchmaker.framework.selection.RouletteWheelSelection;
import uk.ac.standrews.cs.trombone.core.DisseminationStrategy;
import uk.ac.standrews.cs.trombone.core.Maintenance;
import uk.ac.standrews.cs.trombone.core.Peer;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class EvolutionaryMaintenance {

    private static final Logger LOGGER = LoggerFactory.getLogger(EvolutionaryMaintenance.class);
    private static final Probability MUTATION_PROBABILITY = new Probability(0.1d);
    private static final int DISSEMINATION_STRATEGY_LIST_SIZE = 1;
    private static final DisseminationStrategyFactory DISSEMINATION_STRATEGY_FACTORY = new DisseminationStrategyFactory();
    private static final ListCandidateFactory<DisseminationStrategy> DISSEMINATION_STRATEGY_LIST_FACTORY = new ListCandidateFactory<>(DISSEMINATION_STRATEGY_FACTORY, DISSEMINATION_STRATEGY_LIST_SIZE);
    private static final int POPULATION_SIZE = 10;
    private static final int ELITE_COUNT = 2;
    private final Peer local;
    private final Maintenance maintenance;
    private final AbstractEvolutionEngine<List<DisseminationStrategy>> evolution_engine;

    public EvolutionaryMaintenance(final Peer local) {

        this.local = local;
        maintenance = local.getMaintenance();

        final ListMutation<DisseminationStrategy> operator = new ListMutation<>(DISSEMINATION_STRATEGY_FACTORY, MUTATION_PROBABILITY);
        final PVCFitnessEvaluator fitness_evaluator = new PVCFitnessEvaluator(local);
        final SelectionStrategy<Object> selection_strategy = new RouletteWheelSelection();
        final MersenneTwisterRNG random = new MersenneTwisterRNG();

        evolution_engine = new GenerationalEvolutionEngine<List<DisseminationStrategy>>(DISSEMINATION_STRATEGY_LIST_FACTORY, operator, fitness_evaluator, selection_strategy, random);
        evolution_engine.setSingleThreaded(true);
    }

    public void adapt(TerminationCondition... termination_conditions) {

        final List<DisseminationStrategy> current_best = evolution_engine.evolve(POPULATION_SIZE, ELITE_COUNT, termination_conditions);
        LOGGER.info("at least one termination condition is met; terminating adaptation");

        //TODO this is incomplete; here we should have a map of environment to solution.
        maintenance.reset();
        maintenance.addAll(current_best);

    }
}
