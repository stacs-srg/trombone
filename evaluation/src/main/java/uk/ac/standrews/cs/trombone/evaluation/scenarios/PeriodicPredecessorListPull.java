package uk.ac.standrews.cs.trombone.evaluation.scenarios;

import uk.ac.standrews.cs.trombone.core.maintenance.DisseminationStrategy;
import uk.ac.standrews.cs.trombone.core.selector.Last;

import static uk.ac.standrews.cs.trombone.evaluation.scenarios.StrongStabilization.NON_OPPORTUNISTIC;
import static uk.ac.standrews.cs.trombone.evaluation.scenarios.StrongStabilization.PREDECESSOR;
import static uk.ac.standrews.cs.trombone.evaluation.scenarios.StrongStabilization.PULL;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class PeriodicPredecessorListPull extends DisseminationStrategy {

    private static final long serialVersionUID = -281897946144196590L;

    public PeriodicPredecessorListPull(int size) {

        addAction(new Action(NON_OPPORTUNISTIC, PULL, new Last(size), PREDECESSOR));
    }
}
