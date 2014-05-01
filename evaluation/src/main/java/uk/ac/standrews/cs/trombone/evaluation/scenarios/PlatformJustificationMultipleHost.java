package uk.ac.standrews.cs.trombone.evaluation.scenarios;

import uk.ac.standrews.cs.trombone.event.environment.Churn;
import uk.ac.standrews.cs.trombone.event.environment.Workload;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class PlatformJustificationMultipleHost extends PlatformJustification {

    public PlatformJustificationMultipleHost(int host_count) {

        super(PlatformJustificationMultipleHost.class.getSimpleName() + host_count);
        final Churn churn = Constants.NO_CHURN.copy();
        final Workload workload = Constants.WORKLOAD_1.copy();

        for (int i = 0; i < host_count; i++) {
            addHost("compute-0-" + i + ".local", 1, Constants.PORT_NUMBER_PROVIDER.copy(), churn, workload, Constants.NO_MAINTENANCE_CONFIGURATION);
        }
    }
}
