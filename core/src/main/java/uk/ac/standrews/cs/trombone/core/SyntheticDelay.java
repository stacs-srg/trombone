package uk.ac.standrews.cs.trombone.core;

import java.net.InetAddress;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public interface SyntheticDelay {

    void apply(InetAddress from, InetAddress to) throws InterruptedException;
}
