package uk.ac.standrews.cs.trombone.recovery;

import java.io.IOException;
import uk.ac.standrews.cs.shabdiz.ApplicationDescriptor;
import uk.ac.standrews.cs.shabdiz.ApplicationManager;
import uk.ac.standrews.cs.shabdiz.host.LocalHost;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class SingleProcessLocalP2PNetwork extends P2PNetwork {

    private final LocalHost local_host;
    private final ApplicationManager manager;

    public SingleProcessLocalP2PNetwork(final int size) throws IOException {

        super(size);
        local_host = new LocalHost();
        manager = new SingleProcessPeerManager();
    }

    @Override
    protected void populate() {

        for (int i = 0; i < getSize(); i++) {
            add(new ApplicationDescriptor(local_host, manager));
        }
    }
}
