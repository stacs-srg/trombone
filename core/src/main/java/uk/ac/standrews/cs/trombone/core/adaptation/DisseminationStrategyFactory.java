package uk.ac.standrews.cs.trombone.core.adaptation;

import java.util.Random;
import org.uncommons.watchmaker.framework.factories.AbstractCandidateFactory;
import uk.ac.standrews.cs.trombone.core.DisseminationStrategy;
import uk.ac.standrews.cs.trombone.core.selector.Selector;
import uk.ac.standrews.cs.trombone.core.selector.SelectorRegistry;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class DisseminationStrategyFactory extends AbstractCandidateFactory<DisseminationStrategy> {


    @Override
    public DisseminationStrategy generateRandomCandidate(final Random random) {

        final boolean opportunistic = random.nextBoolean();
        final boolean push = random.nextBoolean();
        final Selector data_selector = SelectorRegistry.pickRandomly(random);
        final Selector recipient_selector = SelectorRegistry.pickRandomly(random);

        return new DisseminationStrategy(opportunistic, push, data_selector, recipient_selector);
    }
}
