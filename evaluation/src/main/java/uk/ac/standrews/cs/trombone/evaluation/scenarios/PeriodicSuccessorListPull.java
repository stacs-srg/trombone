package uk.ac.standrews.cs.trombone.evaluation.scenarios;

import uk.ac.standrews.cs.trombone.core.maintenance.DisseminationStrategy;
import uk.ac.standrews.cs.trombone.core.selector.First;

import static uk.ac.standrews.cs.trombone.evaluation.scenarios.StrongStabilization.NON_OPPORTUNISTIC;
import static uk.ac.standrews.cs.trombone.evaluation.scenarios.StrongStabilization.PULL;
import static uk.ac.standrews.cs.trombone.evaluation.scenarios.StrongStabilization.SUCCESSOR;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class PeriodicSuccessorListPull extends DisseminationStrategy {

    private static final long serialVersionUID = -281897946144196590L;

    public PeriodicSuccessorListPull(int successor_list_size) {

        addAction(new DisseminationStrategy.Action(NON_OPPORTUNISTIC, PULL, new First(successor_list_size), SUCCESSOR));
    }
}
