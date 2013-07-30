package uk.ac.standrews.cs.trombone.evaluation;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import javax.inject.Provider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.ac.standrews.cs.shabdiz.host.LocalHost;
import uk.ac.standrews.cs.shabdiz.util.Duration;
import uk.ac.standrews.cs.trombone.Peer;
import uk.ac.standrews.cs.trombone.churn.Churn;
import uk.ac.standrews.cs.trombone.evaluation.job.MultiplePeerConductorJob;
import uk.ac.standrews.cs.trombone.evaluation.membership.MembershipService;
import uk.ac.standrews.cs.trombone.evaluation.membership.RandomMembershipService;
import uk.ac.standrews.cs.trombone.evaluation.provider.ConstantRateWorkloadProvider;
import uk.ac.standrews.cs.trombone.evaluation.provider.NoChurnProvider;
import uk.ac.standrews.cs.trombone.evaluation.provider.PeerProvider;
import uk.ac.standrews.cs.trombone.evaluation.provider.RandomSeedProvider;
import uk.ac.standrews.cs.trombone.evaluation.provider.SerializableProvider;
import uk.ac.standrews.cs.trombone.key.RandomIntegerKeyProvider;
import uk.ac.standrews.cs.trombone.math.ExponentialDistribution;
import uk.ac.standrews.cs.trombone.workload.Workload;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class ExperimentTest {

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testStart() throws Exception {

        //        Experiment experiment = new Experiment("test", new TestScenario());
        //        experiment.call();

        //        WorkerNetwork network = new WorkerNetwork();
        //        final ApplicationDescriptor descriptor = network.add(new LocalHost());
        //        network.deployAll();
        //        network.awaitAnyOfStates(ApplicationState.RUNNING);
        //        final Worker worker = descriptor.getApplicationReference();

        final TestScenario scenario = new TestScenario();
        final MembershipService membershipService = scenario.getMembershipService();
        final MembershipServiceExposer exposer = new MembershipServiceExposer(membershipService);
        exposer.expose();
        final MultiplePeerConductorJob job = new MultiplePeerConductorJob(scenario.getPeerConductorProvider().get(), exposer.getAddress(), new Duration(60, TimeUnit.SECONDS), 10, new Duration(5, TimeUnit.SECONDS));

        job.call();

    }

    public static class TestScenario extends Scenario {

        public static final RandomSeedProvider seed_provider = new RandomSeedProvider(0xffffL);
        public static final Provider<SerializableProvider<Peer>> peer_provider = new Provider<SerializableProvider<Peer>>() {

            @Override
            public SerializableProvider<Peer> get() {

                return new PeerProvider(new RandomIntegerKeyProvider(seed_provider.get()));
            }
        };
        public static final Provider<SerializableProvider<Long>> SEED_PROVIDER = new Provider<SerializableProvider<Long>>() {

            @Override
            public SerializableProvider<Long> get() {

                return new RandomSeedProvider(seed_provider.get());
            }
        };
        public static final Provider<SerializableProvider<Churn>> churn_rpvider = new Provider<SerializableProvider<Churn>>() {

            @Override
            public SerializableProvider<Churn> get() {

                //                return new ConstantRateUncorrelatedUniformChurnProvider(ExponentialDistribution.byMean(new Duration(1, TimeUnit.MILLISECONDS)), ExponentialDistribution.byMean(new Duration(10, TimeUnit.SECONDS)), ExponentialDistribution.byMean(new Duration(5, TimeUnit.SECONDS)), new RandomSeedProvider(seed_provider.get()));
                return NoChurnProvider.getInstance();
            }
        };
        public static final Provider<SerializableProvider<Workload>> workload_provider = new Provider<SerializableProvider<Workload>>() {

            @Override
            public SerializableProvider<Workload> get() {

                return new ConstantRateWorkloadProvider(ExponentialDistribution.byMean(new Duration(200, TimeUnit.MILLISECONDS)), 5, SEED_PROVIDER.get());
            }
        };
        final RandomMembershipService membershipService = new RandomMembershipService(seed_provider.get());

        private TestScenario() throws IOException {

            add(new LocalHost());

        }

        @Override
        MembershipService getMembershipService() {

            return membershipService;
        }

        @Override
        Provider<SerializableProvider<Long>> getSeedProvider() {

            return SEED_PROVIDER;
        }

        @Override
        Provider<SerializableProvider<Churn>> getChurnProvider() {

            return churn_rpvider;
        }

        @Override
        Provider<SerializableProvider<Workload>> getWorkloadProvider() {

            return workload_provider;
        }

        @Override
        Provider<SerializableProvider<Peer>> getPeerProvider() {

            return peer_provider;
        }
    }

}
