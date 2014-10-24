package uk.ac.standrews.cs.trombone.evaluation.scenarios;

import java.util.List;
import uk.ac.standrews.cs.shabdiz.util.Duration;
import uk.ac.standrews.cs.trombone.core.PeerConfiguration;
import uk.ac.standrews.cs.trombone.event.Scenario;
import uk.ac.standrews.cs.trombone.event.environment.Workload;

import static uk.ac.standrews.cs.trombone.evaluation.scenarios.Constants.ALL_CHURN_MODELS;
import static uk.ac.standrews.cs.trombone.evaluation.scenarios.Constants.EXPERIMENT_DURATION_4;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class Batch5EffectOfFeedback implements ScenarioBatch {

    private static final Batch5EffectOfFeedback BATCH_1_EFFECT_OF_CHURN = new Batch5EffectOfFeedback();
    public static final PeerConfiguration TROMBONE_ADAPTIVE_GA_FEEDBACK_ENABLED = PeerConfiguration.builder(Constants.TROMBONE_ADAPTIVE_GA_BUILDER)
            .enableApplicationFeedback(true)
            .build();
    public static final PeerConfiguration TROMBONE_RANDOM_GA_FEEDBACK_ENABLED = PeerConfiguration.builder(Constants.TROMBONE_ADAPTIVE_RANDOM_BUILDER)
            .enableApplicationFeedback(true)
            .build();

    public static Batch5EffectOfFeedback getInstance() {

        return BATCH_1_EFFECT_OF_CHURN;
    }

    private Batch5EffectOfFeedback() {

    }

    @Override
    public List<Scenario> get() {

        return BaseScenario.generateAll(getName(), ALL_CHURN_MODELS, new Workload[] {
                Constants.WORKLOAD_10_SEC
        }, new PeerConfiguration[] {
                TROMBONE_ADAPTIVE_GA_FEEDBACK_ENABLED,
                TROMBONE_RANDOM_GA_FEEDBACK_ENABLED

        }, new Duration[] {
                EXPERIMENT_DURATION_4
        });
    }

    @Override
    public String getName() {

        return "feedback";
    }
}
