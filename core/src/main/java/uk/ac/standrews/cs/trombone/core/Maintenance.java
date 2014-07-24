package uk.ac.standrews.cs.trombone.core;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.mashti.gauge.Rate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class Maintenance implements PropertyChangeListener {

    public static final Rate RECONFIGURATION_RATE = new Rate();
    private static final Logger LOGGER = LoggerFactory.getLogger(Maintenance.class);
    protected final Peer peer;
    protected final ScheduledExecutorService scheduler;
    private final AtomicReference<DisseminationStrategy> strategy;
    private final Runnable nonOpportunisticDisseminator;
    private volatile boolean started;
    private ScheduledFuture<?> non_opp_maintenance;

    protected Maintenance(Peer peer, DisseminationStrategy strategy, ScheduledExecutorService scheduler) {

        this.peer = peer;
        this.scheduler = scheduler;
        this.strategy = new AtomicReference<>(strategy);
        nonOpportunisticDisseminator = newNonOpportunisticDisseminator();
        peer.addExposureChangeListener(this);
    }

    protected Runnable newNonOpportunisticDisseminator() {

        return new NonOpportunisticDisseminator();
    }

    protected DisseminationStrategy getDisseminationStrategy() {

        return strategy.get();
    }

    protected DisseminationStrategy setDisseminationStrategy(DisseminationStrategy new_strategy) {

        final DisseminationStrategy old_strategy = strategy.getAndSet(new_strategy);
        if (!Objects.equals(old_strategy, new_strategy)) {
            RECONFIGURATION_RATE.mark();
        }
        return old_strategy;
    }

    @Override
    public void propertyChange(final PropertyChangeEvent event) {

        if (peer.equals(event.getSource())) {
            final boolean exposed = (boolean) event.getNewValue();
            if (exposed) {
                start();
            }
            else {
                stop();
            }
        }
        else {
            LOGGER.error("bad code! same listener is registered to multiple peers");
        }
    }

    protected synchronized void start() {

        if (!isStarted()) {
            non_opp_maintenance = scheduler.scheduleWithFixedDelay(nonOpportunisticDisseminator, ThreadLocalRandom.current().nextInt(500, 5000), 2000, TimeUnit.MILLISECONDS);
            started = true;
        }
    }

    protected synchronized void stop() {

        if (isStarted()) {
            non_opp_maintenance.cancel(true);
            started = false;
        }
    }

    protected synchronized boolean isStarted() {

        return started;
    }

    protected class NonOpportunisticDisseminator implements Runnable {

        @Override
        public void run() {

            try {
                final DisseminationStrategy current_strategy = strategy.get();
                if (current_strategy != null) {
                    for (DisseminationStrategy.Action action : current_strategy) {
                        if (!action.isOpportunistic()) {
                            action.nonOpportunistically(peer);
                        }
                    }
                }
            }
            catch (Throwable e) {
                LOGGER.error("failed to execute non-opportunistic maintenance", e);
            }
        }
    }
}
