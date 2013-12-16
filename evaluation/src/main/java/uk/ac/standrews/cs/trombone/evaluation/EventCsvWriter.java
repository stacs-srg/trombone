package uk.ac.standrews.cs.trombone.evaluation;

import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.io.FileUtils;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;
import uk.ac.standrews.cs.trombone.core.key.Key;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class EventCsvWriter implements Closeable {

    private final ConcurrentHashMap<Key, Integer> lookup_targets_index;
    private final File events_home;
    private final AtomicInteger next_target_index = new AtomicInteger();
    private final HashMap<String, CsvListWriter> host_event_writers;
    private final TreeMap<String, Integer> host_indices;
    private final Set<Participant> participants = new HashSet<>();
    private final AtomicInteger next_host_index = new AtomicInteger();
    private final CsvListWriter hosts_csv_writer;
    private final CsvListWriter peers_csv_writer;
    private final CsvListWriter lookup_targets_csv_writer;
    private final CsvListWriter oracle_csv_writer;

    public EventCsvWriter(File events_home) throws IOException {

        this.events_home = events_home;
        lookup_targets_index = new ConcurrentHashMap<Key, Integer>();
        host_event_writers = new HashMap<>();
        host_indices = new TreeMap<>();
        hosts_csv_writer = new CsvListWriter(new FileWriter(new File(events_home, "hosts.csv")), CsvPreference.STANDARD_PREFERENCE);
        hosts_csv_writer.writeHeader("index", "host_name");
        peers_csv_writer = new CsvListWriter(new FileWriter(new File(events_home, "peers.csv")), CsvPreference.STANDARD_PREFERENCE);
        peers_csv_writer.writeHeader("index", "peer_key", "host_name", "port");
        lookup_targets_csv_writer = new CsvListWriter(new FileWriter(new File(events_home, "lookup_targets.csv")), CsvPreference.STANDARD_PREFERENCE);
        lookup_targets_csv_writer.writeHeader("index", "key");
        oracle_csv_writer = new CsvListWriter(new FileWriter(new File(events_home, "oracle.csv")), CsvPreference.STANDARD_PREFERENCE);
        oracle_csv_writer.writeHeader("time", "alive_peer_indices");
    }

    public void write(Event event) throws IOException {

        final Participant participant = event.getParticipant();
        final CsvListWriter writer = getWriterByParticipant(participant);

        if (event instanceof LookupEvent) {
            LookupEvent lookup_event = (LookupEvent) event;
            lookup_event.setTargetId(getLookupTargetIndex(lookup_event.getTarget()));
        }
        final Long timeInNanos = event.getTimeInNanos();
        writer.write(timeInNanos, event.getParticipant().getId(), event.getCode(), event.getParameters());
        writer.flush();

        if (participants.add(participant)) {
            peers_csv_writer.write(participant.getId(), participant.getKey(),participant.getHostName(), participant.getPort());
            peers_csv_writer.flush();
        }
    }

    public void write(long time, final Collection<Participant> alive_participants) throws IOException {

        StringBuilder sb = new StringBuilder();
        for (Participant participant : new TreeSet<Participant>(alive_participants)) {
            sb.append(participant.getId());
            sb.append(' ');
        }

        oracle_csv_writer.write(time, sb.toString());
        oracle_csv_writer.flush();

    }

    @Override
    public void close() throws IOException {

        for (CsvListWriter writer : host_event_writers.values()) {
            writer.flush();
            writer.close();
        }
        hosts_csv_writer.flush();
        hosts_csv_writer.close();

        peers_csv_writer.flush();
        peers_csv_writer.close();

        oracle_csv_writer.flush();
        oracle_csv_writer.close();

        lookup_targets_csv_writer.flush();
        lookup_targets_csv_writer.close();
    }

    private synchronized CsvListWriter getWriterByParticipant(Participant participant) throws IOException {

        final String host_name = participant.getHostName();
        final CsvListWriter writer;
        if (!host_event_writers.containsKey(host_name)) {
            final int host_index = getHostIndex(host_name);
            writer = new CsvListWriter(new FileWriter(getHostEventCsv(host_index)), CsvPreference.STANDARD_PREFERENCE);
            writer.writeHeader("time", "peer_index", "event_code", "event_params");
            host_event_writers.put(host_name, writer);
        }
        else {
            writer = host_event_writers.get(host_name);
        }
        return writer;
    }

    private File getHostEventCsv(final int host_index) throws IOException {

        final File hostEventHome = getHostEventHome(host_index);
        if (!hostEventHome.isDirectory()) {
            FileUtils.forceMkdir(hostEventHome);
        }
        return new File(hostEventHome, "events.csv");
    }

    private File getHostEventHome(final int host_index) {

        return new File(events_home, String.valueOf(host_index));
    }

    private synchronized int getHostIndex(final String host_name) throws IOException {

        final int index;
        if (!host_indices.containsKey(host_name)) {
            index = next_host_index.incrementAndGet();
            host_indices.put(host_name, index);
            hosts_csv_writer.write(index, host_name);
            hosts_csv_writer.flush();
        }
        else {
            index = host_indices.get(host_name);
        }

        return index;
    }

    private synchronized Integer getLookupTargetIndex(Key target) throws IOException {

        final Integer index;
        if (lookup_targets_index.containsKey(target)) {
            index = lookup_targets_index.get(target);
        }
        else {
            index = next_target_index.getAndIncrement();
            lookup_targets_index.putIfAbsent(target, index);
            lookup_targets_csv_writer.write(index, target);
        }
        return index;
    }
}
