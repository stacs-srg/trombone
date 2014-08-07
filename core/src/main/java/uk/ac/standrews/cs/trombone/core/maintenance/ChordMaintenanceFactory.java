package uk.ac.standrews.cs.trombone.core.maintenance;

import java.util.concurrent.TimeUnit;
import uk.ac.standrews.cs.trombone.core.Peer;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class ChordMaintenanceFactory implements MaintenanceFactory {

    private final long interval;
    private final TimeUnit interval_unit;

    public ChordMaintenanceFactory(long interval, TimeUnit interval_unit) {

        this.interval = interval;
        this.interval_unit = interval_unit;
    }

    @Override
    public Maintenance apply(final Peer peer) {

        return new ChordMaintenance(peer, interval, interval_unit);
    }

    public long getInterval() {

        return interval;
    }

    public TimeUnit getIntervalUnit() {

        return interval_unit;
    }
}
