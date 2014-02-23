package uk.ac.standrews.cs.trombone.evaluation.scenarios;

import uk.ac.standrews.cs.trombone.core.DisseminationStrategy;
import uk.ac.standrews.cs.trombone.core.selector.RandomSelector;
import uk.ac.standrews.cs.trombone.core.util.Named;
import uk.ac.standrews.cs.trombone.core.util.NamingUtils;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class RandomMaintenance extends DisseminationStrategy implements Named {

    private static final long serialVersionUID = -5879079165256164612L;

    public RandomMaintenance(int recipient_count, int data_count) {

        addAction(new DisseminationStrategy.Action(SuccessorMaintenance.NON_OPPORTUNISTIC, SuccessorMaintenance.PULL, new RandomSelector(data_count), new RandomSelector(recipient_count)));
    }

    @Override
    public String getName() {

        return NamingUtils.name(this);
    }
}
                       