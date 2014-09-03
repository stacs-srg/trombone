package uk.ac.standrews.cs.trombone.evaluation.scenarios;

import uk.ac.standrews.cs.trombone.core.maintenance.DisseminationStrategy;
import uk.ac.standrews.cs.trombone.core.selector.ChordPredecessorSelector;
import uk.ac.standrews.cs.trombone.core.selector.ChordSuccessorSelector;
import uk.ac.standrews.cs.trombone.core.selector.Selector;
import uk.ac.standrews.cs.trombone.core.selector.Self;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class StrongStabilization extends DisseminationStrategy {

    static final boolean PUSH = true;
    static final boolean PULL = !PUSH;
    static final boolean OPPORTUNISTIC = true;
    static final boolean NON_OPPORTUNISTIC = !OPPORTUNISTIC;
    static final Selector SUCCESSOR = ChordSuccessorSelector.getInstance();
    static final Selector PREDECESSOR = ChordPredecessorSelector.getInstance();
    static final Self SELF = Self.getInstance();
    private static final long serialVersionUID = 3153617698283830110L;

    public StrongStabilization() {

        addAction(new DisseminationStrategy.Action(NON_OPPORTUNISTIC, PULL, PREDECESSOR, SUCCESSOR));
        addAction(new DisseminationStrategy.Action(NON_OPPORTUNISTIC, PULL, SUCCESSOR, PREDECESSOR));
        addAction(new DisseminationStrategy.Action(NON_OPPORTUNISTIC, PUSH, SELF, SUCCESSOR));
        addAction(new DisseminationStrategy.Action(NON_OPPORTUNISTIC, PUSH, SELF, PREDECESSOR));
    }
}
