package uk.ac.standrews.cs.trombone.evaluation.scenarios;

import uk.ac.standrews.cs.trombone.core.DisseminationStrategy;
import uk.ac.standrews.cs.trombone.core.selector.First;
import uk.ac.standrews.cs.trombone.core.selector.Last;
import uk.ac.standrews.cs.trombone.core.selector.Self;
import uk.ac.standrews.cs.trombone.core.util.Named;
import uk.ac.standrews.cs.trombone.core.util.NamingUtils;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class SuccessorMaintenance extends DisseminationStrategy implements Named {

    static final boolean PUSH = true;
    static final boolean PULL = !PUSH;
    static final boolean OPPORTUNISTIC = true;
    static final boolean NON_OPPORTUNISTIC = !OPPORTUNISTIC;
    static final First SUCCESSOR = new First(1, true);
    static final Last PREDECESSOR = new Last(1, false);
    static final Last REACHABLE_PREDECESSOR = new Last(1, true);
    static final Self SELF = Self.getInstance();
    private static final long serialVersionUID = 3153617698283830110L;

    public SuccessorMaintenance() {

        addAction(new DisseminationStrategy.Action(NON_OPPORTUNISTIC, PULL, PREDECESSOR, SUCCESSOR));
        addAction(new DisseminationStrategy.Action(NON_OPPORTUNISTIC, PULL, REACHABLE_PREDECESSOR, SUCCESSOR));
        addAction(new DisseminationStrategy.Action(NON_OPPORTUNISTIC, PUSH, SELF, SUCCESSOR));
    }

    @Override
    public String getName() {

        return NamingUtils.name(this);
    }
}
