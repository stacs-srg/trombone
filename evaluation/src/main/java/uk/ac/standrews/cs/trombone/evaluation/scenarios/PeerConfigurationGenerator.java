package uk.ac.standrews.cs.trombone.evaluation.scenarios;

import java.util.ArrayList;
import java.util.List;
import uk.ac.standrews.cs.shabdiz.util.Combinations;
import uk.ac.standrews.cs.trombone.core.MaintenanceFactory;
import uk.ac.standrews.cs.trombone.core.PeerConfiguration;
import uk.ac.standrews.cs.trombone.core.SyntheticDelay;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public final class PeerConfigurationGenerator {

    private static final Boolean[] DEFAULT_APPLICATION_FEEDBACK = {Boolean.FALSE};

    private PeerConfigurationGenerator() {

    }

    public static List<PeerConfiguration> generate(MaintenanceFactory[] maintenance_factories, SyntheticDelay[] delays) {

        return generate(maintenance_factories, delays, DEFAULT_APPLICATION_FEEDBACK);
    }

    public static List<PeerConfiguration> generate(MaintenanceFactory[] maintenance_factories, SyntheticDelay[] delays, Boolean[] application_feedbacks) {

        List<PeerConfiguration> configurations = new ArrayList<>();
        final List<Object[]> args = Combinations.generateArgumentCombinations(new Object[][] {
                maintenance_factories, delays, application_feedbacks
        });
        for (Object[] arg : args) {
            final MaintenanceFactory maintenance_factory = (MaintenanceFactory) arg[0];
            final SyntheticDelay delay = (SyntheticDelay) arg[1];
            final Boolean application_feedback = (Boolean) arg[2];
            final PeerConfiguration configuration = new PeerConfiguration(maintenance_factory, delay);
            configuration.setApplicationFeedbackEnabled(application_feedback);
            configurations.add(configuration);
        }
        return configurations;
    }
}
