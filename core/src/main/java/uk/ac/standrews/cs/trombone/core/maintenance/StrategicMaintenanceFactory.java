package uk.ac.standrews.cs.trombone.core.maintenance;

import java.util.concurrent.TimeUnit;
import uk.ac.standrews.cs.trombone.core.Peer;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class StrategicMaintenanceFactory implements MaintenanceFactory {

    private final DisseminationStrategy strategy;
    private final long interval;
    private final TimeUnit interval_unit;

    public StrategicMaintenanceFactory(DisseminationStrategy strategy, long interval, TimeUnit interval_unit) {

        this.strategy = strategy;
        this.interval = interval;
        this.interval_unit = interval_unit;
    }

    @Override
    public Maintenance apply(final Peer peer) {

        return new StrategicMaintenance(peer, strategy, interval, interval_unit);
    }
}
