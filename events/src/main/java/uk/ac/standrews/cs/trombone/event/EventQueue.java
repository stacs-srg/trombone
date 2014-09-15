package uk.ac.standrews.cs.trombone.event;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomAdaptor;
import org.mashti.gauge.Rate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.trombone.core.Key;
import uk.ac.standrews.cs.trombone.core.PeerReference;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class EventQueue implements Iterator<Event> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventQueue.class);
    private static final int BUFFERED_EVENTS = 1;
    private static final int MAX_JOIN_KNOWN_PEERS = 5;
    private final LinkedBlockingQueue<Event> events;
    private final Scenario scenario;
    private final int host_index;
    private final Future<Void> generator_task;
    private final Random random;
    private final PriorityQueue<ParticipantEventIterator> event_generators;
    private final Rate event_generation_rate = new Rate();
    private final AtomicLong last_persisted_event_time = new AtomicLong();
    private final ConcurrentSkipListMap<Key, Participant> alive_peers;
    private final Set<Participant> participants;

    public EventQueue(Scenario scenario, int host_index) {

        this(scenario, host_index, new HashMap<Integer, String>());
    }

    public EventQueue(Scenario scenario, int host_index, final Map<Integer, String> substitute_host_indices) {

        this.host_index = host_index;
        this.scenario = new Scenario(scenario);
        this.scenario.substituteHostNames(substitute_host_indices);
        events = new LinkedBlockingQueue<>(BUFFERED_EVENTS);

        random = new RandomAdaptor(new MersenneTwister(this.scenario.getMasterSeed()));
        event_generators = new PriorityQueue<>();
        alive_peers = new ConcurrentSkipListMap<Key, Participant>();
        participants = this.scenario.getParticipants();

        init(this.scenario);

        generator_task = Executors.newSingleThreadExecutor()
                .submit(new Callable<Void>() {

                    @Override
                    public Void call() throws Exception {

                        while (!Thread.currentThread()
                                .isInterrupted() && !event_generators.isEmpty()) {

                            nextEvent();
                        }
                        System.out.println(events.size());
                        return null;
                    }
                });

        while (!generator_task.isDone() && events.remainingCapacity() != 0) {

            try {
                Thread.sleep(1000);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public Set<Participant> getParticipants() {

        return participants;
    }

    private void init(final Scenario scenario) {

        final long experiment_duration_nanos = scenario.getExperimentDurationInNanos();

        for (final Participant participant : participants) {
            final ParticipantEventIterator event_iterator = new ParticipantEventIterator(participant, experiment_duration_nanos, random);
            event_generators.add(event_iterator);
        }
    }

    public Scenario getScenario() {

        //TODO implement unmodifiable version
        return scenario;
    }

    void put(final Event event) throws InterruptedException {

        final int host_index = event.getSource()
                .getHostIndex();
        if (this.host_index == host_index) {
            events.put(event);
        }
    }

    @Override
    public boolean hasNext() {

        return !event_generators.isEmpty() || !events.isEmpty();
    }

    @Override
    public Event next() {

        try {
            return events.take();
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void remove() {

        throw new UnsupportedOperationException("remove is not supported");
    }

    public boolean isAlive(PeerReference reference) {

        return alive_peers.containsKey(reference.getKey());
    }

    private void nextEvent() throws InterruptedException {

        ParticipantEventIterator first = event_generators.poll();
        if (first.hasNext()) {
            final Event event = first.next();
            event_generators.add(first);
            persist(event);
        }
    }

    private void checkEventPersistenceOrder(final Event event) {

        if (event.getTimeInNanos() < last_persisted_event_time.get()) {
            LOGGER.error("WTF events are out of order; concurrency error, probably due to bad bad code");
            throw new IllegalStateException("WTF events are out of order; concurrency error, probably due to bad bad code");
        }
    }

    private void persist(final Event event) throws InterruptedException {

        checkEventPersistenceOrder(event);
        final Participant participant = event.getSource();
        final Key peer_key = participant.getKey();
        if (event instanceof JoinEvent) {

            final JoinEvent join_event = (JoinEvent) event;
            final Set<Participant> known_peers = pickRandomly(MAX_JOIN_KNOWN_PEERS, alive_peers.values());
            final Set<PeerReference> references = known_peers.stream()
                    .map(Participant:: getReference)
                    .collect(Collectors.toSet());
            join_event.setKnownPeerReferences(references);
            alive_peers.put(peer_key, participant);
        }
        else if (event instanceof LeaveEvent) {
            alive_peers.remove(peer_key);
        }
        else {
            final LookupEvent lookupEvent = (LookupEvent) event;
            if (alive_peers.isEmpty()) {
                throw new IllegalStateException("no peer is alive at the given time and a lookup is happening?! something is wrong");
            }

            Map.Entry<Key, Participant> expected_result = alive_peers.ceilingEntry(lookupEvent.getTarget());
            if (expected_result == null) {
                expected_result = alive_peers.firstEntry();
            }
            lookupEvent.setExpectedResult(expected_result.getValue()
                    .getReference());
        }

        put(event);
        last_persisted_event_time.set(event.getTimeInNanos());
        event_generation_rate.mark();
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
