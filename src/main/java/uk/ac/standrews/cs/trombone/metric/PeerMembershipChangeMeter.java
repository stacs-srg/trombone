package uk.ac.standrews.cs.trombone.metric;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.mashti.gauge.Counter;
import org.mashti.gauge.Rate;
import uk.ac.standrews.cs.trombone.Peer;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class PeerMembershipChangeMeter {

    private static final Rate GLOBAL_ARRIVALS = new Rate();
    private static final Rate GLOBAL_DEPARTURES = new Rate();
    private static final Counter ARRIVED_PEERS_COUNTER = new Counter();
    private final Rate arrival;
    private final Rate departure;

    public PeerMembershipChangeMeter(final Peer peer) {

        arrival = new Rate();
        departure = new Rate();

        peer.addMembershipChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(final PropertyChangeEvent event) {

                final Boolean arrived = (Boolean) event.getNewValue();
                if (arrived) {
                    GLOBAL_ARRIVALS.mark();
                    ARRIVED_PEERS_COUNTER.increment();
                    arrival.mark();
                }
                else {
                    GLOBAL_DEPARTURES.mark();
                    ARRIVED_PEERS_COUNTER.decrement();
                    departure.mark();
                }
            }
        });
    }

    public static Counter getArrivedPeersCounter() {

        return ARRIVED_PEERS_COUNTER;
    }

    public static Rate getGlobalDeparturesMeter() {

        return GLOBAL_DEPARTURES;
    }

    public static Rate getGlobalPeerArrivalMeter() {

        return GLOBAL_ARRIVALS;
    }
}
