package uk.ac.standrews.cs.trombone.metric;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import uk.ac.standrews.cs.trombone.Peer;
import uk.ac.standrews.cs.trombone.metric.core.Counter;
import uk.ac.standrews.cs.trombone.metric.core.Rate;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class PeerExposureChangeMeter {

    static final Counter EXPOSED_PEERS_COUNTER = new Counter();
    private final Rate exposed;
    private final Rate unexposed;

    public PeerExposureChangeMeter(final Peer peer) {

        exposed = new Rate();
        unexposed = new Rate();

        peer.addExposureChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(final PropertyChangeEvent event) {

                final Boolean arrived = (Boolean) event.getNewValue();
                if (arrived) {
                    EXPOSED_PEERS_COUNTER.increment();
                    exposed.mark();
                }
                else {
                    EXPOSED_PEERS_COUNTER.decrement();
                    unexposed.mark();
                }
            }
        });
    }

    public static Counter getGlobalExposedPeersCounter() {

        return EXPOSED_PEERS_COUNTER;
    }
}
