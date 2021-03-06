package uk.ac.standrews.cs.trombone.event;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.ac.standrews.cs.shabdiz.util.Duration;
import uk.ac.standrews.cs.trombone.event.environment.RandomKeySupplier;
import uk.ac.standrews.cs.trombone.event.environment.Churn;
import uk.ac.standrews.cs.trombone.event.environment.ConstantIntervalGenerator;
import uk.ac.standrews.cs.trombone.event.environment.ExponentialIntervalGenerator;
import uk.ac.standrews.cs.trombone.event.environment.Workload;
import uk.ac.standrews.cs.trombone.event.util.SequentialPortNumberSupplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EventQueueTest {

    private EventQueue event_queue;
    private Scenario scenario;

    @Before
    public void setUp() throws Exception {

        scenario = new Scenario("test", 123123);
        scenario.setExperimentDuration(new Duration(5, TimeUnit.MINUTES));
        scenario.setLookupRetryCount(5);
        scenario.setObservationInterval(new Duration(10, TimeUnit.SECONDS));
        final RandomKeySupplier peer_key_provider = new RandomKeySupplier(scenario.getMasterSeed());
        scenario.setPeerKeyProvider(peer_key_provider);
        final Churn churn = new Churn(new ExponentialIntervalGenerator(new Duration(500, TimeUnit.MILLISECONDS), 123123), new ExponentialIntervalGenerator(new Duration(500, TimeUnit.MILLISECONDS), 123123));
        final Workload workload = new Workload(peer_key_provider, new ConstantIntervalGenerator(new Duration(500, TimeUnit.MILLISECONDS)));
        scenario.addHost("localhost", 500, new SequentialPortNumberSupplier(45000), churn, workload, null);
        event_queue = new EventQueue(scenario, 1);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testSubstitution() throws Exception {

        final Map<Integer, String> substitutes = new HashMap<>();
        final String substitute_host_name = "AAAAAA";
        substitutes.put(1, substitute_host_name);
        event_queue = new EventQueue(scenario, 1, substitutes);
        assertEquals(substitute_host_name, event_queue.next()
                .getSource()
                .getHostName());
    }

    @Test
    public void testEventOrder() throws Exception {

        long last_event_time = 0;
        while (event_queue.hasNext()) {
            Event next = event_queue.next();
            final Long event_time = next.getTimeInNanos();
            assertTrue(event_time >= last_event_time);
            last_event_time = event_time;
        }
    }
}