package uk.ac.standrews.cs.trombone.evaluation;

import java.io.File;
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
import uk.ac.standrews.cs.trombone.evaluation.provider.ConstantRateWorkloadProvider;
import uk.ac.standrews.cs.trombone.evaluation.provider.NoChurnProvider;
import uk.ac.standrews.cs.trombone.evaluation.provider.PortNumberProvider;
import uk.ac.standrews.cs.trombone.evaluation.provider.RandomSeedProvider;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
@Category(Ignore.class)
public class EventGeneratorTest {

    private static final ProbabilityDistribution session_length_distribution = new ExponentialDistribution(Double.valueOf(new Duration(100, TimeUnit.SECONDS).getLength(TimeUnit.NANOSECONDS)));
    private static final ProbabilityDistribution downtime_distribution = new ExponentialDistribution(Double.valueOf(new Duration(1, TimeUnit.SECONDS).getLength(TimeUnit.NANOSECONDS)));
    private static final ProbabilityDistribution workload_intervals_distribution = new ExponentialDistribution(Double.valueOf(new Duration(500, TimeUnit.MILLISECONDS).getLength(TimeUnit.NANOSECONDS)));
    private static final Duration experiment_duration = new Duration(30, TimeUnit.MINUTES);
    private Scenario scenario;

    @Before
    public void setUp() throws Exception {

        scenario = new Scenario("test", 89562);
        final Provider<Key> target_key_provider = new ZipfKeyProvider(10000, 1, scenario.generateSeed());
        //        final Provider<Key> target_key_provider = new RandomKeyProvider(scenario.generateSeed());
        scenario.setPeerKeyProvider(new RandomKeyProvider(scenario.generateSeed()));
        //        scenario.setChurnProvider(new ConstantRateUncorrelatedUniformChurnProvider(session_length_distribution, downtime_distribution, new RandomSeedProvider(scenario.generateSeed())));
        scenario.setChurnProvider(new NoChurnProvider());
        scenario.setWorkloadProvider(new ConstantRateWorkloadProvider(workload_intervals_distribution, target_key_provider, 5, new RandomSeedProvider(scenario.generateSeed())));
        scenario.setExperimentDuration(experiment_duration);
        scenario.addHost("localhost", 1000, new PortNumberProvider(45000));
    }

    @Test
    public void testGeneration() throws Exception {

        final File events_home = new File("/Users/masih/Desktop");
        final long now = System.currentTimeMillis();
        final EventGenerator generator = new EventGenerator(scenario, events_home);
        generator.generate();

        System.out.println("TOOK " + (System.currentTimeMillis() - now) + " ms");

//                final FileSystem fileSystem = FileSystems.newFileSystem(URI.create("jar:file:/Users/masih/Desktop/test.zip"), new HashMap<String, String>());
        //        EventExecutor executor = new EventExecutor(fileSystem);
        //        executor.start();
        //        experiment_duration.sleep();
        //        executor.stop();
    }
}
