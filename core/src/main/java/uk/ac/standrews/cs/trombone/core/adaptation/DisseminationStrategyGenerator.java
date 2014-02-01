package uk.ac.standrews.cs.trombone.core.adaptation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import uk.ac.standrews.cs.trombone.core.DisseminationStrategy;
import uk.ac.standrews.cs.trombone.core.selector.First;
import uk.ac.standrews.cs.trombone.core.selector.FirstReachable;
import uk.ac.standrews.cs.trombone.core.selector.Last;
import uk.ac.standrews.cs.trombone.core.selector.LastReachable;
import uk.ac.standrews.cs.trombone.core.selector.RandomSelector;
import uk.ac.standrews.cs.trombone.core.selector.Selector;
import uk.ac.standrews.cs.trombone.core.selector.Self;
import uk.ac.standrews.cs.trombone.core.util.SelectionUtil;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class DisseminationStrategyGenerator {

    private static final Set<Selector> SELECTORS = new HashSet<>();

    static {
        SELECTORS.add(First.getInstance());
        SELECTORS.add(FirstReachable.getInstance());
        SELECTORS.add(Last.getInstance());
        SELECTORS.add(LastReachable.getInstance());
        SELECTORS.add(Self.getInstance());
        SELECTORS.add(new RandomSelector(1));
        SELECTORS.add(new RandomSelector(2));
        SELECTORS.add(new RandomSelector(3));
        SELECTORS.add(new RandomSelector(4));
        SELECTORS.add(new RandomSelector(5));
    }

    private final int max_action;
    private final Set<Selector> selectors;

    public DisseminationStrategyGenerator(int max_action) {

        this(SELECTORS, max_action);
    }

    public DisseminationStrategyGenerator(Set<Selector> selectors, int max_action) {

        if (max_action < 1) { throw new IllegalArgumentException("maximum number of actions must be at least 1"); }
        this.selectors = selectors;
        this.max_action = max_action;
    }

    public List<DisseminationStrategy> generate(int count, final Random random) {

        final List<DisseminationStrategy> strategies = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            strategies.add(generate(random));
        }
        return strategies;
    }

    public DisseminationStrategy generate(final Random random) {

        final int actions_size = random.nextInt(max_action);
        final DisseminationStrategy strategy = new DisseminationStrategy();
        for (int i = 0; i < actions_size; i++) {
            strategy.addAction(generateAction(random));
        }
        return strategy;
    }

    public void mutate(DisseminationStrategy strategy, Random random) {

        final int mutation_index = random.nextInt(strategy.size());
        final DisseminationStrategy.Action action = generateAction(random);
        strategy.setActionAt(mutation_index, action);
    }

    public DisseminationStrategy mate(DisseminationStrategy first, final DisseminationStrategy second, final Random random) {

        final int second_size = second.size();
        final int crossover_point = random.nextInt(Math.min(first.size(), second_size));
        final DisseminationStrategy offspring = new DisseminationStrategy();

        offspring.addActions(first.subActionList(0, crossover_point));
        offspring.addActions(second.subActionList(crossover_point, second_size));

        return offspring;
    }

    DisseminationStrategy.Action generateAction(final Random random) {

        final boolean opportunistic = random.nextBoolean();
        final boolean push = random.nextBoolean();
        final Selector data_selector = SelectionUtil.selectRandomly(selectors, random);
        final Selector recipient_selector = SelectionUtil.selectRandomly(selectors, random);

        return new DisseminationStrategy.Action(opportunistic, push, data_selector, recipient_selector);
    }
}
