package uk.ac.standrews.cs.trombone.core.maintenance;

import uk.ac.standrews.cs.trombone.core.Peer;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class NoMaintenanceFactory implements MaintenanceFactory {

    private static final NoMaintenanceFactory NO_MAINTENANCE_FACTORY = new NoMaintenanceFactory();

    private NoMaintenanceFactory() {

    }

    public static NoMaintenanceFactory getInstance() {

        return NO_MAINTENANCE_FACTORY;
    }

    @Override
    public Maintenance apply(final Peer peer) {

        return null;
    }
}
