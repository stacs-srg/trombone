package uk.ac.standrews.cs.trombone.evaluation.scenarios;

import uk.ac.standrews.cs.trombone.core.DisseminationStrategy;
import uk.ac.standrews.cs.trombone.core.selector.MostRecentlySeen;
import uk.ac.standrews.cs.trombone.core.util.Named;
import uk.ac.standrews.cs.trombone.core.util.NamingUtils;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class MostRecentlySeenMaintenance extends DisseminationStrategy implements Named {

    private static final long serialVersionUID = -8028117026365216530L;

    public MostRecentlySeenMaintenance(int recipient_count, int data_count) {

        addAction(new Action(SuccessorMaintenance.NON_OPPORTUNISTIC, SuccessorMaintenance.PULL, new MostRecentlySeen(data_count), new MostRecentlySeen(recipient_count)));
    }

    @Override
    public String getName() {

        return NamingUtils.name(this);
    }
}
                       