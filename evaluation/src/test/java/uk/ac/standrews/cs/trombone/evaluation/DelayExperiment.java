package uk.ac.standrews.cs.trombone.evaluation;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import net.schmizz.sshj.userauth.keyprovider.OpenSSHKeyFile;
import net.schmizz.sshj.userauth.method.AuthPublickey;
import net.schmizz.sshj.userauth.password.PasswordFinder;
import net.schmizz.sshj.userauth.password.Resource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mashti.sina.distribution.statistic.Statistics;
import uk.ac.standrews.cs.shabdiz.ApplicationDescriptor;
import uk.ac.standrews.cs.shabdiz.host.Host;
import uk.ac.standrews.cs.shabdiz.host.SSHHost;
import uk.ac.standrews.cs.shabdiz.job.Worker;
import uk.ac.standrews.cs.shabdiz.job.WorkerNetwork;
import uk.ac.standrews.cs.shabdiz.util.Duration;
import uk.ac.standrews.cs.shabdiz.util.Input;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class DelayExperiment {

    private static AuthPublickey public_key_authenticator;
    private Set<Host> hosts;
    private WorkerNetwork workerNetwork;

    @Before
    public void setUp() throws Exception {

        final OpenSSHKeyFile key_provider = new OpenSSHKeyFile();
        key_provider.init(new File(System.getProperty("user.home") + File.separator + ".ssh", "id_rsa"), new PasswordFinder() {

            @Override
            public char[] reqPassword(final Resource<?> resource) {

                return Input.readPassword("private key passphrase");
            }

            @Override
            public boolean shouldRetry(final Resource<?> resource) {

                return false;
            }
        });
        public_key_authenticator = new AuthPublickey(key_provider);

        System.out.println(System.getProperty("java.io.tmpdir"));

        hosts = new HashSet<>();
        hosts.add(new SSHHost("blub.cs.st-andrews.ac.uk", public_key_authenticator));

        //                        hosts.add(new SSHHost("masih.host.cs.st-andrews.ac.uk", public_key_authenticator));
        //        hosts.add(new LocalHost());

        workerNetwork = new WorkerNetwork(49958);
        workerNetwork.getWorkerManager().setWorkerDeploymentTimeout(new Duration(2, TimeUnit.MINUTES));
        for (Host host : hosts) {
            workerNetwork.add(host);
        }
        workerNetwork.addCurrentJVMClasspath();
        workerNetwork.deployAll();
    }

    @Test
    public void testDelay() throws Exception {

        HashSet<String> hosts_to_ping = new HashSet<>();
        hosts_to_ping.add("blub.cs.st-andrews.ac.uk");
        //        hosts_to_ping.add("compute-0-1.local");
        //        hosts_to_ping.add("compute-0-2.local");
        //        hosts_to_ping.add("compute-0-3.local");

        for (ApplicationDescriptor descriptor : workerNetwork) {

            Worker worker = descriptor.getApplicationReference();
            System.out.println("submitting job...");

            Future<HashSet<Statistics>> submit = null;
            try {
                submit = worker.submit(new RoundTripDelayMeasurementJob(hosts_to_ping, new Duration(1, TimeUnit.SECONDS), 5, new Duration(1, TimeUnit.SECONDS)));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("awaiting result...");
            for (Statistics statistics : submit.get()) {
                System.out.println(" >>> " + statistics.getMean());
            }
        }

        //        final LeanClientFactory<WorkerRemote> proxy_factory = new LeanClientFactory<>(WorkerRemote.class);
        //        final WorkerRemote workerRemote = proxy_factory.get(new InetSocketAddress("138.251.6.157", 35997));
        //        final UUID submit = workerRemote.submit(new RoundTripDelayMeasurementJob(hosts_to_ping, new Duration(1, TimeUnit.SECONDS), 5, new Duration(1, TimeUnit.SECONDS)));
        //        System.out.println(submit);
        //        Thread.sleep(1000);

        //        final Timer timer = new Timer();
        //        System.out.println(InetAddress.getLocalHost().getHostName());
        //        for (int i = 0; i < 30; i++) {
        //            final Timer.Time time = timer.time();
        //            InetAddress.getByName("dyn-195-201.cs.st-andrews.ac.uk").isReachable(5000);
        //            final long duration_nanos = time.stop();
        //            System.out.println(TimeUnit.MICROSECONDS.convert(duration_nanos, TimeUnit.NANOSECONDS));
        //            Thread.sleep(1000);
        //        }
        //
        //        final Statistics stats = timer.getAndReset();
        //        System.out.println(" mean " + TimeUnit.MILLISECONDS.convert(stats.getMean().longValue(), TimeUnit.NANOSECONDS));

    }

    @After
    public void tearDown() throws Exception {

        workerNetwork.shutdown();
    }
}
