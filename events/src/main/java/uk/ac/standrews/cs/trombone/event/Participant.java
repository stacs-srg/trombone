package uk.ac.standrews.cs.trombone.event;

import java.net.InetSocketAddress;
import uk.ac.standrews.cs.shabdiz.util.HashCodeUtil;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerConfiguration;
import uk.ac.standrews.cs.trombone.core.PeerFactory;
import uk.ac.standrews.cs.trombone.core.PeerReference;
import uk.ac.standrews.cs.trombone.core.Key;
import uk.ac.standrews.cs.trombone.event.environment.Churn;
import uk.ac.standrews.cs.trombone.event.environment.Workload;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class Participant implements Comparable<Participant> {

    private final Integer id;
    private final Key key;
    private final InetSocketAddress address;
    private final Churn churn;
    private final Workload workload;
    private final PeerConfiguration configuration;
    private final PeerReference reference;
    private int host_index;

    private Peer peer;

    public Participant(int id, final Key key, final InetSocketAddress address, final Churn churn, final Workload workload, final PeerConfiguration configuration) {

        this.id = id;
        this.key = key;
        this.address = address;
        this.churn = churn;
        this.workload = workload;
        this.configuration = configuration;
        reference = new PeerReference(key, address);
    }

    public Integer getId() {

        return id;
    }

    public Key getKey() {

        return key;
    }

    public InetSocketAddress getAddress() {

        return address;
    }

    public String getHostName() {

        return address.getHostName();
    }

    public int getPort() {

        return address.getPort();
    }

    public Churn getChurn() {

        return churn;
    }

    public Workload getWorkload() {

        return workload;
    }

    public PeerReference getReference() {

        return reference;
    }

    @Override
    public int compareTo(final Participant other) {

        return id.compareTo(other.id);
    }

    @Override
    public int hashCode() {

        return HashCodeUtil.generate(id.hashCode());
    }

    @Override
    public boolean equals(final Object other) {

        if (this == other) { return true; }
        if (!(other instanceof Participant)) { return false; }
        final Participant that = (Participant) other;
        return id.equals(that.id);
    }

    @Override
    public String toString() {

        return "Participant{" + "id=" + id + ", key=" + key + ", address=" + address + '}';
    }

    public PeerConfiguration getPeerConfiguration() {

        return configuration;
    }

    public int getHostIndex() {

        return host_index;
    }

    public void setHostIndex(final int host_index) {

        this.host_index = host_index;
    }

    public synchronized Peer getPeer() {

        if (peer == null) {
            peer = PeerFactory.createPeer(reference, configuration);
        }
        return peer;
    }
}
