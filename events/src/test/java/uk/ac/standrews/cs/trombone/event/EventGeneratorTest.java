package uk.ac.standrews.cs.trombone.event;

import java.io.File;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.inject.Provider;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mashti.sina.distribution.ExponentialDistribution;
import org.mashti.sina.distribution.ProbabilityDistribution;
import uk.ac.standrews.cs.shabdiz.util.Duration;
import uk.ac.standrews.cs.test.category.Ignore;
import uk.ac.standrews.cs.trombone.core.key.Key;
import uk.ac.standrews.cs.trombone.core.key.RandomKeyProvider;
import uk.ac.standrews.cs.trombone.core.key.ZipfKeyProvider;
import uk.ac.standrews.cs.trombone.event.provider.ConstantRateUncorrelatedUniformChurnProvider;
import uk.ac.standrews.cs.trombone.event.provider.ConstantRateWorkloadProvider;
import uk.ac.standrews.cs.trombone.event.provider.RandomSeedProvider;
import uk.ac.standrews.cs.trombone.event.provider.SequentialPortNumberProvider;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
@Category(Ignore.class)
public class EventGeneratorTest {

    private static final ProbabilityDistribution session_length_distribution = new ExponentialDistribution(Double.valueOf(new Duration(100, TimeUnit.SECONDS).getLength(TimeUnit.NANOSECONDS)));
    private static final ProbabilityDistribution downtime_distribution = new ExponentialDistribution(Double.valueOf(new Duration(3, TimeUnit.SECONDS).getLength(TimeUnit.NANOSECONDS)));
    private static final ProbabilityDistribution workload_intervals_distribution = new ExponentialDistribution(Double.valueOf(new Duration(500, TimeUnit.MILLISECONDS).getLength(TimeUnit.NANOSECONDS)));
    private static final Duration experiment_duration = new Duration(1, TimeUnit.MINUTES);
    private Scenario scenario;

    @Before
    public void setUp() throws Exception {

        scenario = new Scenario("test", 89562);
        final Provider<Key> target_key_provider = new ZipfKeyProvider(200, 1, 32, scenario.generateSeed());
        scenario.setPeerKeyProvider(new RandomKeyProvider(scenario.generateSeed(), 32));
        scenario.setChurnProvider(new ConstantRateUncorrelatedUniformChurnProvider(session_length_distribution, downtime_distribution, new RandomSeedProvider(scenario.generateSeed())));
        //        scenario.setChurnProvider(NoChurnProvider.getInstance());
        scenario.setWorkloadProvider(new ConstantRateWorkloadProvider(workload_intervals_distribution, target_key_provider, new RandomSeedProvider(scenario.generateSeed())));
        scenario.setExperimentDuration(experiment_duration);
        scenario.addHost("localhost", 10, new SequentialPortNumberProvider(45000));
    }

    @Test
    public void testGeneration() throws Exception {

        final File events_home = new File("/Users/masih/Desktop", scenario.getName() + ".zip");
        final URI path = URI.create("jar:" + events_home.toURI());
        final Map<String, String> environment = new HashMap<>();
        environment.put("create", "true");
        try (final FileSystem events_file_system = FileSystems.newFileSystem(path, environment)) {
            final EventGenerator generator = new EventGenerator(scenario, events_file_system.getPath("/"));
            generator.generate();
        }

        final FileSystem fileSystem = FileSystems.newFileSystem(URI.create("jar:file:/Users/masih/Desktop/test.zip"), new HashMap<String, String>());

        //        EventExecutor executor = new EventExecutor(fileSystem);
        //        executor.start();
        //        experiment_duration.sleep();
        //        executor.stop();
    }
}
