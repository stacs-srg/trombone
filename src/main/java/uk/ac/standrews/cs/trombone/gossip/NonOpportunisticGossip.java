package uk.ac.standrews.cs.trombone.gossip;

import java.lang.reflect.Proxy;
import uk.ac.standrews.cs.shabdiz.util.Duration;
import uk.ac.standrews.cs.shabdiz.util.HashCodeUtil;
import uk.ac.standrews.cs.trombone.Peer;
import uk.ac.standrews.cs.trombone.PeerReference;
import uk.ac.standrews.cs.trombone.PeerRemote;
import uk.ac.standrews.cs.trombone.PeerRemoteFactory;
import uk.ac.standrews.cs.trombone.gossip.selector.Selector;

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

    @Override
    public int hashCode() {

        return HashCodeUtil.generate(peer.hashCode(), selector.hashCode(), gossip.hashCode(), delay.hashCode());
    }

    @Override
    public boolean equals(final Object other) {

        if (this == other) { return true; }
        if (!(other instanceof NonOpportunisticGossip)) { return false; }
        final NonOpportunisticGossip that = (NonOpportunisticGossip) other;
        return delay.equals(that.delay) && gossip.equals(that.gossip) && peer.equals(that.peer) && selector.equals(that.selector);
    }
}
