package uk.ac.standrews.cs.trombone.core.adaptation;

import java.util.concurrent.TimeUnit;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.Clusterer;
import org.uncommons.maths.random.Probability;
import uk.ac.standrews.cs.trombone.core.DisseminationStrategy;
import uk.ac.standrews.cs.trombone.core.Peer;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class RandomMaintenance extends EvolutionaryMaintenance {

    private static final long serialVersionUID = -5580895854216773029L;

    @Override
    protected PeerMaintainer maintain(Peer peer) {

        final PeerMaintainer listener = new RandomPeerMaintainer(peer);
        peer.addExposureChangeListener(listener);
        return listener;
    }

    public RandomMaintenance(int population_size, long cycle_length, TimeUnit cycle_unit, Clusterer<EvaluatedDisseminationStrategy> clusterer, int max_action_size, int max_selection_size) {

        super(population_size, 0, Probability.ZERO, cycle_length, cycle_unit, clusterer, max_action_size, max_selection_size);
    }

    class RandomPeerMaintainer extends EvolutionaryPeerMaintainer {

        private RandomPeerMaintainer(final Peer peer) {

            super(peer);
        }

        @Override
        protected DisseminationStrategy generateNextStrategy(final Cluster<EvaluatedDisseminationStrategy> current_cluster) {

            return strategy_generator.generate(random);
        }
    }
}
