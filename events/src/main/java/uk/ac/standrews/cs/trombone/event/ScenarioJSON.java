package uk.ac.standrews.cs.trombone.event;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONObject;
import uk.ac.standrews.cs.shabdiz.util.Duration;
import uk.ac.standrews.cs.trombone.core.DisseminationStrategy;
import uk.ac.standrews.cs.trombone.core.Maintenance;
import uk.ac.standrews.cs.trombone.core.PeerConfiguration;
import uk.ac.standrews.cs.trombone.core.SyntheticDelay;
import uk.ac.standrews.cs.trombone.core.key.KeyProvider;
import uk.ac.standrews.cs.trombone.core.key.ZipfKeyProvider;
import uk.ac.standrews.cs.trombone.core.util.NamingUtils;
import uk.ac.standrews.cs.trombone.event.churn.Churn;
import uk.ac.standrews.cs.trombone.event.churn.FixedExponentialInterval;
import uk.ac.standrews.cs.trombone.event.churn.Workload;
import uk.ac.standrews.cs.trombone.event.provider.SequentialPortNumberProvider;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class ScenarioJSON {

    public static JSONObject toJSON(Scenario scenario) {

        return new JSONObject(scenario);
    }

    public static void main(String[] args) {

        final byte[] seed = DigestUtils.md5("masih");
        final Scenario sss = new Scenario("sss", seed);
        sss.setExperimentDuration(new Duration(10, TimeUnit.MINUTES));
        sss.setLookupRetryCount(5);
        sss.setObservationInterval(new Duration(5, TimeUnit.MINUTES));
        final KeyProvider peer_key_provider1 = new ZipfKeyProvider(100, 1, 16, seed);
        sss.setPeerKeyProvider(peer_key_provider1);
        final FixedExponentialInterval session_lengths = new FixedExponentialInterval(new Duration(10, TimeUnit.SECONDS), seed);
        sss.addHost("localhost", 50, new SequentialPortNumberProvider(4500), new Churn(session_lengths, session_lengths), new Workload(peer_key_provider1, session_lengths), new PeerConfiguration(new Maintenance(new DisseminationStrategy()), new AAA()));
        //        sss.addHost("aaaa", 50, new SequentialPortNumberProvider(4500), new Churn(session_lengths, session_lengths), new Workload(peer_key_provider1, session_lengths), null);

        System.out.println(ScenarioJSON.toJSON(sss).toString(3));
    }

    public static class AAA implements SyntheticDelay {

        private static final long serialVersionUID = 1443894556270333178L;

        @Override
        public void apply(final InetAddress from, final InetAddress to) {

        }

        @Override
        public String getName() {

            return NamingUtils.name(this);
        }
    }
}
