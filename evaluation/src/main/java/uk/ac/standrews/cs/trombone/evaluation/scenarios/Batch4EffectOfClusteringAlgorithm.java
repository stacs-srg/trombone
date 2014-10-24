package uk.ac.standrews.cs.trombone.evaluation.scenarios;

import java.util.ArrayList;
import java.util.List;
import uk.ac.standrews.cs.trombone.event.Scenario;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class Batch4EffectOfClusteringAlgorithm implements ScenarioBatch {

    private static final Batch4EffectOfClusteringAlgorithm BATCH_3_EFFECT_OF_TRAINING_DURATION = new Batch4EffectOfClusteringAlgorithm();

    public static Batch4EffectOfClusteringAlgorithm getInstance() {

        return BATCH_3_EFFECT_OF_TRAINING_DURATION;
    }

    private Batch4EffectOfClusteringAlgorithm() {

    }

    @Override
    public List<Scenario> get() {

        final List<Scenario> scenarios = new ArrayList<>();

        int i = 0;
        scenarios.add(new BaseScenario(getName() + ++i, Constants.CHURN_30_MIN, Constants.WORKLOAD_10_SEC, Constants.TROMBONE_ADAPTIVE_GA_KMEAN, Constants.EXPERIMENT_DURATION_4));
        scenarios.add(new BaseScenario(getName() + ++i, Constants.CHURN_30_MIN, Constants.WORKLOAD_10_SEC, Constants.TROMBONE_ADAPTIVE_GA_KMEAN_2, Constants.EXPERIMENT_DURATION_6));
        scenarios.add(new BaseScenario(getName() + ++i, Constants.CHURN_30_MIN, Constants.WORKLOAD_10_SEC, Constants.TROMBONE_ADAPTIVE_GA_KMEAN_4, Constants.EXPERIMENT_DURATION_8));
        scenarios.add(new BaseScenario(getName() + ++i, Constants.CHURN_30_MIN, Constants.WORKLOAD_10_SEC, Constants.TROMBONE_ADAPTIVE_GA_KMEAN_8, Constants.EXPERIMENT_DURATION_12));
        scenarios.add(new BaseScenario(getName() + ++i, Constants.CHURN_30_MIN, Constants.WORKLOAD_10_SEC, Constants.TROMBONE_ADAPTIVE_GA_KMEAN_16, Constants.EXPERIMENT_DURATION_20));
        scenarios.add(new BaseScenario(getName() + ++i, Constants.CHURN_30_MIN, Constants.WORKLOAD_10_SEC, Constants.TROMBONE_ADAPTIVE_RANDOM_KMEAN, Constants.EXPERIMENT_DURATION_4));
        scenarios.add(new BaseScenario(getName() + ++i, Constants.CHURN_30_MIN, Constants.WORKLOAD_10_SEC, Constants.TROMBONE_ADAPTIVE_RANDOM_KMEAN_2, Constants.EXPERIMENT_DURATION_6));
        scenarios.add(new BaseScenario(getName() + ++i, Constants.CHURN_30_MIN, Constants.WORKLOAD_10_SEC, Constants.TROMBONE_ADAPTIVE_RANDOM_KMEAN_4, Constants.EXPERIMENT_DURATION_8));
        scenarios.add(new BaseScenario(getName() + ++i, Constants.CHURN_30_MIN, Constants.WORKLOAD_10_SEC, Constants.TROMBONE_ADAPTIVE_RANDOM_KMEAN_8, Constants.EXPERIMENT_DURATION_12));
        scenarios.add(new BaseScenario(getName() + ++i, Constants.CHURN_30_MIN, Constants.WORKLOAD_10_SEC, Constants.TROMBONE_ADAPTIVE_RANDOM_KMEAN_16, Constants.EXPERIMENT_DURATION_20));
        scenarios.add(new BaseScenario(getName() + ++i, Constants.CHURN_30_MIN, Constants.WORKLOAD_10_SEC, Constants.TROMBONE_ADAPTIVE_GA_PER_POINT, Constants.EXPERIMENT_DURATION_4));
        scenarios.add(new BaseScenario(getName() + ++i, Constants.CHURN_30_MIN, Constants.WORKLOAD_10_SEC, Constants.TROMBONE_ADAPTIVE_GA_PER_POINT_2, Constants.EXPERIMENT_DURATION_6));
        scenarios.add(new BaseScenario(getName() + ++i, Constants.CHURN_30_MIN, Constants.WORKLOAD_10_SEC, Constants.TROMBONE_ADAPTIVE_GA_PER_POINT_4, Constants.EXPERIMENT_DURATION_8));
        scenarios.add(new BaseScenario(getName() + ++i, Constants.CHURN_30_MIN, Constants.WORKLOAD_10_SEC, Constants.TROMBONE_ADAPTIVE_GA_PER_POINT_8, Constants.EXPERIMENT_DURATION_12));
        scenarios.add(new BaseScenario(getName() + ++i, Constants.CHURN_30_MIN, Constants.WORKLOAD_10_SEC, Constants.TROMBONE_ADAPTIVE_GA_PER_POINT_16, Constants.EXPERIMENT_DURATION_20));
        scenarios.add(new BaseScenario(getName() + ++i, Constants.CHURN_30_MIN, Constants.WORKLOAD_10_SEC, Constants.TROMBONE_ADAPTIVE_RANDOM_PER_POINT, Constants.EXPERIMENT_DURATION_4));
        scenarios.add(new BaseScenario(getName() + ++i, Constants.CHURN_30_MIN, Constants.WORKLOAD_10_SEC, Constants.TROMBONE_ADAPTIVE_RANDOM_PER_POINT_2, Constants.EXPERIMENT_DURATION_6));
        scenarios.add(new BaseScenario(getName() + ++i, Constants.CHURN_30_MIN, Constants.WORKLOAD_10_SEC, Constants.TROMBONE_ADAPTIVE_RANDOM_PER_POINT_4, Constants.EXPERIMENT_DURATION_8));
        scenarios.add(new BaseScenario(getName() + ++i, Constants.CHURN_30_MIN, Constants.WORKLOAD_10_SEC, Constants.TROMBONE_ADAPTIVE_RANDOM_PER_POINT_8, Constants.EXPERIMENT_DURATION_12));
        scenarios.add(new BaseScenario(getName() + ++i, Constants.CHURN_30_MIN, Constants.WORKLOAD_10_SEC, Constants.TROMBONE_ADAPTIVE_RANDOM_PER_POINT_16, Constants.EXPERIMENT_DURATION_20));

        return scenarios;
    }

    @Override
    public String getName() {

        return "clustering";
    }
}
