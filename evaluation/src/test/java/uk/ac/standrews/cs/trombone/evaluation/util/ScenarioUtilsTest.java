package uk.ac.standrews.cs.trombone.evaluation.util;

import java.util.List;
import org.junit.Test;
import uk.ac.standrews.cs.trombone.evaluation.scenarios.Batch1EffectOfChurn;
import uk.ac.standrews.cs.trombone.event.Scenario;

public class ScenarioUtilsTest {

    @Test
    public void testSaveScenarioAsJson() throws Exception {

        final List<Scenario> scenarios = Batch1EffectOfChurn.getInstance()
                .get();
        System.out.println(ScenarioUtils.OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(scenarios.get(1)));
    }
}