package uk.ac.standrews.cs.trombone.core;

import java.io.Serializable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.mashti.jetson.util.NamedThreadFactory;
import uk.ac.standrews.cs.trombone.core.util.Named;
import uk.ac.standrews.cs.trombone.core.util.NamingUtils;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class MaintenanceFactory implements Serializable, Named {

    private static final long serialVersionUID = -15296211081078575L;

    public static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(300, new NamedThreadFactory("maintenance_", true));
    private final DisseminationStrategy strategy;

    public MaintenanceFactory() {

        this(new DisseminationStrategy());

    }

    public MaintenanceFactory(DisseminationStrategy strategy) {

        this.strategy = strategy;
    }

    @Override
    public String getName() {

        return NamingUtils.name(this);
    }

    public DisseminationStrategy getStrategy() {

        return strategy;
    }

    protected PeerMaintainer maintain(Peer peer) {

        final PeerMaintainer listener = new PeerMaintainer(peer, strategy, SCHEDULER);
        peer.addExposureChangeListener(listener);
        return listener;
    }
}
