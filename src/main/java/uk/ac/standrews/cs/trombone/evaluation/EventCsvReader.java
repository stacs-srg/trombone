package uk.ac.standrews.cs.trombone.evaluation;

import au.com.bytecode.opencsv.CSVReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;
import org.mashti.jetson.util.CloseableUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.trombone.PeerReference;
import uk.ac.standrews.cs.trombone.key.IntegerKey;
import uk.ac.standrews.cs.trombone.key.Key;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class EventCsvReader implements Closeable, Iterator<Event> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventCsvReader.class);
    private static final boolean DEFAULT_SKIP_FIRST_ROW = true;
    private final CSVReader event_reader;
    private final Map<Integer, Key> lookup_targets_index;
    private final Map<Integer, PeerReference> peers_index;
    private final AtomicReference<String[]> next_row_reference;

    public EventCsvReader(File peers_csv, File events_csv, File lookup_targets_csv) throws IOException {
        this(peers_csv, events_csv, lookup_targets_csv, DEFAULT_SKIP_FIRST_ROW);
    }

    public EventCsvReader(File peers_csv, File events_csv, File lookup_targets_csv, boolean skip_first_row) throws IOException {

        event_reader = new CSVReader(new FileReader(events_csv));
        lookup_targets_index = readLookupTargets(lookup_targets_csv);
        peers_index = readPeers(peers_csv);
        next_row_reference = new AtomicReference<String[]>();

        if (skip_first_row) {
            skipFirstRow();
        }
    }

    @Override
    public synchronized boolean hasNext() {

        return setNextRow();
    }

    @Override
    public synchronized Event next() {

        final String[] next_row = next_row_reference.getAndSet(null);
        if (next_row != null) {
            return decode(next_row);
        }
        throw new NoSuchElementException();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() throws IOException {

        event_reader.close();
    }

    private static Map<Integer, PeerReference> readPeers(final File peers_csv) throws IOException {

        final Map<Integer, PeerReference> peers = new HashMap<Integer, PeerReference>();
        CSVReader reader = null;

        try {
            reader = new CSVReader(new FileReader(peers_csv));
            reader.readNext();  //skip header
            String[] row = reader.readNext();
            do {
                final Integer index = Integer.valueOf(row[0]);
                final Key key = new IntegerKey(Integer.valueOf(row[1]));
                final InetSocketAddress address = new InetSocketAddress(row[2], Integer.valueOf(row[3]));
                peers.put(index, new PeerReference(key, address));
                row = reader.readNext();
            } while (row != null);
            return peers;
        }
        finally {
            CloseableUtil.closeQuietly(reader);
        }
    }

    private static Map<Integer, Key> readLookupTargets(final File lookup_targets_csv) throws IOException {

        final Map<Integer, Key> lookup_targets = new HashMap<Integer, Key>();
        CSVReader reader = null;
        try {
            reader = new CSVReader(new FileReader(lookup_targets_csv));
            reader.readNext();  //skip header
            String[] row = reader.readNext();
            do {
                final Integer index = Integer.valueOf(row[0]);
                final Key key = new IntegerKey(Integer.valueOf(row[1]));
                lookup_targets.put(index, key);
                row = reader.readNext();
            } while (row != null);
            return lookup_targets;
        }
        finally {
            CloseableUtil.closeQuietly(reader);
        }
    }

    private Event decode(String[] next_row) {

        assert next_row != null;

        final long time = Long.valueOf(next_row[0]);
        final Integer peer_id = Integer.valueOf(next_row[1]);
        final PeerReference peer = getPeerReferenceByIndex(peer_id);
        final int code = Integer.valueOf(next_row[2]);
        switch (code) {
            case ChurnEvent.UNAVAILABLE_CODE:
                return new ChurnEvent(peer, peer_id, time, false);

            case ChurnEvent.AVAILABLE_CODE:
                final ChurnEvent event = new ChurnEvent(peer, peer_id, time, true);
                event.setDurationInNanos(Long.valueOf(next_row[3]));
                return event;

            case LookupEvent.LOOKUP_EVENT_CODE:
                final String[] params = next_row[3].split(":");
                final Key target = getLookupTargetByIndex(Integer.valueOf(params[0]));
                final Integer expected_result_id = Integer.valueOf(params[1]);
                final PeerReference expected_result = getPeerReferenceByIndex(expected_result_id);
                final LookupEvent lookup_event = new LookupEvent(peer, peer_id, time, target);
                lookup_event.setExpectedResult(expected_result, expected_result_id);
                return lookup_event;

            default:
                throw new IllegalArgumentException("unknown event code " + code);
        }
    }

    private Key getLookupTargetByIndex(final Integer index) {
        return lookup_targets_index.get(index);
    }

    PeerReference getPeerReferenceByIndex(final Integer peer_id) {
        return peers_index.get(peer_id);
    }

    private void skipFirstRow() throws IOException {
        event_reader.readNext();
    }

    private boolean setNextRow() {

        String[] next_row;
        try {
            next_row = event_reader.readNext();
        }
        catch (final IOException e) {
            next_row = null;
            LOGGER.error("failure occured while reading next row from event CSV file", e);
        }
        next_row_reference.set(next_row);
        return next_row != null;
    }
}
