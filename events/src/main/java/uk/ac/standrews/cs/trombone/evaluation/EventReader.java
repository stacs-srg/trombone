package uk.ac.standrews.cs.trombone.evaluation;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import org.apache.commons.codec.DecoderException;
import org.mashti.jetson.util.CloseableUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.io.CsvListReader;
import org.supercsv.prefs.CsvPreference;
import uk.ac.standrews.cs.trombone.core.PeerReference;
import uk.ac.standrews.cs.trombone.core.key.Key;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class EventReader implements Closeable, Iterator<Event> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventReader.class);
    private static final boolean DEFAULT_SKIP_FIRST_ROW = true;
    private static final Pattern LOOKUP_EVENT_PARAM_PATTERN = Pattern.compile(":");
    private final CsvListReader event_reader;
    private final Map<Integer, Key> lookup_targets_index;
    private final Map<Integer, PeerReference> peers_index;
    private final AtomicReference<List<String>> next_row_reference;

    public EventReader(FileSystem events_home, int index) throws IOException, DecoderException {

        this(events_home, index, DEFAULT_SKIP_FIRST_ROW);
    }

    public EventReader(FileSystem events_home, int index, boolean skip_first_row) throws IOException, DecoderException {

        event_reader = new CsvListReader(Files.newBufferedReader(events_home.getPath(String.valueOf(index), "events.csv"), StandardCharsets.UTF_8), CsvPreference.STANDARD_PREFERENCE);
        lookup_targets_index = readLookupTargets(Files.newBufferedReader(events_home.getPath("lookup_targets.csv"), StandardCharsets.UTF_8));
        peers_index = readPeers(Files.newBufferedReader(events_home.getPath("peers.csv"), StandardCharsets.UTF_8));
        next_row_reference = new AtomicReference<>();

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

        final List<String> next_row = next_row_reference.getAndSet(null);
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

    PeerReference getPeerReferenceByIndex(final Integer peer_id) {

        return peers_index.get(peer_id);
    }

    private static Map<Integer, PeerReference> readPeers(final Reader peers_csv) throws IOException, DecoderException {

        final Map<Integer, PeerReference> peers = new HashMap<Integer, PeerReference>();
        CsvListReader reader = null;

        try {
            reader = new CsvListReader(peers_csv, CsvPreference.STANDARD_PREFERENCE);
            reader.getHeader(true);  //skip header
            List<String> row = reader.read();
            do {
                final Integer index = Integer.valueOf(row.get(0));
                final Key key = Key.valueOf(row.get(1));
                final InetSocketAddress address = new InetSocketAddress(row.get(2), Integer.valueOf(row.get(3)));
                peers.put(index, new PeerReference(key, address));
                row = reader.read();
            } while (row != null);
            return peers;
        }
        finally {
            CloseableUtil.closeQuietly(reader);
        }
    }

    private static Map<Integer, Key> readLookupTargets(final Reader lookup_targets_csv) throws IOException, DecoderException {

        final Map<Integer, Key> lookup_targets = new HashMap<Integer, Key>();
        CsvListReader reader = null;
        try {
            reader = new CsvListReader(lookup_targets_csv, CsvPreference.STANDARD_PREFERENCE);
            reader.getHeader(true); //skip header
            List<String> row = reader.read();
            do {
                final Integer index = Integer.valueOf(row.get(0));
                final Key key = Key.valueOf(row.get(1));
                lookup_targets.put(index, key);
                row = reader.read();
            } while (row != null);
            return lookup_targets;
        }
        finally {
            CloseableUtil.closeQuietly(reader);
        }
    }

    private Event decode(List<String> next_row) {

        assert next_row != null;

        final long time = Long.valueOf(next_row.get(0));
        final Integer peer_id = Integer.valueOf(next_row.get(1));
        final PeerReference peer = getPeerReferenceByIndex(peer_id);
        final int code = Integer.valueOf(next_row.get(2));
        switch (code) {
            case ChurnEvent.UNAVAILABLE_CODE:
                return new ChurnEvent(peer, peer_id, time, false);

            case ChurnEvent.AVAILABLE_CODE:
                final ChurnEvent event = new ChurnEvent(peer, peer_id, time, true);
                event.setDurationInNanos(Long.valueOf(next_row.get(3)));
                return event;

            case LookupEvent.LOOKUP_EVENT_CODE:
                final String[] params = LOOKUP_EVENT_PARAM_PATTERN.split(next_row.get(3));
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

    private void skipFirstRow() throws IOException {

        event_reader.getHeader(true);
    }

    private boolean setNextRow() {

        List<String> next_row;
        try {
            next_row = event_reader.read();
        }
        catch (final IOException e) {
            next_row = null;
            LOGGER.error("failure occurred while reading next row from event CSV file", e);
        }
        next_row_reference.set(next_row);
        return next_row != null;
    }
}
