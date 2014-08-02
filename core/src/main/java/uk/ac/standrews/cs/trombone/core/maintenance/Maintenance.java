package uk.ac.standrews.cs.trombone.core.maintenance;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.trombone.core.Peer;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public abstract class Maintenance implements PropertyChangeListener, Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Maintenance.class);

    protected final Peer local;
    protected final ScheduledExecutorService scheduler;
    private volatile boolean started;
    private ScheduledFuture<?> periodic_maintenance;
    private long interval;
    private TimeUnit interval_unit;

    protected Maintenance(Peer local, long interval, TimeUnit interval_unit) {

        this.local = local;
        this.interval = interval;
        this.interval_unit = interval_unit;
        scheduler = local.getExecutor();
        local.addExposureChangeListener(this);
    }

    public void setInterval(long interval, TimeUnit interval_unit) {

        this.interval = interval;
        this.interval_unit = interval_unit;
    }

    @Override
    public synchronized void propertyChange(final PropertyChangeEvent event) {

        final boolean exposed = (boolean) event.getNewValue();
        if (exposed) {
            start();
        }
        else {
            stop();
        }
    }

    protected synchronized void start() {

        if (!isStarted()) {
            periodic_maintenance = scheduler.scheduleWithFixedDelay(this, 0, interval, interval_unit);
            started = true;

            LOGGER.debug("started maintenance on {}", local);
        }
    }

    protected synchronized void stop() {

        if (isStarted()) {
            periodic_maintenance.cancel(true);
            started = false;

            LOGGER.debug("stopped maintenance on {}", local);
        }
    }

    protected synchronized boolean isStarted() {

        return started;
    }

    protected synchronized void restart() {

        if (isStarted()) {
            stop();
            start();
        }
    }
}
