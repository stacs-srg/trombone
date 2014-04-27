package uk.ac.standrews.cs.trombone.evaluation.scenarios;

import uk.ac.standrews.cs.trombone.core.DisseminationStrategy;
import uk.ac.standrews.cs.trombone.core.selector.MostRecentlySeen;
import uk.ac.standrews.cs.trombone.core.selector.Selector;
import uk.ac.standrews.cs.trombone.core.util.Named;
import uk.ac.standrews.cs.trombone.core.util.NamingUtils;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class MostRecentlySeenMaintenance extends DisseminationStrategy implements Named {

    private static final long serialVersionUID = -8028117026365216530L;

    public MostRecentlySeenMaintenance(int recipient_count, int data_count) {

        final MostRecentlySeen data_selector = new MostRecentlySeen(data_count, Selector.ReachabilityCriteria.REACHABLE_OR_UNREACHABLE);
        final MostRecentlySeen recipient_selector = new MostRecentlySeen(recipient_count, Selector.ReachabilityCriteria.REACHABLE_OR_UNREACHABLE);
        addAction(new Action(SuccessorMaintenance.NON_OPPORTUNISTIC, SuccessorMaintenance.PULL, data_selector, recipient_selector));
    }

    @Override
    public String getName() {

        return NamingUtils.name(this);
    }
}
                       