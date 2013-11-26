package uk.ac.standrews.cs.trombone.evaluation;

import au.com.bytecode.opencsv.CSVWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.trombone.churn.Churn;
import uk.ac.standrews.cs.trombone.core.key.Key;
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
    private final ExecutorService event_generation_executor = Executors.newFixedThreadPool(500);
    private final Map<String, CSVWriter> host_event_writers = new HashMap<String, CSVWriter>();
    private final List<EventGeneratorDelegate> delegates = new ArrayList<EventGeneratorDelegate>();
    private final AtomicLong min_time = new AtomicLong();
    private final ConcurrentHashMap<Key, Integer> lookup_targets_index;
    private final AtomicInteger next_target_index = new AtomicInteger();
    private final CSVWriter oracle_csv_writer;
    private final AtomicLong oracle_change_time = new AtomicLong();

    public EventGenerator(Scenario scenario, File events_home) throws IOException {

        this.scenario = scenario;
        scenario_home_dir = new File(events_home, scenario.getName());
        experiment_duration_nanos = scenario.getExperimentDurationInNanos();
        participants = new TreeSet<Participant>();
        host_index = new HashMap<String, Integer>();
        lookup_targets_index = new ConcurrentHashMap<Key, Integer>();
        oracle_csv_writer = new CSVWriter(new FileWriter(new File(scenario_home_dir, "oracle.csv")));
        writeCSVRow(oracle_csv_writer, "time", "alive_peer_indices");
        init();
    }

    public void generate() throws IOException, ExecutionException, InterruptedException {

        Future<Void> persistor = null;
        final NavigableMap<Key, Participant> alive_peers = new ConcurrentSkipListMap<Key, Participant>();
        final ConcurrentSkipListSet<Event> events = new ConcurrentSkipListSet<Event>();

        while (!delegates.isEmpty()) {
            final Iterator<EventGeneratorDelegate> iterator = delegates.iterator();
            Event min_event = null;

            while (iterator.hasNext()) {

                final EventGeneratorDelegate deligate = iterator.next();
                final Event event = deligate.getNextEvent();
                if (event != null) {
                    min_event = min_event == null || min_event.getTimeInNanos().compareTo(event.getTimeInNanos()) > 0 ? event : min_event;
                    events.add(event);
                }
                else {
                    iterator.remove();
                }
            }

            final NavigableSet<Event> persistable_events = min_event == null ? events : events.headSet(min_event, true);
            persistor = startPersistor(persistor, persistable_events, alive_peers);
            System.out.println(min_event == null ? experiment_duration_nanos : min_event.getTimeInNanos());
        }
        min_time.getAndSet(experiment_duration_nanos);
        System.out.println(events.size());
        System.out.println("waiting for persistor ");
        if (persistor != null) {
            persistor.get();
        }
        persistParticipants();
        persistTargetKeys();
        writeOracleRow(experiment_duration_nanos, alive_peers.values());
        oracle_csv_writer.close();
        System.out.println(events.size());
    }

    private void persistTargetKeys() throws IOException {

        final File hosts_csv = new File(scenario_home_dir, "lookup_targets.csv");
        final CSVWriter writer = new CSVWriter(new FileWriter(hosts_csv));
        try {
            writeCSVRow(writer, "index", "key");
            for (Map.Entry<Key, Integer> entry : lookup_targets_index.entrySet()) {
                writeCSVRow(writer, entry.getValue(), entry.getKey());
            }
        }
        finally {
            writer.close();
        }
    }

    private Future<Void> startPersistor(final Future<Void> persistor, final NavigableSet<Event> events, final NavigableMap<Key, Participant> alive_peers) {

        return event_generation_executor.submit(new Callable<Void>() {

            @Override
            public Void call() throws Exception {

                try {
                    if (persistor != null) {
                        persistor.get();
                    }
                    if (events.isEmpty()) {
                        System.out.println("empty");
                        return null;
                    }

                    for (Event event : events) {
                        final Participant peer = event.getParticipant();
                        final Long event_time = event.getTimeInNanos();
                        if (event instanceof ChurnEvent) {
                            ChurnEvent churnEvent = (ChurnEvent) event;
                            final Key peer_key = peer.getKey();

                            final long old_time = oracle_change_time.getAndSet(event_time);
                            if (event_time != old_time) {
                                writeOracleRow(old_time, alive_peers.values());
                            }

                            if (churnEvent.isAvailable()) {
                                alive_peers.put(peer_key, peer);
                            }
                            else {
                                alive_peers.remove(peer_key);
                            }
                        }
                        else {
                            LookupEvent lookupEvent = (LookupEvent) event;
                            if (alive_peers.isEmpty()) { throw new IllegalStateException("no peer is alive at the given time and a lookup is happening?! something is wrong"); }
                            Map.Entry<Key, Participant> expected_result = alive_peers.ceilingEntry(lookupEvent.getTarget());
                            if (expected_result == null) {
                                expected_result = alive_peers.firstEntry();
                            }
                            lookupEvent.setExpectedResult(expected_result.getValue());
                        }

                        final CSVWriter writer = host_event_writers.get(event.getSource().getAddress().getHostName());
                        writeCSVRow(writer, event_time, event.getSourceId(), event.getCode(), event.getParameters());

                    }
                    events.clear();

                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                for (CSVWriter writer : host_event_writers.values()) {
                    writer.flush();
                }

                return null;
            }
        }

        );
    }

    private void writeOracleRow(long time, final Collection<Participant> participants) throws IOException {

        StringBuilder sb = new StringBuilder();
        for (Participant participant : new TreeSet<Participant>(participants)) {
            sb.append(participant.getId());
            sb.append(' ');
        }

        writeCSVRow(oracle_csv_writer, time, sb.toString().trim());
        oracle_csv_writer.flush();
    }

    private void init() throws IOException {

        final Set<String> hosts = scenario.getHostNames();
        FileUtils.forceMkdir(scenario_home_dir);
        persistHosts();

        for (final String host : hosts) {
            final File host_home = new File(scenario_home_dir, String.valueOf(host_index.get(host)));
            FileUtils.forceMkdir(host_home);
            final File events_csv = new File(host_home, "events.csv");
            final CSVWriter writer = new CSVWriter(new FileWriter(events_csv), CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER);
            writeCSVRow(writer, "time", "participant_id", "event_code", "parameters");
            host_event_writers.put(host, writer);

            for (int i = 0; i < scenario.getMaximumPeersOnHost(host); i++) {
                final EventGeneratorDelegate deligate = new EventGeneratorDelegate(host);
                delegates.add(deligate);
            }
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

    private void persistParticipants() throws IOException {

        final File hosts_csv = new File(scenario_home_dir, "peers.csv");
        final CSVWriter writer = new CSVWriter(new FileWriter(hosts_csv));
        try {
            writeCSVRow(writer, "index", "key", "address", "port");
            for (Participant peer : participants) {
                writeCSVRow(writer, peer.getId(), peer.getKey().getValue(), peer.getHostName(), peer.getPort());
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

    class EventGeneratorDelegate {

        private final String host;
        private final AtomicLong time_nanos = new AtomicLong(0);
        private volatile Participant participant;
        private volatile Churn churn;
        private volatile Workload workload;
        private volatile ChurnEvent current_availability;
        private volatile long end_of_current_availability;
        private boolean arrived_at_least_once;

        EventGeneratorDelegate(String host) {

            this.host = host;
            init();
        }

        String getHost() {

            return host;
        }

        private void init() {

            participant = newPeerReference(host);
            churn = participant.getChurn();
            workload = participant.getWorkload();
            arrived_at_least_once = false;
        }

        long getTimeInNanos() {

            return time_nanos.get();
        }

        synchronized Event getNextEvent() {

            final long current_time = getTimeInNanos();
            if (isBeyondExperimentationTime(current_time)) {
                if (current_availability.isAvailable()) {
                    current_availability = new ChurnEvent(participant, experiment_duration_nanos, false);
                    return current_availability;
                }
                return null;
            }

            if (current_availability == null) { return getChurnEventAt(current_time); }

            if (!current_availability.isAvailable()) {

                if (isBeyondExperimentationTime(end_of_current_availability)) {
                    if (arrived_at_least_once) {
                        //                        init();
                        //                        return getChurnEventAt(current_time);
                        return null;
                    }
                    else {
                        //                        LOGGER.warn("peer didnt arrive at least once!!!!!");
                        //                                                init();
                        //                                                return getChurnEventAt(current_time);
                        return null;
                    }
                }
                else {
                    time_nanos.set(end_of_current_availability);
                    return getChurnEventAt(end_of_current_availability);
                }
            }

            if (current_time < end_of_current_availability) {

                final Workload.Lookup lookup = workload.getLookupAt(current_time);
                final Key target = lookup.getTarget();
                final long interval = lookup.getIntervalInNanos();
                final long lookup_time = current_time + interval;

                if (lookup_time < end_of_current_availability) {

                    time_nanos.addAndGet(interval);
                    final LookupEvent lookupEvent = new LookupEvent(participant, lookup_time, target);
                    lookupEvent.setTargetId(getLookupTargetIndex(target));
                    return lookupEvent;
                }
                else {
                    time_nanos.set(end_of_current_availability);
                    return getChurnEventAt(end_of_current_availability);
                }
            }
            else {
                time_nanos.set(end_of_current_availability);
                return getChurnEventAt(end_of_current_availability);
            }
        }

        private ChurnEvent getChurnEventAt(final long time) {

            if (isBeyondExperimentationTime(time)) {
                current_availability = new ChurnEvent(participant, experiment_duration_nanos, false);
                current_availability.setDurationInNanos(0L);
                return current_availability;
            }

            Availability availability = churn.getAvailabilityAt(time);
            final long duration_nanos = Math.min(availability.getDurationInNanos(), experiment_duration_nanos - time);
            final boolean available = availability.isAvailable();
            current_availability = new ChurnEvent(participant, time, available);
            current_availability.setDurationInNanos(duration_nanos);
            end_of_current_availability = time + duration_nanos;
            arrived_at_least_once |= available;
            return current_availability;
        }

        boolean isBeyondExperimentationTime(long time_nanos) {

            return experiment_duration_nanos <= time_nanos;
        }

        long remainingExperimentTime(long time_nanos) {

            return experiment_duration_nanos - time_nanos;
        }

        private synchronized Integer getLookupTargetIndex(Key target) {

            final Integer index;
            if (lookup_targets_index.containsKey(target)) {
                index = lookup_targets_index.get(target);
            }
            else {
                index = next_target_index.getAndIncrement();
                lookup_targets_index.putIfAbsent(target, index);
            }

            return index;
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
}
