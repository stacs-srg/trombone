package uk.ac.standrews.cs.trombone.core.selector;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerReference;
import uk.ac.standrews.cs.trombone.core.util.Copyable;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public abstract class Selector implements Serializable, Copyable {

    private static final long serialVersionUID = -1994233167230411201L;
    private static final int MAX_SELECTION_SIZE = 3;
    private static final ReachabilityCriteria[] REACHABILITY_CRITERIA = ReachabilityCriteria.values();
    private static final int REACHABILITY_CRITERIA_LENGTH = REACHABILITY_CRITERIA.length;
    private static final List<Selector> SELECTORS = new ArrayList<>();
    private static final int SELECTORS_SIZE = SELECTORS.size();

    static {

        SELECTORS.add(EmptySelector.getInstance());
        SELECTORS.add(new First(1, ReachabilityCriteria.REACHABLE_OR_UNREACHABLE));
        SELECTORS.add(new Last(1, ReachabilityCriteria.REACHABLE_OR_UNREACHABLE));
        SELECTORS.add(new MostRecentlySeen(1, ReachabilityCriteria.REACHABLE_OR_UNREACHABLE));
        SELECTORS.add(new RandomSelector(1, ReachabilityCriteria.REACHABLE_OR_UNREACHABLE));
        SELECTORS.add(Self.getInstance());
    }

    protected int size;
    protected ReachabilityCriteria reachability_criteria;

    protected Selector(int size, ReachabilityCriteria reachability_criteria) {

        this.size = size;
        this.reachability_criteria = reachability_criteria;
    }

    public static Selector generate(Random random) {

        final int selector_type_index = random.nextInt(SELECTORS_SIZE);
        final Selector generated_selector = SELECTORS.get(selector_type_index).copy();
        if (!generated_selector.isSingleton()) {

            generated_selector.size = getRandomSelectionSize(random);
            generated_selector.reachability_criteria = getRandomReachabilityCriteria(random);
        }

        return generated_selector;
    }

    @Override
    public abstract Selector copy();

    public boolean isSingleton() {

        return false;
    }

    private static int getRandomSelectionSize(final Random random) {

        return random.nextInt(MAX_SELECTION_SIZE) + 1;
    }

    private static ReachabilityCriteria getRandomReachabilityCriteria(final Random random) {

        return REACHABILITY_CRITERIA[random.nextInt(REACHABILITY_CRITERIA_LENGTH)];
    }

    public abstract List<PeerReference> select(Peer peer);

    public static Selector mutate(Selector selector, Random random) {

        final Selector mutant;
        final boolean same_type_as_selector = random.nextBoolean();

        if (same_type_as_selector) {
            mutant = selector.copy();
        }
        else {
            mutant = generate(random);
        }

        if (random.nextBoolean()) {
            final boolean same_as_selector = random.nextBoolean();
            mutant.size = same_as_selector ? selector.getSelectionSize() : getRandomSelectionSize(random);
        }

        if (random.nextBoolean()) {
            final boolean same_as_selector = random.nextBoolean();
            mutant.reachability_criteria = same_as_selector ? selector.getReachabilityCriteria() : getRandomReachabilityCriteria(random);
        }

        return mutant;
    }

    public int getSelectionSize() {

        return size;
    }

    public ReachabilityCriteria getReachabilityCriteria() {

        return reachability_criteria;
    }

    public enum ReachabilityCriteria {
        REACHABLE,
        UNREACHABLE,
        REACHABLE_OR_UNREACHABLE
    }

}
