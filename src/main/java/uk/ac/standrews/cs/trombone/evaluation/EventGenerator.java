package uk.ac.standrews.cs.trombone.evaluation;

import au.com.bytecode.opencsv.CSVWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.trombone.churn.Churn;
import uk.ac.standrews.cs.trombone.key.Key;
import uk.ac.standrews.cs.trombone.workload.Workload;

import static uk.ac.standrews.cs.trombone.churn.Churn.Availability;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class EventGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventGenerator.class);
    private final Scenario scenario;
    private final long experiment_duration_nanos;
    private final File scenario_home_dir;
    private final SortedSet<Participant> participants;
    private final Map<String, Integer> host_index;
    private final ConcurrentSkipListSet<ExperimentEvent> events = new ConcurrentSkipListSet<ExperimentEvent>();

    public EventGenerator(Scenario scenario, File events_home) throws IOException {

        this.scenario = scenario;
        scenario_home_dir = new File(events_home, scenario.getName());
        experiment_duration_nanos = scenario.getExperimentDurationInNanos();
        participants = new TreeSet<Participant>();
        host_index = new HashMap<String, Integer>();

        FileUtils.forceMkdir(scenario_home_dir);
        persistHosts();
    }

    public void generate() throws IOException {

        final SortedSet<AvailabilityChangeEvent> availability_events = generateAvailabilityChangeEvents();
        persistPeerReferences();
        generateLookupEvents(availability_events);
    }

    private void persistPeerReferences() throws IOException {

        final File peers_csv = new File(scenario_home_dir, "peers.csv");
        final CSVWriter writer = new CSVWriter(new FileWriter(peers_csv));
        try {
            writeCSVRow(writer, "index", "key", "address", "port");
            for (Participant participant : participants) {
                writeCSVRow(writer, participant.getId(), participant.getKey(), participant.getHostName(), participant.getPort());
            }
        }
        finally {
            writer.close();
        }
    }

    private void persistHosts() throws IOException {

        final File hosts_csv = new File(scenario_home_dir, "hosts.csv");
        final CSVWriter writer = new CSVWriter(new FileWriter(hosts_csv));
        try {
            writeCSVRow(writer, "index", "name", "max_peers");
            int index = 1;
            for (String host : scenario.getHostNames()) {
                final Integer max_peers_on_host = scenario.getMaximumPeersOnHost(host);
                writeCSVRow(writer, index, host, max_peers_on_host);
                host_index.put(host, index);
                index++;
            }
        }
        finally {
            writer.close();
        }
    }

    static void writeCSVRow(CSVWriter writer, Object... values) {

        final String[] string_values = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            string_values[i] = String.valueOf(values[i]);
        }

        writer.writeNext(string_values);
    }

    private SortedSet<AvailabilityChangeEvent> generateAvailabilityChangeEvents() throws IOException {

        final TreeSet<AvailabilityChangeEvent> availability_events = new TreeSet<AvailabilityChangeEvent>();
        final Set<String> hosts = scenario.getHostNames();

        for (final String host : hosts) {

            final SortedSet<AvailabilityChangeEvent> events = generateAvailabilityChangeEventsOnHost(host);
            availability_events.addAll(events);
            persistAvailabilityChangeEventsOfHost(events, host);
        }
        return availability_events;
    }

    private void persistAvailabilityChangeEventsOfHost(final SortedSet<AvailabilityChangeEvent> events, final String host) throws IOException {

        final Integer host_index = this.host_index.get(host);
        final File host_events_home = new File(scenario_home_dir, String.valueOf(host_index));

        FileUtils.forceMkdir(host_events_home);

        final File churn_csv = new File(host_events_home, "churn.csv");
        final CSVWriter writer = new CSVWriter(new FileWriter(churn_csv));
        try {

            writeCSVRow(writer, "time", "participant_id", "availability");
            for (AvailabilityChangeEvent event : events) {
                writeCSVRow(writer, event.getTimeInNanos(), event.getSource().getId(), event.isAvailable());
            }
        }
        finally {
            writer.close();
        }

    }

    private void generateLookupEvents(final SortedSet<AvailabilityChangeEvent> availability_events) throws IOException {

        final Oracle oracle = new Oracle(availability_events, participants, scenario_home_dir);
        for (String host : scenario.getHostNames()) {

            final Integer host_index = this.host_index.get(host);
            final File host_events_home = new File(scenario_home_dir, String.valueOf(host_index));

            FileUtils.forceMkdir(host_events_home);

            final File workload_csv = new File(host_events_home, "workload.csv");
            final CSVWriter writer = new CSVWriter(new FileWriter(workload_csv));
            try {

                writeCSVRow(writer, "time", "participant_id", "target", "expected_result");
                final SortedSet<LookupEvent> host_lookups = new TreeSet<LookupEvent>();
                for (AvailabilityChangeEvent availability_event : availability_events) {

                    if (availability_event.isAvailable() && availability_event.getSource().getHostName().equals(host)) {
                        final Participant peer = availability_event.getSource();
                        final long availability_duration = availability_event.getAvailabilityDurationInNanos();
                        final long time = availability_event.getTimeInNanos();
                        final SortedSet<LookupEvent> lookups = generateLookupEvents(oracle, peer, time, availability_duration);
                        host_lookups.addAll(lookups);
                    }
                }

                for (LookupEvent event : host_lookups) {
                    writeCSVRow(writer, event.getTimeInNanos(), event.getSource().getId(), event.getTarget(), event.getExpectedResult().getId());
                }
            }
            finally {
                writer.close();
            }
        }
    }

    private SortedSet<LookupEvent> generateLookupEvents(final Oracle oracle, final Participant peer, final long time, final long availability_duration) {

        final Workload workload = peer.getWorkload();
        final TreeSet<LookupEvent> events = new TreeSet<LookupEvent>();
        Workload.Lookup lookupAt = workload.getLookupAt(time);
        Key target = lookupAt.getTarget();
        long interval = lookupAt.getInterval().getLength(TimeUnit.NANOSECONDS);
        long lookup_time = time + interval;
        final long end_time = Math.min(availability_duration + time, experiment_duration_nanos);
        while (lookup_time < end_time) {
            final Participant expected_result = oracle.lookupAtTime(target, lookup_time);
            events.add(new LookupEvent(peer, target, expected_result, lookup_time));

            lookupAt = workload.getLookupAt(time);
            target = lookupAt.getTarget();
            interval = lookupAt.getInterval().getLength(TimeUnit.NANOSECONDS);
            lookup_time += interval;
        }
        return events;
    }

    private SortedSet<AvailabilityChangeEvent> generateAvailabilityChangeEventsOnHost(final String host) throws IOException {

        final Integer max_peers_per_host = scenario.getMaximumPeersOnHost(host);
        final TreeSet<AvailabilityChangeEvent> experiment_events = new TreeSet<AvailabilityChangeEvent>();
        for (int i = 0; i < max_peers_per_host; i++) {

            final SortedSet<AvailabilityChangeEvent> events = generateAvailabilityEvents(0, host);
            experiment_events.addAll(events);
        }
        return experiment_events;
    }

    private SortedSet<AvailabilityChangeEvent> generateAvailabilityEvents(long start_time_nanos, final String host) {

        final SortedSet<AvailabilityChangeEvent> events = new TreeSet<AvailabilityChangeEvent>();
        final Participant participant = newPeerReference(host);
        final Churn churn = participant.getChurn();

        long time_nanos = start_time_nanos;
        Long last_available_time = null;
        boolean alive_at_last = false;
        while (time_nanos < experiment_duration_nanos) {

            final Availability availability = churn.getAvailabilityAt(time_nanos);
            final boolean available = availability.isAvailable();
            final long duration_nanos = Math.min(availability.getDurationInNanos(), experiment_duration_nanos - time_nanos);
            alive_at_last |= available;
            if (available) {
                last_available_time = Math.min(time_nanos + duration_nanos, experiment_duration_nanos);
            }
            events.add(new AvailabilityChangeEvent(participant, time_nanos, available, duration_nanos));
            time_nanos += duration_nanos;
        }

        if (last_available_time != null && last_available_time < experiment_duration_nanos) {
            final SortedSet<AvailabilityChangeEvent> events_after_permanant_departure = generateAvailabilityEvents(last_available_time, host);
            events.addAll(events_after_permanant_departure);
        }

        if (alive_at_last) {
            events.add(new AvailabilityChangeEvent(participant, experiment_duration_nanos, false, 0L));
        }

        return events;
    }

    private Participant newPeerReference(final String host) {

        Participant participant = scenario.newParticipantOnHost(host);

        final boolean added = participants.add(participant);
        if (!added) {
            LOGGER.warn("duplicate participant was generated by scenario {} on host {}; participant: {}", scenario, host, participant);
        }
        return participant;
    }
}
