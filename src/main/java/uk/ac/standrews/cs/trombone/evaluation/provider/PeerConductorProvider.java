package uk.ac.standrews.cs.trombone.evaluation.provider;

import java.io.Serializable;
import javax.inject.Provider;
import uk.ac.standrews.cs.trombone.Peer;
import uk.ac.standrews.cs.trombone.churn.Churn;
import uk.ac.standrews.cs.trombone.evaluation.PeerConductor;
import uk.ac.standrews.cs.trombone.workload.Workload;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class PeerConductorProvider implements Provider<PeerConductor>, Serializable {

    private static final long serialVersionUID = -1967797697738843989L;
    private final SerializableProvider<Peer> peer_provider;
    private final SerializableProvider<Churn> churn_prvider;
    private final SerializableProvider<Workload> workload_prvider;

    public PeerConductorProvider(final SerializableProvider<Peer> peer_provider, final SerializableProvider<Churn> churn_prvider, final SerializableProvider<Workload> workload_prvider) {

        this.peer_provider = peer_provider;
        this.churn_prvider = churn_prvider;
        this.workload_prvider = workload_prvider;
    }

    @Override
    public PeerConductor get() {

        final Peer peer = peer_provider.get();
        final Churn churn = churn_prvider.get();
        final Workload workload = workload_prvider.get();

        return new PeerConductor(peer, churn, workload);
    }
}
