package uk.ac.standrews.cs.trombone.evaluation.scenarios;

import uk.ac.standrews.cs.trombone.core.DisseminationStrategy;
import uk.ac.standrews.cs.trombone.core.selector.First;
import uk.ac.standrews.cs.trombone.core.selector.Selector;
import uk.ac.standrews.cs.trombone.core.util.Named;
import uk.ac.standrews.cs.trombone.core.util.NamingUtils;

import static uk.ac.standrews.cs.trombone.evaluation.scenarios.SuccessorMaintenance.NON_OPPORTUNISTIC;
import static uk.ac.standrews.cs.trombone.evaluation.scenarios.SuccessorMaintenance.PULL;
import static uk.ac.standrews.cs.trombone.evaluation.scenarios.SuccessorMaintenance.SUCCESSOR;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class SuccessorListMaintenance extends DisseminationStrategy implements Named {

    private static final long serialVersionUID = -281897946144196590L;

    public SuccessorListMaintenance(int successor_list_size) {

        addAction(new DisseminationStrategy.Action(NON_OPPORTUNISTIC, PULL, new First(successor_list_size, Selector.ReachabilityCriteria.REACHABLE_OR_UNREACHABLE), SUCCESSOR));
    }

    @Override
    public String getName() {

        return NamingUtils.name(this);
    }
}
