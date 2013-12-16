package uk.ac.standrews.cs.adaptation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.uncommons.watchmaker.framework.CandidateFactory;
import org.uncommons.watchmaker.framework.factories.AbstractCandidateFactory;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class ListCandidateFactory<T> extends AbstractCandidateFactory<List<T>> {

    private final CandidateFactory<T> candidate_factory;
    private final int list_size;

    public ListCandidateFactory(CandidateFactory<T> candidate_factory, int list_size) {

        this.candidate_factory = candidate_factory;
        this.list_size = list_size;
    }

    @Override
    public List<T> generateRandomCandidate(final Random random) {

        final List<T> strategy_list = new ArrayList<>(list_size);

        for (int i = 0; i < list_size; i++) {
            strategy_list.add(candidate_factory.generateRandomCandidate(random));
        }

        return strategy_list;
    }
}
