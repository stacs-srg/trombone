package uk.ac.standrews.cs.trombone.core.maintenance;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.mashti.gauge.Rate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.trombone.core.Peer;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class StrategicMaintenance extends Maintenance {

    public static final Rate RECONFIGURATION_RATE = new Rate();
    private static final Logger LOGGER = LoggerFactory.getLogger(StrategicMaintenance.class);
    private final AtomicReference<DisseminationStrategy> strategy;

    public StrategicMaintenance(Peer peer, DisseminationStrategy strategy, long interval, TimeUnit interval_unit) {

        super(peer, interval, interval_unit);
        this.strategy = new AtomicReference<>(strategy);
    }

    @Override
    public void run() {

        try {
            final DisseminationStrategy current_strategy = strategy.get();
            if (current_strategy != null) {
                for (DisseminationStrategy.Action action : current_strategy) {
                    if (!action.isOpportunistic()) {
                        action.nonOpportunistically(local);
                    }
                }
            }
        }
        catch (Throwable e) {
            LOGGER.error("failed to execute non-opportunistic maintenance", e);
        }
    }

    public DisseminationStrategy getDisseminationStrategy() {

        return strategy.get();
    }

    protected DisseminationStrategy setDisseminationStrategy(DisseminationStrategy new_strategy) {

        final DisseminationStrategy old_strategy = strategy.getAndSet(new_strategy);
        if (!Objects.equals(old_strategy, new_strategy)) {
            RECONFIGURATION_RATE.mark();
        }
        return old_strategy;
    }
}
