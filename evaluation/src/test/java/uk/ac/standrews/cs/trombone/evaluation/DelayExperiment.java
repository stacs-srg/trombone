package uk.ac.standrews.cs.trombone.evaluation;

import java.io.File;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
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
import uk.ac.standrews.cs.shabdiz.ApplicationDescriptor;
import uk.ac.standrews.cs.shabdiz.host.Host;
import uk.ac.standrews.cs.shabdiz.host.SSHHost;
import uk.ac.standrews.cs.shabdiz.job.Job;
import uk.ac.standrews.cs.shabdiz.job.Worker;
import uk.ac.standrews.cs.shabdiz.job.WorkerNetwork;
import uk.ac.standrews.cs.shabdiz.util.Duration;
import uk.ac.standrews.cs.shabdiz.util.Input;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class DelayExperiment {

    private static final AuthPublickey SSHJ_AUTH;

    static {
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
        SSHJ_AUTH = new AuthPublickey(key_provider);
    }

    private Set<Host> hosts;
    private WorkerNetwork workerNetwork;

    @Before
    public void setUp() throws Exception {

        System.out.println(System.getProperty("java.io.tmpdir"));
        
        hosts = new HashSet<>();
//        hosts.add(new SSHHost("masih.host.cs.st-andrews.ac.uk", SSHJ_AUTH));
        hosts.add(new SSHHost("blub.cs.st-andrews.ac.uk", SSHJ_AUTH));

        //        hosts.add(new LocalHost());

        workerNetwork = new WorkerNetwork();
        workerNetwork.getWorkerManager().setWorkerDeploymentTimeout(new Duration(2, TimeUnit.MINUTES));
        for (Host host : hosts) {
            workerNetwork.add(host);
        }

        workerNetwork.setScanEnabled(false);
        workerNetwork.addCurrentJVMClasspath();
        workerNetwork.deployAll();
    }

    @Test
    public void testDelay() throws Exception {

        for (ApplicationDescriptor descriptor : workerNetwork) {

            Worker worker = descriptor.getApplicationReference();
            System.out.println("submitting job...");
            final Future<Serializable> submit = worker.submit(new aa());
            System.out.println("awaiting result...");
            System.out.println(" >>> " + submit.get());
        }
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

    public static class aa implements Job<Serializable> {

        private static final long serialVersionUID = -2763435033698416932L;

        @Override
        public Serializable call() throws Exception {

            return ManagementFactory.getOperatingSystemMXBean().getName();
        }
    }
}
