package uk.ac.standrews.cs.trombone.core.adaptation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.uncommons.maths.random.Probability;
import uk.ac.standrews.cs.trombone.core.DisseminationStrategy;
import uk.ac.standrews.cs.trombone.core.selector.Selector;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class DisseminationStrategyGenerator {

    public static final Probability ACTION_FIELD_MUTATION_PROBABILITY = new Probability(1.0 / 4);
    private final int max_action;

    public DisseminationStrategyGenerator(int max_action) {

        if (max_action < 1) { throw new IllegalArgumentException("maximum number of actions must be at least 1"); }
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

        final int actions_size = random.nextInt(max_action + 1);
        final DisseminationStrategy strategy = new DisseminationStrategy();
        for (int i = 0; i < actions_size; i++) {
            strategy.addAction(generateAction(random));
        }

        return strategy;
    }

    public void mutate(DisseminationStrategy strategy, Random random, Probability mutation_probability) {

        final int size = strategy.size();
        final int mutation_index = random.nextInt(size);
        strategy.setActionAt(mutation_index, generateAction(random));
        final Probability action_mutation_chance = new Probability(mutation_probability.doubleValue() / size);

        for (DisseminationStrategy.Action action : strategy) {
            if (action_mutation_chance.nextEvent(random)) {
                mutate(action, random, ACTION_FIELD_MUTATION_PROBABILITY);
            }
        }
    }

    private void mutate(final DisseminationStrategy.Action action, final Random random, Probability mutation_probability) {

        if (mutation_probability.nextEvent(random)) {
            action.setPush(!action.isPush());
        }
        if (mutation_probability.nextEvent(random)) {
            action.setOpportunistic(!action.isOpportunistic());
        }
        if (mutation_probability.nextEvent(random)) {
            Selector recipient_selector = action.getRecipientSelector();
            action.setRecipientSelector(Selector.mutate(recipient_selector, random));
        }
        if (mutation_probability.nextEvent(random)) {
            Selector data_selector = action.getDataSelector();
            action.setDataSelector(Selector.mutate(data_selector, random));
        }
    }

    public DisseminationStrategy mate(DisseminationStrategy first, final DisseminationStrategy second, final Random random) {

        final DisseminationStrategy offspring = new DisseminationStrategy();
        final int first_size = first.size();
        final int second_size = second.size();

        final int min_size = Math.min(first_size, second_size);
        final int crossover_point = min_size != 0 ? random.nextInt(min_size) : 0;
        final boolean first_goes_first = random.nextBoolean();

        final DisseminationStrategy first_parent;
        final DisseminationStrategy second_parent;
        final int second_parent_size;
        if (first_goes_first) {
            first_parent = first;
            second_parent = second;
            second_parent_size = second_size;
        }
        else {
            first_parent = second;
            second_parent = first;
            second_parent_size = first_size;
        }

        offspring.addActions(first_parent.subActionList(0, crossover_point));
        offspring.addActions(second_parent.subActionList(crossover_point, second_parent_size));

        return offspring;
    }

    DisseminationStrategy.Action generateAction(final Random random) {

        final boolean opportunistic = random.nextBoolean();
        final boolean push = random.nextBoolean();
        final Selector data_selector = Selector.generate(random);
        final Selector recipient_selector = Selector.generate(random);

        return new DisseminationStrategy.Action(opportunistic, push, data_selector, recipient_selector);
    }
}
