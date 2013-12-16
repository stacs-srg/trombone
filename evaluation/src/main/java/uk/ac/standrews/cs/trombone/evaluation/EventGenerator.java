package uk.ac.standrews.cs.trombone.evaluation;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.io.FileUtils;
import uk.ac.standrews.cs.trombone.core.key.Key;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class EventGenerator {

    private final Scenario scenario;
    private final File event_home;
    private final TreeSet<ParticipantEventIterator> event_iterators;
    private final ConcurrentSkipListSet<Event> events = new ConcurrentSkipListSet<Event>();
    private final AtomicLong min_event_time = new AtomicLong();
    private final EventCsvWriter event_csv_writer;
    private final ExecutorService executor;
    private Future<?> event_generator_task;

    public EventGenerator(final Scenario scenario, final File event_home) throws IOException {

        this.scenario = scenario;
        this.event_home = event_home;
        event_iterators = new TreeSet<>();
        final File events_home = new File(event_home, scenario.getName());
        FileUtils.forceMkdir(events_home);
        event_csv_writer = new EventCsvWriter(events_home);
        init(scenario);
        executor = Executors.newCachedThreadPool();
    }

    public void generate() throws ExecutionException, InterruptedException, IOException {

        event_generator_task = executor.submit(new Runnable() {

            @Override
            public void run() {

                while (!Thread.currentThread().isInterrupted() && !event_iterators.isEmpty()) {

                    final ParticipantEventIterator first = event_iterators.pollFirst();
                    if (first.hasNext()) {
                        min_event_time.set(first.getCurrentTime());
                        events.add(first.next());
                        event_iterators.add(first);
                    }
                }
                min_event_time.set(Long.MAX_VALUE);
                System.out.println("done");
            }
        });

        final Future<Void> event_persistor_task = executor.submit(new EventPersistor());

        event_generator_task.get();
        event_persistor_task.get();
        event_csv_writer.close();
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

    class EventPersistor implements Callable<Void> {

        private final ConcurrentSkipListMap<Key, Participant> alive_peers;
        private final AtomicLong oracle_change_time;

        EventPersistor() {

            alive_peers = new ConcurrentSkipListMap<Key, Participant>();
            oracle_change_time = new AtomicLong();
        }

        @Override
        public Void call() throws Exception {

            while (!Thread.currentThread().isInterrupted() && !(events.isEmpty() && event_generator_task.isDone())) {

                final Event event = events.pollFirst();
                if (event == null) {
                    continue;
                }
                if (event.getTimeInNanos() <= min_event_time.get()) {

                    final Participant participant = event.getParticipant();
                    final Long event_time = event.getTimeInNanos();

                    if (event instanceof ChurnEvent) {
                        ChurnEvent churnEvent = (ChurnEvent) event;
                        final Key peer_key = participant.getKey();

                        final long old_time = oracle_change_time.getAndSet(event_time);
                        if (event_time != old_time) {
                            event_csv_writer.write(old_time, alive_peers.values());
                        }

                        if (churnEvent.isAvailable()) {
                            alive_peers.put(peer_key, participant);
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

                    event_csv_writer.write(event);
                }
                else {
                    events.add(event);
                    Thread.sleep(500);
                }
            }

            event_csv_writer.write(oracle_change_time.get(), alive_peers.values());

            System.out.println("done writing " + alive_peers.size() + " " + oracle_change_time.get());
            return null;
        }
    }
}
