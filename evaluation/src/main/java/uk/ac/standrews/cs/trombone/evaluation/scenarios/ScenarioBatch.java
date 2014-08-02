package uk.ac.standrews.cs.trombone.evaluation.scenarios;

import java.util.List;
import java.util.function.Supplier;
import uk.ac.standrews.cs.trombone.event.Scenario;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
interface ScenarioBatch extends Supplier<List<Scenario>> {

    String getName();

}
