package uk.ac.standrews.cs.trombone.evaluation;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Provider;
import org.junit.Before;
import org.junit.Test;
import org.mashti.sina.distribution.ExponentialDistribution;
import org.mashti.sina.distribution.ProbabilityDistribution;
import uk.ac.standrews.cs.shabdiz.util.Duration;
import uk.ac.standrews.cs.trombone.churn.Churn;
import uk.ac.standrews.cs.trombone.core.key.Key;
import uk.ac.standrews.cs.trombone.core.key.RandomKeyProvider;
import uk.ac.standrews.cs.trombone.core.key.ZipfKeyProvider;
import uk.ac.standrews.cs.trombone.evaluation.provider.PortNumberProvider;
import uk.ac.standrews.cs.trombone.workload.ConstantRateWorkload;
import uk.ac.standrews.cs.trombone.workload.Workload;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class EventGeneratorTest {

    private static final ProbabilityDistribution session_length_distribution = new ExponentialDistribution(Double.valueOf(new Duration(100, TimeUnit.SECONDS).getLength(TimeUnit.NANOSECONDS)));
    private static final ProbabilityDistribution downtime_distribution = new ExponentialDistribution(Double.valueOf(new Duration(1, TimeUnit.SECONDS).getLength(TimeUnit.NANOSECONDS)));
    private static final ProbabilityDistribution workload_intervals_distribution = new ExponentialDistribution(Double.valueOf(new Duration(500, TimeUnit.MILLISECONDS).getLength(TimeUnit.NANOSECONDS)));
    private static final Duration experiment_duration = new Duration(1, TimeUnit.MINUTES);
    private final AtomicInteger next_port = new AtomicInteger(45000);
    private Scenario scenario;

    @Before
    public void setUp() throws Exception {

        scenario = new Scenario("test", 123456, experiment_duration) {

            final PortNumberProvider port_provider = new PortNumberProvider(45000);
            final Provider<Key> key_provider = new RandomKeyProvider(generateSeed());
                        final Provider<Key> target_key_provider = new ZipfKeyProvider(20000, 1, generateSeed());
//            final Provider<Key> target_key_provider = new RandomKeyProvider(generateSeed());

            public Churn getChurn() {

//                return new ConstantRateUncorrelatedChurn(session_length_distribution, downtime_distribution, generateSeed());
                                                return Churn.NONE;
            }

            public Workload getWorkload() {

                return new ConstantRateWorkload(workload_intervals_distribution, target_key_provider, 5, generateSeed());
            }

            Participant newParticipantOnHost(String host) {

                return new Participant(key_provider.get(), new InetSocketAddress(host, next_port.incrementAndGet()), getChurn(), getWorkload(), null);
            }
        };

        for (int i = 0; i < 1; i++) {
            scenario.setPeersPerHost("localhost", 10);
        }

    }

    @Test
    public void testGeneration() throws Exception {

        final File events_home = new File("/Users/masih/Desktop");
        final long now = System.currentTimeMillis();
        final EventGenerator generator = new EventGenerator(scenario, events_home);
        generator.generate();

        System.out.println("TOOK " + (System.currentTimeMillis() - now) + " ms");

        final FileSystem fileSystem = FileSystems.newFileSystem(URI.create("jar:file:/Users/masih/Desktop/test.zip"), new HashMap<String, String>());
        EventExecutor executor = new EventExecutor(fileSystem);
        executor.start();
        experiment_duration.sleep();
        executor.stop();
    }
}