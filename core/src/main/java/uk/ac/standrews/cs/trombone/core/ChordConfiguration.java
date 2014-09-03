package uk.ac.standrews.cs.trombone.core;

import java.math.BigInteger;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import uk.ac.standrews.cs.trombone.core.maintenance.ChordMaintenanceFactory;
import uk.ac.standrews.cs.trombone.core.maintenance.MaintenanceFactory;
import uk.ac.standrews.cs.trombone.core.state.ChordPeerStateFactory;
import uk.ac.standrews.cs.trombone.core.state.PeerStateFactory;
import uk.ac.standrews.cs.trombone.core.strategy.ChordJoinStrategy;
import uk.ac.standrews.cs.trombone.core.strategy.ChordLookupStrategy;
import uk.ac.standrews.cs.trombone.core.strategy.ChordNextHopStrategy;
import uk.ac.standrews.cs.trombone.core.strategy.JoinStrategy;
import uk.ac.standrews.cs.trombone.core.strategy.LookupStrategy;
import uk.ac.standrews.cs.trombone.core.strategy.NextHopStrategy;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class ChordConfiguration implements PeerConfiguration {

    private final ChordMaintenanceFactory maintenance_factory;
    private final ChordPeerStateFactory peer_state_factory;
    private final ScheduledExecutorService scheduled_executor_service;

    public ChordConfiguration(int finger_table_size, BigInteger finger_base, int successor_list_size, long maintenance_interval, TimeUnit maintenance_interval_unit, int executor_pool_size) {

        maintenance_factory = new ChordMaintenanceFactory(maintenance_interval, maintenance_interval_unit);
        peer_state_factory = new ChordPeerStateFactory(finger_table_size, finger_base, successor_list_size);
        scheduled_executor_service = Executors.newScheduledThreadPool(executor_pool_size);
    }

    @Override
    public MaintenanceFactory getMaintenance() {

        return maintenance_factory;
    }

    @Override
    public PeerStateFactory getPeerState() {

        return peer_state_factory;
    }

    @Override
    public JoinStrategy getJoinStrategy() {

        return new ChordJoinStrategy();
    }

    @Override
    public LookupStrategy getLookupStrategy() {

        return new ChordLookupStrategy();
    }

    @Override
    public NextHopStrategy getNextHopStrategy() {

        return new ChordNextHopStrategy();
    }

    @Override
    public ScheduledExecutorService getExecutor() {

        return scheduled_executor_service;
    }
}
