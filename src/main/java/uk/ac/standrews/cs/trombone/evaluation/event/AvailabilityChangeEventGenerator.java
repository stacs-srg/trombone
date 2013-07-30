package uk.ac.standrews.cs.trombone.evaluation.event;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import javax.inject.Provider;
import uk.ac.standrews.cs.shabdiz.host.Host;
import uk.ac.standrews.cs.trombone.PeerReference;
import uk.ac.standrews.cs.trombone.churn.Churn;
import uk.ac.standrews.cs.trombone.evaluation.Scenario2;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class AvailabilityChangeEventGenerator {

    private final long experiment_duration_nanos;
    private final Scenario2 scenario;
    private final Provider<Churn> churn_provider;

    public AvailabilityChangeEventGenerator(Scenario2 scenario) {

        this.scenario = scenario;
        experiment_duration_nanos = scenario.getExperimentDuration().getLength(TimeUnit.NANOSECONDS);
        churn_provider = scenario.getChurnProvider();
    }

    public TreeSet<AvailabilityChangeEvent> generate() {

        final TreeSet<AvailabilityChangeEvent> availabilityChangeEvents = new TreeSet<AvailabilityChangeEvent>();
        final Map<Host, Integer> peers_per_host = scenario.getMaximumPeersPerHost();
        for (Map.Entry<Host, Integer> entry : peers_per_host.entrySet()) {

            final Host host = entry.getKey();
            final Integer count = entry.getValue();

            for (int i = 0; i < count; i++) {
                final Set<AvailabilityChangeEvent> events = getAvailabilityChangeEvents(host, experiment_duration_nanos);
                availabilityChangeEvents.addAll(events);
            }
        }
        return availabilityChangeEvents;
    }

    private Set<AvailabilityChangeEvent> getAvailabilityChangeEvents(Host host, long total_time_in_nanos) {

        final PeerReference peer_reference = generateByAddress(host.getAddress());
        long time_through_experiment_nanos = 0;
        final HashSet<AvailabilityChangeEvent> availability_events = new HashSet<AvailabilityChangeEvent>();
        boolean arrived_once = false;

        final Churn churn = churn_provider.get();

        while (time_through_experiment_nanos <= total_time_in_nanos) {

            final Churn.Availability availability = churn.getAvailabilityAt(time_through_experiment_nanos);
            final long availability_duration = availability.getDuration().getLength(TimeUnit.NANOSECONDS);
            final boolean available = availability.isAvailable();
            final long trimmed_availability_duration;

            arrived_once |= available;

            if (availability_duration > total_time_in_nanos) {
                trimmed_availability_duration = total_time_in_nanos;
            }
            else {
                trimmed_availability_duration = availability_duration;
            }

            final boolean permanantly_departed = !available && trimmed_availability_duration >= total_time_in_nanos;

            final AvailabilityChangeEvent event = new AvailabilityChangeEvent(peer_reference, time_through_experiment_nanos, available, trimmed_availability_duration);

            availability_events.add(event);
            if (permanantly_departed && arrived_once) {

                final long remaining_time = total_time_in_nanos - time_through_experiment_nanos;
                final Set<AvailabilityChangeEvent> availabilityChangeEvents = getAvailabilityChangeEvents(host, remaining_time);
                availability_events.addAll(availabilityChangeEvents);
            }

            if (trimmed_availability_duration >= total_time_in_nanos && available) {
                availability_events.add(new AvailabilityChangeEvent(peer_reference, experiment_duration_nanos, false, Long.MAX_VALUE));
            }

            time_through_experiment_nanos += availability_duration;
        }

        return availability_events;
    }

    PeerReference generateByAddress(InetAddress address) {

        return null;
    }

}
