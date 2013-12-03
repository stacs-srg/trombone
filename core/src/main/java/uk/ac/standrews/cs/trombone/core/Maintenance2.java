package uk.ac.standrews.cs.trombone.core;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.mashti.jetson.exception.RPCException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.trombone.core.gossip.selector.FirstReachable;
import uk.ac.standrews.cs.trombone.core.gossip.selector.LastReachable;
import uk.ac.standrews.cs.trombone.core.gossip.selector.Selector;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class Maintenance2 implements PropertyChangeListener{

    private static final Logger LOGGER = LoggerFactory.getLogger(Maintenance2.class);
    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(100);
    private final Peer local;

    Maintenance2(Peer local) {

        this.local = local;
        local.addExposureChangeListener(this);
    }

    @Override
    public void propertyChange(final PropertyChangeEvent event) {
        
        
        
        

    }

    void add(boolean pull_push, Selector data_selector, Selector recipient_selector) throws RPCException {

        final PeerReference successor = FirstReachable.getInstance().select(local)[0];
        local.getRemote(successor).pull(LastReachable.getInstance());

    }

}
