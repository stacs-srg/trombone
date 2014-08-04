package uk.ac.standrews.cs.trombone.evaluation.scenarios;

import java.util.ArrayList;
import java.util.List;
import uk.ac.standrews.cs.trombone.event.Scenario;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class Batch3EffectOfTrainingDuration implements ScenarioBatch {

    private static final Batch3EffectOfTrainingDuration BATCH_3_EFFECT_OF_TRAINING_DURATION = new Batch3EffectOfTrainingDuration();

    public static Batch3EffectOfTrainingDuration getInstance() {

        return BATCH_3_EFFECT_OF_TRAINING_DURATION;
    }

    private Batch3EffectOfTrainingDuration() {

    }

    @Override
    public List<Scenario> get() {

        final List<Scenario> scenarios = new ArrayList<>();

        int i = 0;
        scenarios.add(new BaseScenario(getName() + ++i, Constants.CHURN_30_MIN, Constants.WORKLOAD_10_SEC, Constants.TROMBONE_ADAPTIVE_GA_2, Constants.EXPERIMENT_DURATION_6));
        scenarios.add(new BaseScenario(getName() + ++i, Constants.CHURN_30_MIN, Constants.WORKLOAD_10_SEC, Constants.TROMBONE_ADAPTIVE_GA_4, Constants.EXPERIMENT_DURATION_8));
        scenarios.add(new BaseScenario(getName() + ++i, Constants.CHURN_30_MIN, Constants.WORKLOAD_10_SEC, Constants.TROMBONE_ADAPTIVE_GA_8, Constants.EXPERIMENT_DURATION_12));
        scenarios.add(new BaseScenario(getName() + ++i, Constants.CHURN_30_MIN, Constants.WORKLOAD_10_SEC, Constants.TROMBONE_ADAPTIVE_GA_16, Constants.EXPERIMENT_DURATION_20));

        scenarios.add(new BaseScenario(getName() + ++i, Constants.CHURN_30_MIN, Constants.WORKLOAD_10_SEC, Constants.TROMBONE_ADAPTIVE_RANDOM_2, Constants.EXPERIMENT_DURATION_6));
        scenarios.add(new BaseScenario(getName() + ++i, Constants.CHURN_30_MIN, Constants.WORKLOAD_10_SEC, Constants.TROMBONE_ADAPTIVE_RANDOM_4, Constants.EXPERIMENT_DURATION_8));
        scenarios.add(new BaseScenario(getName() + ++i, Constants.CHURN_30_MIN, Constants.WORKLOAD_10_SEC, Constants.TROMBONE_ADAPTIVE_RANDOM_8, Constants.EXPERIMENT_DURATION_12));
        scenarios.add(new BaseScenario(getName() + ++i, Constants.CHURN_30_MIN, Constants.WORKLOAD_10_SEC, Constants.TROMBONE_ADAPTIVE_RANDOM_16, Constants.EXPERIMENT_DURATION_20));

        return scenarios;
    }

    @Override
    public String getName() {

        return "training";
    }
}
