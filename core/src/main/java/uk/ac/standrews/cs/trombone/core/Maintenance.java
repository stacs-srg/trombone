package uk.ac.standrews.cs.trombone.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.mashti.jetson.exception.RPCException;
import org.mashti.jetson.util.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class Maintenance {

    private static final Logger LOGGER = LoggerFactory.getLogger(Maintenance.class);
    //FIXME think of how not to use this fixed size pool; needs to be reconfigured based on the size of the network
    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(100, new NamedThreadFactory("maintenance_", true));
    private static final int ACTIVE_MAINTENANCE_INTERVAL_MILLIS = 1500;
    private final List<DisseminationStrategy> dissemination_strategies;
    private final Peer local;
    private final Runnable non_opportunistic_disseminator = new NonOpportunisticDisseminator();
    private ScheduledFuture<?> scheduled_non_opportunistic_disseminator;

    protected Maintenance(final Peer local) {

        this.local = local;
        dissemination_strategies = new ArrayList<DisseminationStrategy>();
    }

    public synchronized boolean isStarted() {

        return scheduled_non_opportunistic_disseminator != null && !scheduled_non_opportunistic_disseminator.isDone();
    }

    protected synchronized void start() {

        if (!isStarted()) {
            scheduled_non_opportunistic_disseminator = SCHEDULER.scheduleWithFixedDelay(non_opportunistic_disseminator, ACTIVE_MAINTENANCE_INTERVAL_MILLIS, ACTIVE_MAINTENANCE_INTERVAL_MILLIS, TimeUnit.MILLISECONDS);
        }
    }

    protected synchronized void stop() {

        if (isStarted()) {
            scheduled_non_opportunistic_disseminator.cancel(true);
        }
    }

    protected List<DisseminationStrategy> getDisseminationStrategies() {

        return new CopyOnWriteArrayList<DisseminationStrategy>(dissemination_strategies);
    }

    protected void add(DisseminationStrategy strategy) {

        synchronized (dissemination_strategies) {
            dissemination_strategies.add(strategy);
        }
    }

    protected void reset() {

        synchronized (dissemination_strategies) {
            dissemination_strategies.clear();
        }
    }

    protected void addAll(final List<DisseminationStrategy> strategies) {

        synchronized (dissemination_strategies) {
            dissemination_strategies.addAll(strategies);
        }

    }

    private class NonOpportunisticDisseminator implements Runnable {

        @Override
        public void run() {

            for (DisseminationStrategy dissemination_strategy : getDisseminationStrategies()) {
                if (!dissemination_strategy.isOpportunistic()) {
                    try {
                        dissemination_strategy.nonOpportunistically(local);
                    }
                    catch (RPCException e) {
                        LOGGER.debug("failed to execute non opportunistic dissemination strategy", e);
                    }
                }
            }
        }
    }
}
