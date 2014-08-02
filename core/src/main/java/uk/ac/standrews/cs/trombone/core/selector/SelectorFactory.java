package uk.ac.standrews.cs.trombone.core.selector;

import java.util.List;
import java.util.Random;

import static uk.ac.standrews.cs.trombone.core.selector.Selector.ReachabilityCriteria;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class SelectorFactory{

    private static final ReachabilityCriteria[] REACHABILITY_CRITERIA = ReachabilityCriteria.values();
    private static final int REACHABILITY_CRITERIA_LENGTH = REACHABILITY_CRITERIA.length;
    private final List<Selector> initial_selectors;
    private final int max_selection_size;

    public SelectorFactory(List<Selector> initial_selectors, int max_selection_size) {

        this.initial_selectors = initial_selectors;
        this.max_selection_size = max_selection_size;
    }

    public int getMaxSelectionSize() {

        return max_selection_size;
    }

    public Selector mutate(Selector selector, Random random) {

        final Selector mutant;
        final boolean same_type_as_selector = random.nextBoolean();

        if (same_type_as_selector) {
            mutant = selector.copy();
        }
        else {
            mutant = generate(random);
        }

        if (!selector.isSingleton()) {

            if (random.nextBoolean()) {
                final boolean same_as_selector = random.nextBoolean();
                mutant.size = same_as_selector ? selector.getSelectionSize() : getRandomSelectionSize(random);
            }

            if (random.nextBoolean()) {
                final boolean same_as_selector = random.nextBoolean();
                mutant.reachability_criteria = same_as_selector ? selector.getReachabilityCriteria() : getRandomReachabilityCriteria(random);
            }
        }

        return mutant;
    }

    public Selector generate(Random random) {

        final int selector_type_index = random.nextInt(initial_selectors.size());
        final Selector generated_selector = initial_selectors.get(selector_type_index).copy();
        if (!generated_selector.isSingleton()) {

            generated_selector.size = getRandomSelectionSize(random);
            generated_selector.reachability_criteria = getRandomReachabilityCriteria(random);
        }

        return generated_selector;
    }

    private int getRandomSelectionSize(final Random random) {

        return random.nextInt(max_selection_size - 1) + 1;
    }

    private static ReachabilityCriteria getRandomReachabilityCriteria(final Random random) {

        return REACHABILITY_CRITERIA[random.nextInt(REACHABILITY_CRITERIA_LENGTH)];
    }
}
