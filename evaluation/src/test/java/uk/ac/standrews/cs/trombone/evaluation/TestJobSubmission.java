package uk.ac.standrews.cs.trombone.evaluation;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.schmizz.sshj.userauth.method.AuthPublickey;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mashti.jetson.lean.LeanClientFactory;
import uk.ac.standrews.cs.shabdiz.host.Host;
import uk.ac.standrews.cs.shabdiz.integrity.TestJobRemoteFactory;
import uk.ac.standrews.cs.shabdiz.job.WorkerNetwork;
import uk.ac.standrews.cs.shabdiz.job.WorkerRemote;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class TestJobSubmission {

    private static AuthPublickey public_key_authenticator;
    private Set<Host> hosts;
    private WorkerNetwork workerNetwork;

    @Before
    public void setUp() throws Exception {

        System.out.println(System.getProperty("java.io.tmpdir"));

//        workerNetwork = new WorkerNetwork(49958);
    }

    @Test
    public void testDelay() throws Exception {

        HashSet<String> hosts_to_ping = new HashSet<>();
        hosts_to_ping.add("blub.cs.st-andrews.ac.uk");
        //        hosts_to_ping.add("compute-0-1.local");
        //        hosts_to_ping.add("compute-0-2.local");
        //        hosts_to_ping.add("compute-0-3.local");

        final LeanClientFactory<WorkerRemote> proxy_factory = new LeanClientFactory<>(WorkerRemote.class);
//        final WorkerRemote workerRemote = proxy_factory.get(new InetSocketAddress(InetAddress.getByName("blub.cs.st-andrews.ac.uk"), 56407));
        final WorkerRemote workerRemote = proxy_factory.get(new InetSocketAddress(InetAddress.getByName("blub.cs.st-andrews.ac.uk"), 44837));
        final UUID submit = workerRemote.submit(TestJobRemoteFactory.makeEchoJob("SSS"));
//        final UUID submit = workerRemote.submit(new RoundTripDelayMeasurementJob(hosts_to_ping, new Duration(1, TimeUnit.SECONDS), 5, new Duration(1, TimeUnit.SECONDS)));
        System.out.println(submit);
        Thread.sleep(10000);


    }

    @After
    public void tearDown() throws Exception {

        workerNetwork.shutdown();
    }
}
