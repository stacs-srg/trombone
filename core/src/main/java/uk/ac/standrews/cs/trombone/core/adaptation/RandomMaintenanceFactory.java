package uk.ac.standrews.cs.trombone.core.adaptation;

import java.util.concurrent.TimeUnit;
import org.apache.commons.math3.ml.clustering.Clusterer;
import uk.ac.standrews.cs.trombone.core.Maintenance;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.util.Probability;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class RandomMaintenanceFactory extends EvolutionaryMaintenanceFactory {

    private static final long serialVersionUID = -5580895854216773029L;

    public RandomMaintenanceFactory(int population_size, long cycle_length, TimeUnit cycle_unit, Clusterer<EvaluatedDisseminationStrategy> clusterer, int max_action_size, int max_selection_size) {

        super(population_size, 0, Probability.ZERO, cycle_length, cycle_unit, clusterer, max_action_size, max_selection_size);
    }

    @Override
    protected Maintenance maintain(Peer peer) {

        return new RandomMaintenance(peer, SCHEDULER, population_size, evolution_cycle_length, evolution_cycle_unit, clusterer, strategy_generator);
    }
}
