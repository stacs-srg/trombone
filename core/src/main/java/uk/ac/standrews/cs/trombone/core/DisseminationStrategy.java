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
import uk.ac.standrews.cs.trombone.core.selector.Selector;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class DisseminationStrategy implements Iterable<DisseminationStrategy.Action>, Serializable {

    private static final long serialVersionUID = 7398182589384122556L;
    static final Method PUSH_METHOD = MethodUtils.getAccessibleMethod(PeerRemote.class, "push", List.class);
    static final Method PULL_METHOD = MethodUtils.getAccessibleMethod(PeerRemote.class, "pull", Selector.class);
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

    public List<Action> getActions() {

        return Collections.unmodifiableList(actions);
    }

    @Override
    public String toString() {

        final StringBuilder sb = new StringBuilder("DisseminationStrategy{");
        sb.append("actions=").append(actions);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(final Object o) {

        if (this == o) { return true; }
        if (!(o instanceof DisseminationStrategy)) { return false; }

        final DisseminationStrategy that = (DisseminationStrategy) o;

        if (non_opportunistic_interval_millis != that.non_opportunistic_interval_millis) { return false; }
        if (actions != null ? !actions.equals(that.actions) : that.actions != null) { return false; }

        return true;
    }

    @Override
    public int hashCode() {

        int result = actions != null ? actions.hashCode() : 0;
        result = 31 * result + non_opportunistic_interval_millis;
        return result;
    }

    public static class Action implements Serializable {

        private static final long serialVersionUID = -8157677096305292929L;
        private boolean opportunistic;

        private boolean push;
        private Selector data_selector;
        private Selector recipient_selector;

        public Action(final boolean opportunistic, final boolean push, final Selector data_selector, final Selector recipient_selector) {

            this.opportunistic = opportunistic;
            this.push = push;
            this.data_selector = data_selector;
            this.recipient_selector = recipient_selector;
        }

        public boolean isOpportunistic() {

            return opportunistic;
        }

        public boolean isPush() {

            return push;
        }

        public boolean recipientsContain(Peer local, final PeerReference recipient) {

            return local.pull(recipient_selector).contains(recipient);
        }

        public void nonOpportunistically(final Peer local) throws RPCException {

            if (!opportunistic) {

                final List<PeerReference> recipients = getRecipients(local);
                if (recipients != null && !recipients.isEmpty()) {
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
        }

        @Override
        public String toString() {

            final StringBuilder builder = new StringBuilder();
            builder.append("Action{");
            builder.append(opportunistic ? "opportunistically " : "non-opportunistically ");
            if (push) {
                builder.append("push ");
                builder.append(data_selector);
                builder.append(" to ");
                builder.append(recipient_selector);
            }
            else {
                builder.append("pull ");
                builder.append(data_selector);
                builder.append(" from ");
                builder.append(recipient_selector);
            }
            builder.append('}');
            return builder.toString();
        }

        public void setPush(final boolean push) {

            this.push = push;
        }

        public void setOpportunistic(final boolean opportunistic) {

            this.opportunistic = opportunistic;
        }

        public Selector getRecipientSelector() {

            return recipient_selector;
        }

        public Selector getDataSelector() {

            return data_selector;
        }

        public void setRecipientSelector(final Selector recipient_selector) {

            this.recipient_selector = recipient_selector;
        }

        public void setDataSelector(final Selector data_selector) {

            this.data_selector = data_selector;
        }

        List<PeerReference> getPushData(final Peer local) {

            return local.pull(data_selector);
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

        @Override
        public boolean equals(final Object o) {

            if (this == o) { return true; }
            if (!(o instanceof Action)) { return false; }

            final Action action = (Action) o;

            if (opportunistic != action.opportunistic) { return false; }
            if (push != action.push) { return false; }
            if (data_selector != null ? !data_selector.equals(action.data_selector) : action.data_selector != null) { return false; }
            if (recipient_selector != null ? !recipient_selector.equals(action.recipient_selector) : action.recipient_selector != null) { return false; }

            return true;
        }

        @Override
        public int hashCode() {

            int result = (opportunistic ? 1 : 0);
            result = 31 * result + (push ? 1 : 0);
            result = 31 * result + (data_selector != null ? data_selector.hashCode() : 0);
            result = 31 * result + (recipient_selector != null ? recipient_selector.hashCode() : 0);
            return result;
        }
    }
}
