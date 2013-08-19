package uk.ac.standrews.cs.trombone.evaluation;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;
import uk.ac.standrews.cs.shabdiz.util.HashCodeUtil;
import uk.ac.standrews.cs.trombone.PeerConfigurator;
import uk.ac.standrews.cs.trombone.PeerReference;
import uk.ac.standrews.cs.trombone.churn.Churn;
import uk.ac.standrews.cs.trombone.key.Key;
import uk.ac.standrews.cs.trombone.workload.Workload;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class Participant implements Comparable<Participant> {

    private static final AtomicInteger NEXT_ID = new AtomicInteger();
    private final Integer id;
    private final Key key;
    private final InetSocketAddress address;
    private final Churn churn;
    private final Workload workload;
    private final PeerConfigurator configurator;
    private final PeerReference reference;

    public Participant(final Key key, final InetSocketAddress address, final Churn churn, final Workload workload, final PeerConfigurator configurator) {

        id = NEXT_ID.incrementAndGet();
        this.key = key;
        this.address = address;
        this.churn = churn;
        this.workload = workload;
        this.configurator = configurator;
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
}
