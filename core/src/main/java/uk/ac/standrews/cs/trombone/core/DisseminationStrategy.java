package uk.ac.standrews.cs.trombone.core;

import java.lang.reflect.Method;
import org.apache.commons.lang.ArrayUtils;
import org.mashti.jetson.exception.RPCException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncommons.maths.binary.BitString;
import org.uncommons.util.reflection.ReflectionUtils;
import uk.ac.standrews.cs.trombone.core.selector.Selector;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class DisseminationStrategy {

    static final Method PUSH_METHOD = ReflectionUtils.findKnownMethod(PeerRemote.class, "push", PeerReference[].class);
    static final Method PULL_METHOD = ReflectionUtils.findKnownMethod(PeerRemote.class, "pull", Selector.class);
    private static final PeerReference[] EMPTY_REFERENCES = new PeerReference[0];
    private static final Logger LOGGER = LoggerFactory.getLogger(DisseminationStrategy.class);
    private final boolean opportunistic;
    private final boolean push;
    private final Selector data_selector;
    private final Selector recipient_selector;

    public DisseminationStrategy(final boolean opportunistic, final boolean push, final Selector data_selector, final Selector recipient_selector) {

        this.opportunistic = opportunistic;
        this.push = push;
        this.data_selector = data_selector;
        this.recipient_selector = recipient_selector;
    }
    
    public BitString toBitString(){
        
        final BitString bit_string = new BitString(9);
        bit_string.setBit(0, opportunistic);
        bit_string.setBit(0, push);
        bit_string.setBit(0, push);
        
        return null;
    }

    public boolean isOpportunistic() {

        return opportunistic;
    }

    public void nonOpportunistically(final Peer local) throws RPCException {

        final PeerReference[] recipients = recipient_selector.select(local);
        if (push) {
            final PeerReference[] data_to_push = pullQuietly(local, data_selector);
            if (data_to_push != null && data_to_push.length > 0) {
                for (PeerReference recipient : recipients) {
                    local.getRemote(recipient).push(data_to_push);
                }
            }
        }
        else {
            for (PeerReference recipient : recipients) {
                final PeerReference[] pulled_data = local.getRemote(recipient).pull(data_selector);
                local.push(pulled_data);
            }
        }
    }

    public boolean recipientsContain(Peer local, final PeerReference recipient) {

        final PeerReference[] recipients = pullQuietly(local, recipient_selector);
        return ArrayUtils.contains(recipients, recipient);
    }

    Method getMethod() {

        return push ? PUSH_METHOD : PULL_METHOD;
    }

    Object[] getArguments(Peer local) {

        return push ? new Object[] {pullQuietly(local, data_selector)} : new Object[] {data_selector};
    }

    private static PeerReference[] pullQuietly(final Peer local, Selector selector) {

        try {
            return local.pull(selector);
        }
        catch (RPCException e) {
            LOGGER.warn("failed to pull from local", e);
            return EMPTY_REFERENCES;
        }
    }
}
