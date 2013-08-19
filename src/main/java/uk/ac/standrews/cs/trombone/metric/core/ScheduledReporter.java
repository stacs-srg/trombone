package uk.ac.standrews.cs.trombone.metric.core;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.mashti.jetson.util.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public abstract class ScheduledReporter implements Reporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledReporter.class);
    private final MetricRegistry registry;
    private final ScheduledExecutorService scheduler;
    private volatile ScheduledFuture<?> scheduled_report;

    public ScheduledReporter(MetricRegistry registry) {

        this.registry = registry;
        scheduler = constructSchedulerService(registry);
    }

    public synchronized void start(long interval, TimeUnit unit) {

        if (!isReportScheduled()) {
            scheduled_report = scheduler.scheduleAtFixedRate(new Runnable() {

                @Override
                public void run() {
                    LOGGER.debug("reporter started for registry {}", registry.getName());
                    try {
                        report();
                    }
                    catch (final Exception e) {
                        LOGGER.error("failure occured while reporting", e);
                    }
                    finally {
                        LOGGER.debug("reporter stopped for registry {}", registry.getName());
                    }
                }
            }, 0, interval, unit);
        }
    }

    public synchronized void stop() {

        if (isReportScheduled()) {
            scheduled_report.cancel(false);
            report();
        }
    }

    public synchronized boolean isReportScheduled() {

        return scheduled_report != null && !scheduled_report.isDone();
    }

    protected MetricRegistry getRegistry() {

        return registry;
    }

    private static ScheduledExecutorService constructSchedulerService(MetricRegistry registry) {

        final NamedThreadFactory thread_factory = new NamedThreadFactory(registry.getName() + "_reporter_");
        thread_factory.setDaemon(true);
        return Executors.newSingleThreadScheduledExecutor(thread_factory);
    }
}