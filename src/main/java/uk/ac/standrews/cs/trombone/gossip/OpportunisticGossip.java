package uk.ac.standrews.cs.trombone.gossip;

import java.lang.reflect.Method;
import java.util.Arrays;
import org.mashti.jetson.FutureResponse;
import uk.ac.standrews.cs.shabdiz.util.HashCodeUtil;
import uk.ac.standrews.cs.trombone.Peer;
import uk.ac.standrews.cs.trombone.PeerReference;
import uk.ac.standrews.cs.trombone.PeerRemote;
import uk.ac.standrews.cs.trombone.PeerRemoteFactory;
import uk.ac.standrews.cs.trombone.gossip.selector.Selector;

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

    @Override
    public int hashCode() {

        return HashCodeUtil.generate(peer.hashCode(), selector.hashCode(), push ? 1 : 0, max_size, method.hashCode(), Arrays.hashCode(pull_arguments));
    }

    @Override
    public boolean equals(final Object other) {

        if (this == other) { return true; }
        if (!(other instanceof OpportunisticGossip)) { return false; }

        final OpportunisticGossip that = (OpportunisticGossip) other;
        if (max_size != that.max_size) { return false; }
        if (push != that.push) { return false; }
        if (!method.equals(that.method)) { return false; }
        if (!peer.equals(that.peer)) { return false; }
        if (!Arrays.equals(pull_arguments, that.pull_arguments)) { return false; }
        if (!selector.equals(that.selector)) { return false; }

        return true;
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
