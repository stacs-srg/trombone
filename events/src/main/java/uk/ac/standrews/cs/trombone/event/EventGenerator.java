package uk.ac.standrews.cs.trombone.event;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;
import org.mashti.gauge.Rate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncommons.maths.random.MersenneTwisterRNG;
import uk.ac.standrews.cs.trombone.core.key.Key;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class EventGenerator {

    private static final int PROGRESS_LOG_INTERVAL_MILLIS = 10000;
    private static final Logger LOGGER = LoggerFactory.getLogger(EventGenerator.class);
    private final TreeSet<ParticipantEventIterator> event_iterators;
    private final ConcurrentSkipListSet<Event> events = new ConcurrentSkipListSet<>();
    private final AtomicLong last_persisted_event_time = new AtomicLong();
    private final EventWriter event_writer;
    private final long experiment_duration;
    private final MersenneTwisterRNG random;
    private final Scenario scenario;
    private final Semaphore load_balancer = new Semaphore(1_000, true);
    private Future<Void> event_persistor_task;
    private final Rate event_generation_rate = new Rate();
    private final Rate event_persistence_rate = new Rate();

    public EventGenerator(final Scenario scenario, final Path event_home) throws IOException {

        this(scenario, new CsvEventWriter(event_home));
    }

    public EventGenerator(final Scenario scenario, EventWriter writer) {

        this.scenario = scenario;

        event_iterators = new TreeSet<>();
        event_writer = writer;
        experiment_duration = scenario.getExperimentDurationInNanos();
        random = new MersenneTwisterRNG(scenario.getMasterSeed());
    }

    public synchronized void generate() throws ExecutionException, InterruptedException, IOException {

        init(scenario);

        final ExecutorService executor = Executors.newFixedThreadPool(5);

        try {
            event_writer.write(scenario);
            final Future<Void> event_populator = executor.submit(new EventPopulator());
            final Future<Void> progress_logger = executor.submit(new ProgressLogger());
            event_persistor_task = executor.submit(new EventPersistor(event_populator));

            LOGGER.info("awaiting event object generation...");
            event_populator.get();
            LOGGER.info("awaiting event object persistence...");
            event_persistor_task.get();
            LOGGER.info("finalising...");
            progress_logger.cancel(true);
        }
        finally {
            executor.shutdownNow();
            event_writer.close();
        }
    }

    private void init(final Scenario scenario) {

        final Set<Participant> participants = scenario.getParticipants();
        final long experiment_duration_nanos = scenario.getExperimentDurationInNanos();

        for (final Participant participant : participants) {
            final ParticipantEventIterator event_iterator = new ParticipantEventIterator(participant, experiment_duration_nanos, random);
            event_iterators.add(event_iterator);
        }
    }

    private class EventPopulator implements Callable<Void> {

        private final TreeSet<Event> generated_events = new TreeSet<>();

        @Override
        public Void call() throws Exception {

            try {
                Event max_event = null;
                while (!Thread.currentThread().isInterrupted() && !event_iterators.isEmpty()) {

                    final Iterator<ParticipantEventIterator> iterator = event_iterators.iterator();
                    while (iterator.hasNext()) {

                        ParticipantEventIterator first = iterator.next();

                        if (first.hasNext()) {

                            Event event_time = addEvent(first);

                            if (max_event == null) {
                                max_event = event_time;
                            }
                            else {
                                while (event_time.compareTo(max_event) < 0 && first.hasNext()) {
                                    event_time = addEvent(first);
                                }
                            }
                        }
                        else {
                            iterator.remove();
                        }
                    }

                    final Set<Event> to_persist;
                    if (event_iterators.isEmpty()) {
                        to_persist = generated_events;
                    }
                    else {
                        to_persist = generated_events.headSet(max_event);
                    }

                    events.addAll(to_persist);
                    load_balancer.acquire(to_persist.size());
                    to_persist.clear();
                    max_event = generated_events.isEmpty() ? null : generated_events.last();

                }
                return null;
            }
            catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }

        private Event addEvent(ParticipantEventIterator first) {

            final Event event = first.next();
            event_generation_rate.mark();
            generated_events.add(event);
            return event;
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
                LOGGER.info("Event generation rate {}/s, Event persistence rate {}/s", event_generation_rate.getRateAndReset(), event_persistence_rate.getRateAndReset());

                if (event_persistor_task != null && event_persistor_task.isDone()) {
                    try {
                        event_persistor_task.get();
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }

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

            try {
                while (!Thread.currentThread().isInterrupted() && isThereMoreEventsToPersist()) {
                    final Event event = events.pollFirst();
                    if (event != null) {
                        checkEventPersistenceOrder(event);
                        persist(event);
                        load_balancer.release();
                    }
                }

                if (!events.isEmpty()) {
                    throw new IllegalStateException("there are still events to persist but persistor is stopped");
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
            return null;
        }

        private void checkEventPersistenceOrder(final Event event) {

            if (event.getTimeInNanos() < last_persisted_event_time.get()) {
                events.add(event);
                LOGGER.warn("WTF events are out of order; concurrency error, probably due to bad bad code");
                throw new IllegalStateException("WTF events are out of order; concurrency error, probably due to bad bad code");
            }
        }

        private boolean isThereMoreEventsToPersist() {

            return !events.isEmpty() || !event_populator.isDone();
        }

        private void persist(final Event event) throws IOException {

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
            last_persisted_event_time.set(event.getTimeInNanos());
            event_persistence_rate.mark();
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
