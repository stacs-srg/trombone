package uk.ac.standrews.cs.trombone.evaluation.scenarios;

import uk.ac.standrews.cs.trombone.event.provider.SequentialPortNumberProvider;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class LocalTestScenario extends PlatformJustification {

    public LocalTestScenario(int network_size) {

        super(LocalTestScenario.class.getSimpleName() + network_size);
        addHost("localhost", network_size, new SequentialPortNumberProvider(45000));
    }
}
