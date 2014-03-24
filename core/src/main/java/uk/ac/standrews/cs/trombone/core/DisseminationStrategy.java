package uk.ac.standrews.cs.trombone.core;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.commons.lang.reflect.MethodUtils;
import org.mashti.jetson.exception.RPCException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.trombone.core.selector.Selector;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class DisseminationStrategy implements Iterable<DisseminationStrategy.Action>, Serializable {

    private static final long serialVersionUID = 7398182589384122556L;
    static final Method PUSH_METHOD = MethodUtils.getAccessibleMethod(PeerRemote.class, "push", List.class);
    static final Method PULL_METHOD = MethodUtils.getAccessibleMethod(PeerRemote.class, "pull", Selector.class);
    private static final List<PeerReference> EMPTY_REFERENCES = Collections.unmodifiableList(new ArrayList<PeerReference>());
    private static final Logger LOGGER = LoggerFactory.getLogger(DisseminationStrategy.class);
    private final ArrayList<Action> actions;
    private int non_opportunistic_interval_millis = 2_000;

    public DisseminationStrategy() {

        actions = new ArrayList<>();
    }

    @Override
    public Iterator<Action> iterator() {

        return actions.iterator();
    }

    public boolean addAction(Action action) {

        return actions.add(action);
    }

    public int getInterval() {

        return non_opportunistic_interval_millis;
    }

    public void setInterval(int non_opportunistic_interval_millis) {

        this.non_opportunistic_interval_millis = non_opportunistic_interval_millis;
    }

    public Action getActionAt(int index) {

        return actions.get(index);
    }

    public int size() {

        return actions.size();
    }

    public void setActionAt(final int index, final Action action) {

        actions.set(index, action);
    }

    public List<Action> subActionList(int from, int to) {

        return new CopyOnWriteArrayList<>(actions.subList(from, to));
    }

    public void addActions(final Collection<? extends Action> actions) {

        this.actions.addAll(actions);
    }

    @Override
    public String toString() {

        final StringBuilder sb = new StringBuilder("DisseminationStrategy{");
        sb.append("actions=").append(actions);
        sb.append('}');
        return sb.toString();
    }

    public static class Action implements Serializable {

        private static final long serialVersionUID = -8157677096305292929L;
        private final boolean opportunistic;
        private final boolean push;
        private final Selector data_selector;
        private final Selector recipient_selector;

        public Action(final boolean opportunistic, final boolean push, final Selector data_selector, final Selector recipient_selector) {

            this.opportunistic = opportunistic;
            this.push = push;
            this.data_selector = data_selector;
            this.recipient_selector = recipient_selector;
        }

        public boolean isOpportunistic() {

            return opportunistic;
        }

        public boolean recipientsContain(Peer local, final PeerReference recipient) {

            final List<PeerReference> recipients = pullQuietly(local, recipient_selector);
            return recipients.contains(recipient);
        }

        public void nonOpportunistically(final Peer local) throws RPCException {

            if (!opportunistic) {

                final List<PeerReference> recipients = getRecipients(local);
                if (push) {
                    final List<PeerReference> data_to_push = getPushData(local);
                    if (data_to_push != null && !data_to_push.isEmpty()) {
                        for (PeerReference recipient : recipients) {
                            if (recipient != null) {
                                local.getAsynchronousRemote(recipient).push(data_to_push);
                            }
                        }
                    }
                }
                else {
                    for (final PeerReference recipient : recipients) {
                        if (recipient != null) {
                            Futures.addCallback(local.getAsynchronousRemote(recipient).pull(data_selector), new FutureCallback<List<PeerReference>>() {

                                @Override
                                public void onSuccess(final List<PeerReference> result) {

                                    local.push(result);
                                }

                                @Override
                                public void onFailure(final Throwable t) {
                                }
                            }, Maintenance.SCHEDULER);
                        }
                    }
                }
            }
        }

        @Override
        public String toString() {

            return "Action{" + "opportunistic=" + opportunistic + ", push=" + push + ", data_selector=" + data_selector + ", recipient_selector=" + recipient_selector + '}';
        }

        List<PeerReference> getPushData(final Peer local) {

            return pullQuietly(local, data_selector);
        }

        Method getMethod() {

            return push ? PUSH_METHOD : PULL_METHOD;
        }

        Object[] getArguments(Peer local) {

            return new Object[] {push ? getPushData(local) : data_selector};
        }

        List<PeerReference> getRecipients(final Peer local) throws RPCException {

            return recipient_selector.select(local);
        }

        private static List<PeerReference> pullQuietly(final Peer local, Selector selector) {

            try {
                return local.pull(selector);
            }
            catch (RPCException e) {
                LOGGER.warn("failed to pull from local", e);
                return EMPTY_REFERENCES;
            }
        }
    }
}
