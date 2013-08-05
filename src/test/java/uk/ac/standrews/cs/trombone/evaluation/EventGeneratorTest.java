package uk.ac.standrews.cs.trombone.evaluation;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import uk.ac.standrews.cs.shabdiz.util.Duration;
import uk.ac.standrews.cs.trombone.churn.Churn;
import uk.ac.standrews.cs.trombone.churn.ConstantRateUncorrelatedChurn;
import uk.ac.standrews.cs.trombone.evaluation.provider.PortNumberProvider;
import uk.ac.standrews.cs.trombone.key.RandomIntegerKeyProvider;
import uk.ac.standrews.cs.trombone.math.ExponentialDistribution;
import uk.ac.standrews.cs.trombone.math.RandomNumberGenerator;
import uk.ac.standrews.cs.trombone.workload.ConstantRateWorkload;
import uk.ac.standrews.cs.trombone.workload.Workload;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class EventGeneratorTest {

    private static final ExponentialDistribution first_arrival_delay_distribution = ExponentialDistribution.byMean(new Duration(1, TimeUnit.SECONDS));
    private static final ExponentialDistribution session_length_distribution = ExponentialDistribution.byMean(new Duration(5, TimeUnit.SECONDS));
    private static final ExponentialDistribution downtime_distribution = ExponentialDistribution.byMean(new Duration(5, TimeUnit.SECONDS));
    private static final ExponentialDistribution workload_intervals_distribution = ExponentialDistribution.byMean(new Duration(200, TimeUnit.MILLISECONDS));
    private static final Duration experiemnt_duration = new Duration(60, TimeUnit.SECONDS);
    private Scenario scenario;
    private Random uniform_random;

    @Before
    public void setUp() throws Exception {

        scenario = new Scenario("test", 123456, experiemnt_duration) {

            final PortNumberProvider port_provider = new PortNumberProvider(45000);
            final RandomIntegerKeyProvider key_provider = new RandomIntegerKeyProvider(generateSeed());

            public Churn getChurn() {

                final Duration first_arrival_delay = generateFirstArrivalDelay();
                return new ConstantRateUncorrelatedChurn(first_arrival_delay, session_length_distribution, downtime_distribution, generateSeed());
            }

            public Workload getWorkload() {

                return new ConstantRateWorkload(workload_intervals_distribution, new RandomIntegerKeyProvider(generateSeed()), 5, generateSeed());
            }

            Participant newParticipantOnHost(String host) {
                return new Participant(key_provider.get(), new InetSocketAddress(host, 0), getChurn(), getWorkload());
            }
        };
        for (int i = 0; i < 5; i++) {
            scenario.setPeersPerHost("h_" + i, 100);
        }

        uniform_random = new Random(scenario.generateSeed());
    }

    @Test
    public void testGeneration() throws Exception {

        final EventGenerator generator = new EventGenerator(scenario, new File("/Users/masih/Desktop"));
        generator.generate();

    }

    private Duration generateFirstArrivalDelay() {

        return RandomNumberGenerator.generateDurationInNanoseconds(first_arrival_delay_distribution, uniform_random);
    }
}
