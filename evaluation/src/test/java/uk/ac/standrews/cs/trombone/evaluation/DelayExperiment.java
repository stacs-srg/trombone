package uk.ac.standrews.cs.trombone.evaluation;

import java.io.File;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import net.schmizz.sshj.userauth.keyprovider.OpenSSHKeyFile;
import net.schmizz.sshj.userauth.method.AuthPublickey;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mashti.sina.distribution.statistic.Statistics;
import uk.ac.standrews.cs.shabdiz.ApplicationDescriptor;
import uk.ac.standrews.cs.shabdiz.ApplicationState;
import uk.ac.standrews.cs.shabdiz.host.Host;
import uk.ac.standrews.cs.shabdiz.host.SSHHost;
import uk.ac.standrews.cs.shabdiz.job.Worker;
import uk.ac.standrews.cs.shabdiz.job.WorkerNetwork;
import uk.ac.standrews.cs.shabdiz.util.Duration;
import uk.ac.standrews.cs.trombone.evaluation.util.RoundTripDelaySamplingJob;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class DelayExperiment {

    private static AuthPublickey public_key_authenticator;
    private Set<Host> hosts;
    private WorkerNetwork network;

    @Before
    public void setUp() throws Exception {

        final OpenSSHKeyFile key_provider = new OpenSSHKeyFile();
        key_provider.init(new File(System.getProperty("user.home") + File.separator + ".ssh", "id_rsa"));
        public_key_authenticator = new AuthPublickey(key_provider);
        hosts = new HashSet<>();
        final SSHHost blub = new SSHHost("blub.cs.st-andrews.ac.uk", public_key_authenticator);
        hosts.add(blub);
        network = new WorkerNetwork();
        network.addCurrentJVMClasspath();
        network.getWorkerManager().setWorkerDeploymentTimeout(new Duration(2, TimeUnit.MINUTES));
        for (Host host : hosts) {
            network.add(host);
        }
        network.deployAll();
        network.awaitAnyOfStates(ApplicationState.RUNNING);
    }

    @Test
    public void testDelay() throws Exception {

        HashSet<String> hosts_to_ping = new HashSet<>();
        hosts_to_ping.add("blub.cs.st-andrews.ac.uk");
        //        hosts_to_ping.add("compute-0-1.local");
        //        hosts_to_ping.add("compute-0-2.local");
        //        hosts_to_ping.add("compute-0-3.local");

        for (ApplicationDescriptor descriptor : network) {

            Worker worker = descriptor.getApplicationReference();
            System.out.println("submitting job...");

            Future<HashMap<InetAddress, Statistics>> submit = worker.submit(new RoundTripDelaySamplingJob(hosts_to_ping, new Duration(1, TimeUnit.SECONDS), 5, new Duration(1, TimeUnit.SECONDS)));
            System.out.println("awaiting result...");
            for (Map.Entry<InetAddress, Statistics> statistics : submit.get().entrySet()) {
                System.out.println(" >>> " + statistics.getValue().getMean());
            }
        }
    }

    @After
    public void tearDown() throws Exception {

        network.shutdown();
    }
}
