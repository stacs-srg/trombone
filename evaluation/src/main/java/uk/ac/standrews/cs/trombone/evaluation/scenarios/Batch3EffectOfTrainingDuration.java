package uk.ac.standrews.cs.trombone.evaluation.scenarios;

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

        return null;
    }

    @Override
    public String getName() {

        return "training";
    }
}
