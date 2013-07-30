package uk.ac.standrews.cs.trombone;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import uk.ac.standrews.cs.shabdiz.util.Duration;
import uk.ac.standrews.cs.trombone.gossip.NonOpportunisticGossip;
import uk.ac.standrews.cs.trombone.gossip.OpportunisticGossip;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class Maintenance {

    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(100);
    private final List<NonOpportunisticGossip> non_opportunistic_gossips;
    private final List<OpportunisticGossip> opportunistic_gossips;
    private final List<ScheduledFuture<?>> scheduled_non_opportunistic_gossips;
    private final AtomicBoolean started;

    public Maintenance() {

        non_opportunistic_gossips = new ArrayList<NonOpportunisticGossip>();
        opportunistic_gossips = new ArrayList<OpportunisticGossip>();
        scheduled_non_opportunistic_gossips = new ArrayList<ScheduledFuture<?>>();
        started = new AtomicBoolean();
    }

    public synchronized void start() {

        if (started.compareAndSet(false, true)) {
            for (NonOpportunisticGossip gossip : non_opportunistic_gossips) {
                schedule(gossip);
            }
        }
    }

    public synchronized void stop() {

        if (started.compareAndSet(true, false)) {
            for (ScheduledFuture<?> future : scheduled_non_opportunistic_gossips) {
                future.cancel(true);
            }
            scheduled_non_opportunistic_gossips.clear();
        }
    }

    public boolean isStarted() {

        return started.get();
    }

    private boolean schedule(final NonOpportunisticGossip gossip) {

        final Duration delay = gossip.getDelay();
        final long delay_length = delay.getLength();
        final TimeUnit delay_unit = delay.getTimeUnit();
        final ScheduledFuture<?> future = SCHEDULER.scheduleWithFixedDelay(gossip, delay_length, delay_length, delay_unit);
        return scheduled_non_opportunistic_gossips.add(future);
    }

    protected boolean addOpprotunisticGossip(OpportunisticGossip gossip) {

        return opportunistic_gossips.add(gossip);
    }

    protected synchronized boolean addNonOpprotunisticGossip(NonOpportunisticGossip gossip) {

        return non_opportunistic_gossips.add(gossip) && isStarted() ? schedule(gossip) : true;
    }

    protected List<OpportunisticGossip> getOpportunisticGossips() {

        return new CopyOnWriteArrayList(opportunistic_gossips);
    }

    protected List<NonOpportunisticGossip> getNonOpportunisticGossips() {

        return new CopyOnWriteArrayList(non_opportunistic_gossips);
    }
}
