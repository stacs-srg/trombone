package uk.ac.standrews.cs.trombone.trombone.gossip;

import java.lang.reflect.Proxy;
import uk.ac.standrews.cs.trombone.trombone.Peer;
import uk.ac.standrews.cs.trombone.trombone.PeerReference;
import uk.ac.standrews.cs.trombone.trombone.PeerRemote;
import uk.ac.standrews.cs.trombone.trombone.PeerRemoteFactory;
import uk.ac.standrews.cs.trombone.trombone.selector.Selector;
import uk.ac.standrews.cs.shabdiz.util.Duration;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class NonOpportunisticGossip implements Runnable {

    private final Peer peer;
    private final Selector selector;
    private final OpportunisticGossip gossip;
    private final Duration delay;

    public NonOpportunisticGossip(final Peer peer, final Selector selector, OpportunisticGossip gossip, Duration delay) {

        this.peer = peer;
        this.selector = selector;
        this.gossip = gossip;
        this.delay = delay;
    }

    public Duration getDelay() {

        return delay;
    }

    @Override
    public void run() {

        try {
            final PeerReference[] select = selector.select(peer, 1);
            if (select != null && select.length > 0) {
                final PeerReference recipient = select[0];
                final PeerRemote remote = peer.getRemote(recipient);
                PeerRemoteFactory.PeerClient client = (PeerRemoteFactory.PeerClient) Proxy.getInvocationHandler(remote);
                client.writeRequest(gossip.get(client));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
