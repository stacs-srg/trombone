package uk.ac.standrews.cs.trombone.core;

import java.math.BigInteger;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import uk.ac.standrews.cs.trombone.core.maintenance.ChordMaintenance;
import uk.ac.standrews.cs.trombone.core.maintenance.Maintenance;
import uk.ac.standrews.cs.trombone.core.state.ChordPeerState;
import uk.ac.standrews.cs.trombone.core.state.PeerState;
import uk.ac.standrews.cs.trombone.core.strategy.ChordJoinStrategy;
import uk.ac.standrews.cs.trombone.core.strategy.ChordLookupStrategy;
import uk.ac.standrews.cs.trombone.core.strategy.ChordNextHopStrategy;
import uk.ac.standrews.cs.trombone.core.strategy.JoinStrategy;
import uk.ac.standrews.cs.trombone.core.strategy.LookupStrategy;
import uk.ac.standrews.cs.trombone.core.strategy.NextHopStrategy;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class TromboneConfiguration implements PeerConfiguration {

    private final int finger_table_size;
    private final BigInteger finger_base;
    private final int successor_list_size;
    private final long maintenance_interval;
    private final TimeUnit maintenance_interval_unit;

    public TromboneConfiguration(int finger_table_size, BigInteger finger_base, int successor_list_size, long maintenance_interval, TimeUnit maintenance_interval_unit) {

        this.finger_table_size = finger_table_size;
        this.finger_base = finger_base;
        this.successor_list_size = successor_list_size;
        this.maintenance_interval = maintenance_interval;
        this.maintenance_interval_unit = maintenance_interval_unit;
    }

    @Override
    public Maintenance getMaintenance(final Peer peer) {

        final ChordMaintenance maintenance = new ChordMaintenance(peer, maintenance_interval, maintenance_interval_unit);
        return maintenance;
    }

    @Override
    public PeerState getPeerState(final Peer peer) {

        return new ChordPeerState(peer, finger_table_size, finger_base, successor_list_size);
    }

    @Override
    public JoinStrategy getJoinStrategy(final Peer peer) {

        return new ChordJoinStrategy(peer);
    }

    @Override
    public LookupStrategy getLookupStrategy(final Peer peer) {

        return new ChordLookupStrategy(peer);
    }

    @Override
    public NextHopStrategy getNextHopStrategy(final Peer peer) {

        return new ChordNextHopStrategy(peer);
    }

    @Override
    public ScheduledExecutorService getExecutor() {

        return null;
    }
}
