package uk.ac.standrews.cs.trombone.core;

import java.io.IOException;
import uk.ac.standrews.cs.shabdiz.ApplicationNetwork;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public abstract class P2PNetwork extends ApplicationNetwork {

    private final int size;

    protected P2PNetwork(int size) throws IOException {

        super("trombone network");
        this.size = size;
    }

    protected int getSize() {

        return size;
    }

    protected abstract void populate();
}
