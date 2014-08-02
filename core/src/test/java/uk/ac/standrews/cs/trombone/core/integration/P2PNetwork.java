package uk.ac.standrews.cs.trombone.core.integration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import uk.ac.standrews.cs.shabdiz.ApplicationDescriptor;
import uk.ac.standrews.cs.shabdiz.ApplicationNetwork;
import uk.ac.standrews.cs.trombone.core.Peer;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public abstract class P2PNetwork extends ApplicationNetwork {

    private final int size;

    protected P2PNetwork(int size) {

        super("trombone network");
        this.size = size;
    }

    public int getMaxSize() {

        return size;
    }

    public abstract Peer getPeer(ApplicationDescriptor descriptor);

    @Override
    protected ExecutorService createNetworkExecutorService() {

        return Executors.newFixedThreadPool(100);
    }

    protected abstract void populate();
}
