package uk.ac.standrews.cs.trombone.evaluation.util;

import org.junit.Test;
import uk.ac.standrews.cs.trombone.evaluation.scenarios.Batch1EffectOfChurn;

public class ScenarioUtilsTest {

    @Test
    public void testSaveScenarioAsJson() throws Exception {

        System.out.println(ScenarioUtils.OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(Batch1EffectOfChurn.getInstance().get().get(3)));
    }
}