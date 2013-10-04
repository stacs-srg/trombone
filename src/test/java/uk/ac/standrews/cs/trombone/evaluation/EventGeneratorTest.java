package uk.ac.standrews.cs.trombone.evaluation;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Provider;
import org.junit.Before;
import org.junit.Test;
import org.mashti.sina.distribution.ExponentialDistribution;
import uk.ac.standrews.cs.shabdiz.util.Duration;
import uk.ac.standrews.cs.trombone.churn.Churn;
import uk.ac.standrews.cs.trombone.churn.ConstantRateUncorrelatedChurn;
import uk.ac.standrews.cs.trombone.evaluation.provider.PortNumberProvider;
import uk.ac.standrews.cs.trombone.key.Key;
import uk.ac.standrews.cs.trombone.key.RandomIntegerKeyProvider;
import uk.ac.standrews.cs.trombone.workload.ConstantRateWorkload;
import uk.ac.standrews.cs.trombone.workload.Workload;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class EventGeneratorTest {

    private static final ExponentialDistribution session_length_distribution = ExponentialDistribution.byMean(Double.valueOf(new Duration(10, TimeUnit.SECONDS).getLength(TimeUnit.NANOSECONDS)));
    private static final ExponentialDistribution downtime_distribution = ExponentialDistribution.byMean(Double.valueOf(new Duration(10, TimeUnit.SECONDS).getLength(TimeUnit.NANOSECONDS)));
    private static final ExponentialDistribution workload_intervals_distribution = ExponentialDistribution.byMean(Double.valueOf(new Duration(500, TimeUnit.MILLISECONDS).getLength(TimeUnit.NANOSECONDS)));
    private static final Duration experiemnt_duration = new Duration(20, TimeUnit.MINUTES);
    private final AtomicInteger next_port = new AtomicInteger(45000);
    private Scenario scenario;

    @Before
    public void setUp() throws Exception {

        scenario = new Scenario("test", 123456, experiemnt_duration) {

            final PortNumberProvider port_provider = new PortNumberProvider(45000);
            final Provider<Key> key_provider = new RandomIntegerKeyProvider(generateSeed());
            //            final Provider<Key> target_key_provider = new ZipfIntegerKeyProvider(20000, 1, generateSeed());
            final Provider<Key> target_key_provider = new RandomIntegerKeyProvider(generateSeed());

            public Churn getChurn() {

                return new ConstantRateUncorrelatedChurn(session_length_distribution, downtime_distribution, generateSeed());
                //                return Churn.NONE;
            }

            public Workload getWorkload() {

                return new ConstantRateWorkload(workload_intervals_distribution, target_key_provider, 5, generateSeed());
            }

            Participant newParticipantOnHost(String host) {

                return new Participant(key_provider.get(), new InetSocketAddress(host, next_port.incrementAndGet()), getChurn(), getWorkload(), null);
            }
        };

        for (int i = 0; i < 1; i++) {
            scenario.setPeersPerHost("localhost", 100);
        }

    }

    @Test
    public void testGeneration() throws Exception {

        final EventGenerator generator = new EventGenerator(scenario, new File("/Users/masih/Desktop"));
        generator.generate();
    }
}
