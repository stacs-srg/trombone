package uk.ac.standrews.cs.trombone.evaluation.scenarios;

import java.util.ArrayList;
import java.util.List;
import uk.ac.standrews.cs.trombone.core.PeerConfiguration;
import uk.ac.standrews.cs.trombone.event.Scenario;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class Batch6EffectOfFeedbackWithTraining implements ScenarioBatch {

    private static final Batch6EffectOfFeedbackWithTraining BATCH_6_EFFECT_OF_FEEDBACK_WITH_TRAINING = new Batch6EffectOfFeedbackWithTraining();

    public static final PeerConfiguration TROMBONE_ADAPTIVE_GA_FEEDBACK_ENABLED_2 = PeerConfiguration.builder(Constants.TROMBONE_ADAPTIVE_GA_BUILDER)
            .enableApplicationFeedback(true)
            .maintenance(Constants.EVOLUTIONARY_MAINTENANCE_STOP_AFTER_2)
            .build();
    public static final PeerConfiguration TROMBONE_ADAPTIVE_GA_FEEDBACK_ENABLED_4 = PeerConfiguration.builder(Constants.TROMBONE_ADAPTIVE_GA_BUILDER)
            .enableApplicationFeedback(true)
            .maintenance(Constants.EVOLUTIONARY_MAINTENANCE_STOP_AFTER_4)
            .build();
    public static final PeerConfiguration TROMBONE_ADAPTIVE_GA_FEEDBACK_ENABLED_8 = PeerConfiguration.builder(Constants.TROMBONE_ADAPTIVE_GA_BUILDER)
            .enableApplicationFeedback(true)
            .maintenance(Constants.EVOLUTIONARY_MAINTENANCE_STOP_AFTER_8)
            .build();
    public static final PeerConfiguration TROMBONE_ADAPTIVE_GA_FEEDBACK_ENABLED_16 = PeerConfiguration.builder(Constants.TROMBONE_ADAPTIVE_GA_BUILDER)
            .enableApplicationFeedback(true)
            .maintenance(Constants.EVOLUTIONARY_MAINTENANCE_STOP_AFTER_16)
            .build();

    public static final PeerConfiguration TROMBONE_RANDOM_GA_FEEDBACK_ENABLED_2 = PeerConfiguration.builder(Constants.TROMBONE_ADAPTIVE_RANDOM_BUILDER)
            .enableApplicationFeedback(true)
            .maintenance(Constants.RANDOM_MAINTENANCE_STOP_AFTER_2)
            .build();
    public static final PeerConfiguration TROMBONE_RANDOM_GA_FEEDBACK_ENABLED_4 = PeerConfiguration.builder(Constants.TROMBONE_ADAPTIVE_RANDOM_BUILDER)
            .enableApplicationFeedback(true)
            .maintenance(Constants.RANDOM_MAINTENANCE_STOP_AFTER_4)
            .build();
    public static final PeerConfiguration TROMBONE_RANDOM_GA_FEEDBACK_ENABLED_8 = PeerConfiguration.builder(Constants.TROMBONE_ADAPTIVE_RANDOM_BUILDER)
            .enableApplicationFeedback(true)
            .maintenance(Constants.RANDOM_MAINTENANCE_STOP_AFTER_8)
            .build();
    public static final PeerConfiguration TROMBONE_RANDOM_GA_FEEDBACK_ENABLED_16 = PeerConfiguration.builder(Constants.TROMBONE_ADAPTIVE_RANDOM_BUILDER)
            .enableApplicationFeedback(true)
            .maintenance(Constants.RANDOM_MAINTENANCE_STOP_AFTER_16)
            .build();

    public static Batch6EffectOfFeedbackWithTraining getInstance() {

        return BATCH_6_EFFECT_OF_FEEDBACK_WITH_TRAINING;
    }

    private Batch6EffectOfFeedbackWithTraining() {

    }

    @Override
    public List<Scenario> get() {

        final List<Scenario> scenarios = new ArrayList<>();

        int i = 0;
        scenarios.add(new BaseScenario(getName() + ++i, Constants.CHURN_OSCILLATING, Constants.WORKLOAD_10_SEC, TROMBONE_ADAPTIVE_GA_FEEDBACK_ENABLED_2, Constants.EXPERIMENT_DURATION_6));
        scenarios.add(new BaseScenario(getName() + ++i, Constants.CHURN_OSCILLATING, Constants.WORKLOAD_10_SEC, TROMBONE_ADAPTIVE_GA_FEEDBACK_ENABLED_4, Constants.EXPERIMENT_DURATION_8));
        scenarios.add(new BaseScenario(getName() + ++i, Constants.CHURN_OSCILLATING, Constants.WORKLOAD_10_SEC, TROMBONE_ADAPTIVE_GA_FEEDBACK_ENABLED_8, Constants.EXPERIMENT_DURATION_12));
        scenarios.add(new BaseScenario(getName() + ++i, Constants.CHURN_OSCILLATING, Constants.WORKLOAD_10_SEC, TROMBONE_ADAPTIVE_GA_FEEDBACK_ENABLED_16, Constants.EXPERIMENT_DURATION_20));

        scenarios.add(new BaseScenario(getName() + ++i, Constants.CHURN_OSCILLATING, Constants.WORKLOAD_10_SEC, TROMBONE_RANDOM_GA_FEEDBACK_ENABLED_2, Constants.EXPERIMENT_DURATION_6));
        scenarios.add(new BaseScenario(getName() + ++i, Constants.CHURN_OSCILLATING, Constants.WORKLOAD_10_SEC, TROMBONE_RANDOM_GA_FEEDBACK_ENABLED_4, Constants.EXPERIMENT_DURATION_8));
        scenarios.add(new BaseScenario(getName() + ++i, Constants.CHURN_OSCILLATING, Constants.WORKLOAD_10_SEC, TROMBONE_RANDOM_GA_FEEDBACK_ENABLED_8, Constants.EXPERIMENT_DURATION_12));
        scenarios.add(new BaseScenario(getName() + ++i, Constants.CHURN_OSCILLATING, Constants.WORKLOAD_10_SEC, TROMBONE_RANDOM_GA_FEEDBACK_ENABLED_16, Constants.EXPERIMENT_DURATION_20));

        return scenarios;
    }

    @Override
    public String getName() {

        return "feedbacktrainingoscillating";
    }
}
