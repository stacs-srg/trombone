package uk.ac.standrews.cs.trombone.evaluation.scenarios;

import java.util.List;
import uk.ac.standrews.cs.shabdiz.util.Duration;
import uk.ac.standrews.cs.trombone.event.Scenario;
import uk.ac.standrews.cs.trombone.event.environment.Workload;

import static uk.ac.standrews.cs.trombone.evaluation.scenarios.Constants.ALL_CHURN_MODELS;
import static uk.ac.standrews.cs.trombone.evaluation.scenarios.Constants.EXPERIMENT_DURATION_4;
import static uk.ac.standrews.cs.trombone.evaluation.scenarios.Constants.PEER_CONFIGURATIONS;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class Batch1EffectOfChurn implements ScenarioBatch {

    private static final Batch1EffectOfChurn BATCH_1_EFFECT_OF_CHURN = new Batch1EffectOfChurn();


    public static Batch1EffectOfChurn getInstance() {

        return BATCH_1_EFFECT_OF_CHURN;
    }

    private Batch1EffectOfChurn() {

    }

    @Override
    public List<Scenario> get() {

        return BaseScenario.generateAll(getName(), ALL_CHURN_MODELS, new Workload[] {
                Constants.WORKLOAD_10_SEC
        }, PEER_CONFIGURATIONS, new Duration[] {
                EXPERIMENT_DURATION_4
        });
    }

    @Override
    public String getName() {

        return "churn";
    }
}
