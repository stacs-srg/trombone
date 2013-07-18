package uk.ac.standrews.cs.trombone.trombone.gossip;

import org.mashti.jetson.FutureResponse;
import java.lang.reflect.Method;
import uk.ac.standrews.cs.trombone.trombone.Peer;
import uk.ac.standrews.cs.trombone.trombone.PeerReference;
import uk.ac.standrews.cs.trombone.trombone.PeerRemote;
import uk.ac.standrews.cs.trombone.trombone.PeerRemoteFactory;
import uk.ac.standrews.cs.trombone.trombone.selector.Selector;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class OpportunisticGossip {

    private static final Method PUSH;
    private static final Method PULL;
    static {
        try {
            PUSH = PeerRemote.class.getDeclaredMethod("push", PeerReference[].class);
            PULL = PeerRemote.class.getDeclaredMethod("pull", Selector.class, Integer.TYPE);
        }
        catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
    private final Peer peer;
    private final Selector selector;
    private final boolean push;
    private final int max_size;
    private final Method method;
    private final Object[] pull_arguments;

    public OpportunisticGossip(Peer peer, Selector selector, boolean push, int max_size) {

        this.peer = peer;
        this.selector = selector;
        this.push = push;
        this.max_size = max_size;
        if (push) {
            method = PUSH;
            pull_arguments = null;

        }
        else {
            method = PULL;
            pull_arguments = new Object[]{selector, max_size};
        }
    }

    public FutureResponse get(PeerRemoteFactory.PeerClient recipient) {

        final FutureResponse response = recipient.newFutureResponse(method, push ? getPushArguments() : getPullArguments());
        return response;
    }

    private Object[] getPullArguments() {

        return pull_arguments;
    }

    private Object[] getPushArguments() {

        PeerReference[] selection;
        try {
            selection = selector.select(peer, max_size);
        }
        catch (Exception e) {
            e.printStackTrace();
            selection = null;
        }
        return new Object[]{selection};
    }
}
