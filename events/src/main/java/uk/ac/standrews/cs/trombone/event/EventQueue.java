package uk.ac.standrews.cs.trombone.event;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import org.json.JSONObject;
import uk.ac.standrews.cs.trombone.core.PeerConfiguration;
import uk.ac.standrews.cs.trombone.core.PeerReference;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class EventQueue implements EventReader, EventWriter {

    private final LinkedBlockingQueue<Event> events;
    private final Scenario scenario;
    private final int host_index;
    private EventGenerator event_generator;
    private Future<Void> generator_task;

    public EventQueue(Scenario scenario, int host_index) {

        this(scenario, host_index, new HashMap<Integer, String>());
    }

    public EventQueue(Scenario scenario, int host_index, final Map<Integer, String> substitute_host_indices) {

        this.host_index = host_index;

        this.scenario = scenario.copy();
        this.scenario.substituteHostNames(substitute_host_indices);
        events = new LinkedBlockingQueue<>();
        event_generator = new EventGenerator(scenario, this);
        generator_task = Executors.newSingleThreadExecutor().submit(new Callable<Void>() {

            @Override
            public Void call() throws Exception {

                event_generator.generate();
                return null;
            }
        });
    }

    @Override
    public PeerConfiguration getConfiguration(final PeerReference event_source) {

        return null;
    }

    @Override
    public JSONObject getScenario() {

        return ScenarioJSON.toJSON(scenario);
    }

    @Override
    public void write(final Event event) throws IOException {

        final int host_index = event.getParticipant().getHostIndex();
        if (this.host_index == host_index) {
            try {
                events.put(event);
            }
            catch (InterruptedException e) {
                throw new IOException(e);
            }
        }
    }

    @Override
    public void write(final Scenario scenario) throws IOException {

    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public boolean hasNext() {

        return !generator_task.isDone() || !events.isEmpty();
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
}
