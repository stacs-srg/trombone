package uk.ac.standrews.cs.adaptation;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.uncommons.maths.binary.BitString;
import org.uncommons.maths.random.MersenneTwisterRNG;
import org.uncommons.maths.random.Probability;
import org.uncommons.watchmaker.framework.EvolutionEngine;
import org.uncommons.watchmaker.framework.EvolutionaryOperator;
import org.uncommons.watchmaker.framework.FitnessEvaluator;
import org.uncommons.watchmaker.framework.GenerationalEvolutionEngine;
import org.uncommons.watchmaker.framework.SelectionStrategy;
import org.uncommons.watchmaker.framework.factories.AbstractCandidateFactory;
import org.uncommons.watchmaker.framework.operators.BitStringMutation;
import org.uncommons.watchmaker.framework.selection.RouletteWheelSelection;
import uk.ac.standrews.cs.trombone.core.DisseminationStrategy;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerMetric;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class Engine {

    EvolutionEngine<List<DisseminationStrategy>> evolutionEngine;

    public Engine(final Peer peer) {

        final AbstractCandidateFactory<List<DisseminationStrategy>> candidate_factory = new AbstractCandidateFactory<List<DisseminationStrategy>>() {

            @Override
            public List<DisseminationStrategy> generateRandomCandidate(final Random random) {

                // EASY
                // generate a random bitstring of length 9;

                return null;
            }
        };

        EvolutionaryOperator<List<DisseminationStrategy>> operator = new EvolutionaryOperator<List<DisseminationStrategy>>() {

            final BitStringMutation bitstring_mutation = new BitStringMutation(Probability.EVENS);

            @Override
            public List<List<DisseminationStrategy>> apply(final List<List<DisseminationStrategy>> selectedCandidates, final Random random) {

                final List<BitString> aa = null;// maintenance to bitstring
                final List<BitString> maintenance_as_bitstring = bitstring_mutation.apply(aa, random);
                // bitstring to maintenance 

                return null;
            }
        };

        FitnessEvaluator<List<DisseminationStrategy>> fitness_evaluator = new FitnessEvaluator<List<DisseminationStrategy>>() {

            @Override
            public double getFitness(final List<DisseminationStrategy> candidate, final List<? extends List<DisseminationStrategy>> population) {

                final PeerMetric metric = peer.getPeerMetric();
                final long mean_lookup_success_delay_millis = metric.getMeanLookupSuccessDelay(TimeUnit.MILLISECONDS);
                final long sent_bytes = metric.getSentBytes();
                final double lookup_failure_rate = metric.getLookupFailureRate();

                return 0;
            }

            @Override
            public boolean isNatural() {

                return false;
            }
        };

        SelectionStrategy<Object> selection_strategy = new RouletteWheelSelection();

        final MersenneTwisterRNG random = new MersenneTwisterRNG();
        evolutionEngine = new GenerationalEvolutionEngine<List<DisseminationStrategy>>(candidate_factory, operator, fitness_evaluator, selection_strategy, random);
    }
}
