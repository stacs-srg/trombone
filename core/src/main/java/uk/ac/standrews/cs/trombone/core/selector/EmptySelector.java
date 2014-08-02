package uk.ac.standrews.cs.trombone.core.selector;

import java.util.Collections;
import java.util.List;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerReference;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public final class EmptySelector extends Selector {

    private static final long serialVersionUID = -7162399826632352226L;
    public static final EmptySelector INSTANCE = new EmptySelector();

    private EmptySelector() {

        super(0, ReachabilityCriteria.ANY);
    }

    @Override
    public List<PeerReference> select(final Peer peer) {

        return Collections.emptyList();
    }

    @Override
    public Selector copy() {

        return this;
    }

    @Override
    public boolean isSingleton() {

        return true;
    }
}
