package uk.ac.standrews.cs.trombone.evaluation.event;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Provider;
import uk.ac.standrews.cs.shabdiz.host.Host;
import uk.ac.standrews.cs.trombone.PeerReference;
import uk.ac.standrews.cs.trombone.key.Key;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class PeerReferenceGenerator {

    Provider<Key> peer_key_provider;
    Provider<Integer> port_number_provider;
    Provider<Host> host_provider;

    Set<PeerReference> generateEvenlyDistributedAccross(Set<Host> hosts, int count) {

        Set<PeerReference> references = new HashSet<PeerReference>();

        for (int i = 0; i < count; i++) {
            for (final Host host : hosts) {

                final InetAddress address = host.getAddress();
                final PeerReference reference = new PeerReference(peer_key_provider.get(), new InetSocketAddress(address, port_number_provider.get()));
                references.add(reference);
            }
        }
        return references;
    }

    PeerReference generateByAddress(InetAddress address) {

        return new PeerReference(peer_key_provider.get(), new InetSocketAddress(address, port_number_provider.get()));
    }
}
