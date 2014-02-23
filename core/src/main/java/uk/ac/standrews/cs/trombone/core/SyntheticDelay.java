package uk.ac.standrews.cs.trombone.core;

import java.io.Serializable;
import java.net.InetAddress;
import uk.ac.standrews.cs.trombone.core.util.Named;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public interface SyntheticDelay extends Serializable, Named {

    void apply(InetAddress from, InetAddress to) throws InterruptedException;
}
