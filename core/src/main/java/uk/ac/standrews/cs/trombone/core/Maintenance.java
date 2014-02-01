package uk.ac.standrews.cs.trombone.core;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.mashti.jetson.exception.RPCException;
import org.mashti.jetson.util.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class Maintenance {

    //FIXME think of how not to use this fixed size pool; needs to be reconfigured based on the size of the network
    protected static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(500, new NamedThreadFactory("maintenance_", true));
    private static final Logger LOGGER = LoggerFactory.getLogger(Maintenance.class);
    private static final int ACTIVE_MAINTENANCE_INTERVAL_MILLIS = 1500;
    private final AtomicReference<DisseminationStrategy> dissemination_strategy;
    private final Peer local;
    private final Runnable non_opportunistic_disseminator = new NonOpportunisticDisseminator();
    private ScheduledFuture<?> active_maintenance;

    protected Maintenance(final Peer local) {

        this.local = local;
        dissemination_strategy = new AtomicReference<>();
    }

    public synchronized boolean isStarted() {

        return active_maintenance != null && !active_maintenance.isDone();
    }

    protected synchronized void start() {

        if (!isStarted()) {
            active_maintenance = SCHEDULER.scheduleWithFixedDelay(non_opportunistic_disseminator, ACTIVE_MAINTENANCE_INTERVAL_MILLIS, ACTIVE_MAINTENANCE_INTERVAL_MILLIS, TimeUnit.MILLISECONDS);
        }
    }

    protected synchronized void stop() {

        if (isStarted()) {
            active_maintenance.cancel(true);
        }
    }

    protected DisseminationStrategy getDisseminationStrategy() {

        return dissemination_strategy.get();
    }

    protected DisseminationStrategy getAndSet(DisseminationStrategy dissemination_strategy) {

        return this.dissemination_strategy.getAndSet(dissemination_strategy);
    }

    private class NonOpportunisticDisseminator implements Runnable {

        @Override
        public void run() {

            final DisseminationStrategy strategy = getDisseminationStrategy();

            if (strategy != null) {
                for (DisseminationStrategy.Action action : strategy) {
                    if (!action.isOpportunistic()) {
                        try {
                            action.nonOpportunistically(local);
                        }
                        catch (RPCException e) {
                            LOGGER.debug("failed to execute non opportunistic dissemination strategy", e);
                        }
                    }
                }
            }
        }
    }
}
