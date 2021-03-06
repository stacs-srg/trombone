package uk.ac.standrews.cs.trombone.evaluation.util;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;
import net.schmizz.sshj.userauth.keyprovider.OpenSSHKeyFile;
import net.schmizz.sshj.userauth.method.AuthMethod;
import net.schmizz.sshj.userauth.method.AuthPublickey;
import uk.ac.standrews.cs.shabdiz.host.Host;
import uk.ac.standrews.cs.shabdiz.host.SSHHost;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public final class BlubCluster {

    private static final Collection<String> BLUB_HOST_NAMES;
    private static final int BLUB_CLUSTER_SIZE = 48;
    private static final String BLUB_HOST_NAME_FORMAT = "compute-0-%d.local";

    static final OpenSSHKeyFile SSH_KEY_FILE;

    static {

        SSH_KEY_FILE = new OpenSSHKeyFile();
        SSH_KEY_FILE.init(new File(System.getProperty("user.home") + File.separator + ".ssh", "id_rsa"));

        final SortedSet<String> blub_host_names = new TreeSet<>();
        for (int index = 0; index < BLUB_CLUSTER_SIZE; index++) {
            blub_host_names.add(String.format(BLUB_HOST_NAME_FORMAT, index));
        }

        BLUB_HOST_NAMES = Collections.unmodifiableSortedSet(blub_host_names);
    }

    private BlubCluster() {

    }

    public static Collection<String> getNodeNames() {

        return BLUB_HOST_NAMES;
    }

    public static int size() {

        return BLUB_CLUSTER_SIZE;
    }

    public static AuthMethod getAuthMethod() {

        // AuthPublicKey is not thread-safe. To avoid concurrency issues, we make a new instance with a static key provider
        return new AuthPublickey(SSH_KEY_FILE);
    }

    public static Host[] getHosts() throws IOException {

        final Host[] hosts = new Host[BLUB_HOST_NAMES.size()];
        int index = 0;
        for (String host_name : BLUB_HOST_NAMES) {
            hosts[index] = new SSHHost(host_name, getAuthMethod());
            index++;
        }

        return hosts;
    }
}
