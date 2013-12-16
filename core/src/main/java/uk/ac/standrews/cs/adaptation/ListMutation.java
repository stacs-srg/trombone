package uk.ac.standrews.cs.adaptation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.uncommons.maths.number.ConstantGenerator;
import org.uncommons.maths.number.NumberGenerator;
import org.uncommons.maths.random.Probability;
import org.uncommons.watchmaker.framework.CandidateFactory;
import org.uncommons.watchmaker.framework.EvolutionaryOperator;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class ListMutation<T> implements EvolutionaryOperator<List<T>> {

    private final CandidateFactory<T> candidate_factory;
    private final NumberGenerator<Probability> mutationProbability;

    /**
     * Creates a mutation operator that is applied with the given
     * probability and draws its characters from the specified candidate factory.
     *
     * @param candidate_factory The factory to generate mutated candidates from
     * @param mutationProbability The probability that a given character
     * is changed.
     */
    public ListMutation(CandidateFactory<T> candidate_factory, Probability mutationProbability) {

        this(candidate_factory, new ConstantGenerator<Probability>(mutationProbability));
    }

    /**
     * Creates a mutation operator that is applied with the given
     * probability and draws its characters from the specified candidate factory.
     *
     * @param candidate_factory The factory to generate mutated candidates from
     * @param mutationProbability The (possibly variable) probability that a
     * given character is changed.
     */
    public ListMutation(CandidateFactory<T> candidate_factory, NumberGenerator<Probability> mutationProbability) {

        this.candidate_factory = candidate_factory;
        this.mutationProbability = mutationProbability;
    }

    public List<List<T>> apply(List<List<T>> selectedCandidates, Random rng) {

        List<List<T>> mutatedPopulation = new ArrayList<List<T>>(selectedCandidates.size());
        for (List<T> selected_candidate : selectedCandidates) {
            mutatedPopulation.add(mutateList(selected_candidate, rng));
        }
        return mutatedPopulation;
    }

    /**
     * Mutate a single list.  Zero or more elements in the list may be modified.  The
     * probability of any given character being modified is governed by the
     * probability generator configured for this mutation operator.
     *
     * @param selected_candidate The candidate list to mutate.
     * @param random A source of randomness.
     * @return The mutated string.
     */
    private List<T> mutateList(List<T> selected_candidate, Random random) {

        for (int i = 0; i < selected_candidate.size(); i++) {
            if (mutationProbability.nextValue().nextEvent(random)) {
                selected_candidate.set(i, candidate_factory.generateRandomCandidate(random));
            }
        }
        return selected_candidate;
    }
}
