package uk.ac.standrews.cs.trombone.core.integration;

import java.io.IOException;
import uk.ac.standrews.cs.shabdiz.ApplicationDescriptor;
import uk.ac.standrews.cs.shabdiz.host.LocalHost;
import uk.ac.standrews.cs.trombone.core.Peer;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class SingleProcessLocalP2PNetwork extends P2PNetwork {

    private final LocalHost local_host;
    private final SingleProcessPeerManager manager;

    public SingleProcessLocalP2PNetwork(final int size) throws IOException {

        super(size);
        local_host = new LocalHost();
        manager = new SingleProcessPeerManager();
    }

    @Override
    public Peer getPeer(final ApplicationDescriptor descriptor) {

        return manager.getPeer(descriptor);
    }

    @Override
    protected void populate() {

        for (int i = 0; i < getMaxSize(); i++) {
            add(new ApplicationDescriptor(local_host, manager));
        }
    }
}
