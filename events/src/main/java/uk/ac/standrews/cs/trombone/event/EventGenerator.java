package uk.ac.standrews.cs.trombone.event;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.trombone.core.key.Key;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class EventGenerator {

    private static final int PROGRESS_LOG_INTERVAL_MILLIS = 10000;
    private static final Logger LOGGER = LoggerFactory.getLogger(EventGenerator.class);
    private final TreeSet<ParticipantEventIterator> event_iterators;
    private final TreeSet<Event> events = new TreeSet<>();
    private final AtomicLong last_persisted_event_time = new AtomicLong();
    private final EventWriter event_writer;
    private final Semaphore load_balancer = new Semaphore(5_000, true);
    private final long experiment_duration;
    private final Random random;
    private final Properties properties;
    private final Path event_home;

    public EventGenerator(final Scenario scenario, final Path event_home) throws IOException {

        this.event_home = event_home;

        event_iterators = new TreeSet<>();
        event_writer = new EventWriter(event_home);
        init(scenario);
        experiment_duration = scenario.getExperimentDurationInNanos();
        random = new Random(scenario.generateSeed());
        properties = scenario.getProperties();
    }

    public void generate() throws ExecutionException, InterruptedException, IOException {

        final ExecutorService executor = Executors.newCachedThreadPool();

        try {
            final Future<Void> event_populator = executor.submit(new EventPopulator());
            final Future<Void> progress_logger = executor.submit(new ProgressLogger());
            final Future<Void> event_persistor_task = executor.submit(new EventPersistor(event_populator));

            LOGGER.info("awaiting event object generation...");
            event_populator.get();
            LOGGER.info("awaiting event object persistence...");
            event_persistor_task.get();
            LOGGER.info("finalising...");
            progress_logger.cancel(true);
            persistProperties();
        }
        finally {
            executor.shutdownNow();
            event_writer.close();
        }
    }

    private void persistProperties() throws IOException {

        final Path properties_path = event_home.resolve("scenario.properties");
        try (final BufferedWriter writer = Files.newBufferedWriter(properties_path, StandardCharsets.UTF_8)) {
            properties.store(writer, "");
        }
    }

    private void init(final Scenario scenario) {

        for (final String host : scenario.getHostNames()) {
            for (int i = 0; i < scenario.getMaximumPeersOnHost(host); i++) {
                final Participant participant = scenario.newParticipantOnHost(host);
                final ParticipantEventIterator event_iterator = new ParticipantEventIterator(participant, scenario.getExperimentDurationInNanos());
                event_iterators.add(event_iterator);
            }
        }
    }

    private class EventPopulator implements Callable<Void> {

        @Override
        public Void call() throws Exception {

            while (!Thread.currentThread().isInterrupted() && !event_iterators.isEmpty()) {

                final ParticipantEventIterator first = event_iterators.pollFirst();
                if (first.hasNext()) {
                    load_balancer.acquire();
                    synchronized (events) {
                        events.add(first.next());
                    }
                    event_iterators.add(first);
                }
            }
            return null;
        }
    }

    private class ProgressLogger implements Callable<Void> {

        @Override
        public Void call() throws Exception {

            long last_event_time;
            do {
                Thread.sleep(PROGRESS_LOG_INTERVAL_MILLIS);
                last_event_time = last_persisted_event_time.get();
                LOGGER.info("Generated {}% of events", String.format("%4.1f", 100f * last_event_time / experiment_duration));
            } while (!Thread.currentThread().isInterrupted() && last_event_time < experiment_duration);

            return null;
        }
    }

    private class EventPersistor implements Callable<Void> {

        private final ConcurrentSkipListMap<Key, Participant> alive_peers;
        private final Future<Void> event_populator;

        EventPersistor(Future<Void> event_populator) {

            this.event_populator = event_populator;
            alive_peers = new ConcurrentSkipListMap<Key, Participant>();
        }

        @Override
        public Void call() throws Exception {

            while (!Thread.currentThread().isInterrupted() && !(events.isEmpty() && event_populator.isDone())) {
                final Event event;
                synchronized (events) {
                    event = events.pollFirst();
                }

                if (event == null) {
                    continue;
                }

                final Long event_time = event.getTimeInNanos();

                //Sanity check
                if (event_time < last_persisted_event_time.get()) {
                    throw new IllegalStateException("WTF events are out of order; concurrency error, probably due to bad bad code");
                }

                last_persisted_event_time.set(event_time);

                final Participant participant = event.getParticipant();
                final Key peer_key = participant.getKey();
                if (event instanceof JoinEvent) {

                    final JoinEvent join_event = (JoinEvent) event;
                    final Set<Participant> known_peers = pickRandomly(5, alive_peers.values());
                    join_event.setKnownPeers(known_peers);
                    alive_peers.put(peer_key, participant);
                }
                else if (event instanceof LeaveEvent) {
                    alive_peers.remove(peer_key);
                }
                else {
                    final LookupEvent lookupEvent = (LookupEvent) event;
                    if (alive_peers.isEmpty()) { throw new IllegalStateException("no peer is alive at the given time and a lookup is happening?! something is wrong"); }

                    Map.Entry<Key, Participant> expected_result = alive_peers.ceilingEntry(lookupEvent.getTarget());
                    if (expected_result == null) {
                        expected_result = alive_peers.firstEntry();
                    }
                    lookupEvent.setExpectedResult(expected_result.getValue());
                }

                event_writer.write(event);
                load_balancer.release();
            }

            return null;
        }

        private Set<Participant> pickRandomly(final int count, final Collection<Participant> values) {

            final Set<Participant> chosen_participants = new HashSet<>();
            final int values_size = values.size();
            if (values_size <= count) {
                chosen_participants.addAll(values);
            }
            else {
                final Set<Integer> candidate_indices = new HashSet<>();
                while (candidate_indices.size() < count) {
                    candidate_indices.add(random.nextInt(values_size));
                }

                int index = 0;
                for (Participant participant : values) {
                    if (candidate_indices.contains(index)) {
                        chosen_participants.add(participant);
                    }
                    index++;
                }
            }
            return chosen_participants;
        }
    }
}
