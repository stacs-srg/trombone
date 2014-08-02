package uk.ac.standrews.cs.trombone.evaluation.scenarios;

import java.util.List;
import uk.ac.standrews.cs.shabdiz.util.Duration;
import uk.ac.standrews.cs.trombone.event.Scenario;
import uk.ac.standrews.cs.trombone.event.environment.Churn;

import static uk.ac.standrews.cs.trombone.evaluation.scenarios.Constants.EXPERIMENT_DURATION_4;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class Batch2EffectOfWorkload implements ScenarioBatch {

    private static final Batch2EffectOfWorkload BATCH_2_EFFECT_OF_WORKLOAD = new Batch2EffectOfWorkload();

    public static Batch2EffectOfWorkload getInstance() {

        return BATCH_2_EFFECT_OF_WORKLOAD;
    }

    private Batch2EffectOfWorkload() {

    }

    @Override
    public List<Scenario> get() {

        return BaseScenario.generateAll(getName(), new Churn[] {Constants.CHURN_30_MIN}, Constants.WORKLOADS, null, new Duration[] {EXPERIMENT_DURATION_4});
    }

    @Override
    public String getName() {

        return "workload";
    }
}
