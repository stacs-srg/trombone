package uk.ac.standrews.cs.trombone.evaluation.scenarios;

import java.util.ArrayList;
import java.util.List;
import uk.ac.standrews.cs.shabdiz.util.Combinations;
import uk.ac.standrews.cs.trombone.core.Maintenance;
import uk.ac.standrews.cs.trombone.core.PeerConfiguration;
import uk.ac.standrews.cs.trombone.core.SyntheticDelay;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class PeerConfigurationGenerator {

    public static List<PeerConfiguration> generate(Maintenance[] maintenance_factories, SyntheticDelay[] delays) {

        List<PeerConfiguration> configurations = new ArrayList<>();
        final List<Object[]> args = Combinations.generateArgumentCombinations(new Object[][] {maintenance_factories, delays});
        for (Object[] arg : args) {
            final Maintenance maintenance_factory = (Maintenance) arg[0];
            final SyntheticDelay delay = (SyntheticDelay) arg[1];
            configurations.add(new PeerConfiguration(maintenance_factory, delay));
        }
        return configurations;
    }
}
