package uk.ac.standrews.cs.trombone.core;

import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.mashti.gauge.Rate;
import org.mashti.jetson.exception.RPCException;
import org.mashti.jetson.util.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.trombone.core.util.Named;
import uk.ac.standrews.cs.trombone.core.util.NamingUtils;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class Maintenance implements Serializable, Named {

    public static final Rate RECONFIGURATION_RATE = new Rate();
    //FIXME think of how not to use this fixed size pool; needs to be reconfigured based on the size of the network
    public static final ListeningScheduledExecutorService SCHEDULER = MoreExecutors.listeningDecorator(Executors.newScheduledThreadPool(300, new NamedThreadFactory("maintenance_", true)));
    private final Logger logger = LoggerFactory.getLogger(Maintenance.class);
    private static final long serialVersionUID = -15296211081078575L;
    private final DisseminationStrategy strategy;

    public Maintenance() {

        this(new DisseminationStrategy());

    }

    public Maintenance(DisseminationStrategy strategy) {

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

        final PeerMaintainer listener = new PeerMaintainer(peer, strategy);
        peer.addExposureChangeListener(listener);
        return listener;
    }

    public class PeerMaintainer implements PropertyChangeListener {

        private final Peer peer;
        private final AtomicReference<DisseminationStrategy> strategy;
        private final NonOpportunisticDisseminator nonOpportunisticDisseminator;
        private volatile boolean started;
        private ScheduledFuture<?> non_opp_maintenance;

        protected PeerMaintainer(Peer peer, DisseminationStrategy strategy) {

            this.peer = peer;
            this.strategy = new AtomicReference<>(strategy);
            nonOpportunisticDisseminator = new NonOpportunisticDisseminator();
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

            try {
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
                    logger.warn("bad code! same listener is registered to multiple peers");
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        protected synchronized void start() {

            if (!isStarted()) {
                non_opp_maintenance = SCHEDULER.scheduleWithFixedDelay(nonOpportunisticDisseminator, ThreadLocalRandom.current().nextInt(500, 5000), 2000, TimeUnit.MILLISECONDS);
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

        private class NonOpportunisticDisseminator implements Runnable {

            @Override
            public void run() {

                try {
                    final DisseminationStrategy current_strategy = strategy.get();
                    if (current_strategy != null) {
                        for (DisseminationStrategy.Action action : current_strategy) {
                            if (!action.isOpportunistic()) {
                                try {
                                    action.nonOpportunistically(peer);
                                }
                                catch (RPCException e) {
                                    logger.debug("failed to execute non opportunistic dissemination strategy", e);
                                }
                            }
                        }
                    }
                }
                catch (Throwable e) {
                    logger.error("failed to execute non-opportunistic maintenance", e);
                }
            }
        }
    }
}
