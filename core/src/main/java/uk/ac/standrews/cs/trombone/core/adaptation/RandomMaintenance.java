package uk.ac.standrews.cs.trombone.core.adaptation;

import java.util.Collections;
import java.util.List;
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

    public RandomMaintenance(int population_size, long cycle_length, TimeUnit cycle_unit, Clusterer<EvaluatedDisseminationStrategy> clusterer) {

        super(population_size, 0, Probability.ZERO, cycle_length, cycle_unit, clusterer);
    }

    class RandomPeerMaintainer extends EvolutionaryPeerMaintainer {

        private RandomPeerMaintainer(final Peer peer) {

            super(peer);
        }

        @Override
        protected synchronized DisseminationStrategy getNextStrategy(final EnvironmentSnapshot environment_snapshot, final DisseminationStrategy previous_strategy) {

            final DisseminationStrategy next_strategy;

            EvaluatedDisseminationStrategy last_evaluated_strategy = null;
            if (previous_strategy != null) {
                last_evaluated_strategy = addToEvaluatedStrategies(environment_snapshot, previous_strategy);
            }

            final int evaluated_strategies_size = evaluated_strategies.size();
            if (evaluated_strategies_size < population_size) {
                next_strategy = STRATEGY_GENERATOR.generate(random);
            }
            else if (last_evaluated_strategy == null) {
                Collections.sort(evaluated_strategies);
                next_strategy = evaluated_strategies.get(0).getStrategy();
            }
            else {
                assert last_evaluated_strategy != null;

                Collections.sort(evaluated_strategies);

                final Cluster<EvaluatedDisseminationStrategy> current_cluster = getCurrentEnvironmentCluster(last_evaluated_strategy);
                final List<EvaluatedDisseminationStrategy> current_cluster_points = current_cluster.getPoints();
                next_strategy = STRATEGY_GENERATOR.generate(random);

                removeLeastFitFromCurrentCluster(current_cluster_points);
            }
            return next_strategy;
        }

    }
}
