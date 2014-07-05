package uk.ac.standrews.cs.trombone.core.adaptation;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.Clusterer;
import uk.ac.standrews.cs.trombone.core.util.Probability;
import uk.ac.standrews.cs.trombone.core.DisseminationStrategy;
import uk.ac.standrews.cs.trombone.core.Peer;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class RandomMaintenance extends EvolutionaryMaintenance {

    RandomMaintenance(final Peer peer, ScheduledExecutorService scheduler, final int population_size, final long evolution_cycle_length, final TimeUnit evolution_cycle_unit, final Clusterer<EvaluatedDisseminationStrategy> clusterer, final DisseminationStrategyGenerator strategy_generator) {

        super(peer, scheduler, population_size, 0, Probability.ZERO, evolution_cycle_length, evolution_cycle_unit, clusterer, strategy_generator);
    }

    @Override
    protected DisseminationStrategy generateNextStrategy(final Cluster<EvaluatedDisseminationStrategy> current_cluster) {

        return strategy_generator.generate(random);
    }
}
