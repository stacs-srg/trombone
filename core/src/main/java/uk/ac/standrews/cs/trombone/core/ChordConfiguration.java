package uk.ac.standrews.cs.trombone.core;

import java.math.BigInteger;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import uk.ac.standrews.cs.trombone.core.maintenance.ChordMaintenanceFactory;
import uk.ac.standrews.cs.trombone.core.state.ChordPeerStateFactory;
import uk.ac.standrews.cs.trombone.core.strategy.ChordJoinStrategy;
import uk.ac.standrews.cs.trombone.core.strategy.ChordLookupStrategy;
import uk.ac.standrews.cs.trombone.core.strategy.ChordNextHopStrategy;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class ChordConfiguration extends PeerConfiguration {

    public ChordConfiguration(int finger_table_size, BigInteger finger_base, int successor_list_size, long maintenance_interval, TimeUnit maintenance_interval_unit, int executor_pool_size) {

        super(builder().maintenance(new ChordMaintenanceFactory(maintenance_interval, maintenance_interval_unit))
                .peerState(new ChordPeerStateFactory(finger_table_size, finger_base, successor_list_size))
                .joinStrategy(new ChordJoinStrategy())
                .lookupStrategy(new ChordLookupStrategy())
                .nextHopStrategy(new ChordNextHopStrategy())
                .executor(new Supplier<ScheduledExecutorService>() {

                    private final ScheduledExecutorService service = Executors.newScheduledThreadPool(executor_pool_size);

                    @Override
                    public ScheduledExecutorService get() {

                        return service;
                    }
                }));
    }
}
