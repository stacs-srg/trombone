package uk.ac.standrews.cs.trombone.trombone;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import uk.ac.standrews.cs.trombone.trombone.gossip.NonOpportunisticGossip;
import uk.ac.standrews.cs.trombone.trombone.gossip.OpportunisticGossip;
import uk.ac.standrews.cs.shabdiz.util.Duration;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class Maintenance implements PropertyChangeListener {

    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(100);
    private final Peer peer;
    private final List<NonOpportunisticGossip> non_opportunistic_gossips;
    private final List<OpportunisticGossip> opportunistic_gossips;
    private List<ScheduledFuture<?>> scheduled_maintenance;

    public Maintenance(Peer peer, List<NonOpportunisticGossip> non_opportunistic_gossips, List<OpportunisticGossip> opportunistic_gossips) {

        this.peer = peer;
        this.non_opportunistic_gossips = non_opportunistic_gossips;
        this.opportunistic_gossips = opportunistic_gossips;
        scheduled_maintenance = new ArrayList<ScheduledFuture<?>>();
        peer.addExposureChangeListener(this);
    }

    @Override
    public void propertyChange(final PropertyChangeEvent event) {

        final Boolean exposed = (Boolean) event.getNewValue();
        if (exposed) {
            start();
        }
        else {
            stop();
        }
    }

    private synchronized void stop() {

        for (ScheduledFuture<?> future : scheduled_maintenance) {
            future.cancel(true);
        }
        peer.setOpportunisticGossip(null);
        scheduled_maintenance.clear();
    }

    private synchronized void start() {

        if (non_opportunistic_gossips != null) {
            for (NonOpportunisticGossip gossip : non_opportunistic_gossips) {
                final Duration delay = gossip.getDelay();
                final long delay_length = delay.getLength();
                final ScheduledFuture<?> future = SCHEDULER.scheduleWithFixedDelay(gossip, delay_length, delay_length, delay.getTimeUnit());
                scheduled_maintenance.add(future);
            }
        }
        peer.setOpportunisticGossip(opportunistic_gossips);
    }
}
