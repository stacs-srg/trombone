package uk.ac.standrews.cs.trombone.core.maintenance;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerReference;
import uk.ac.standrews.cs.trombone.core.selector.Selector;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class DisseminationStrategy implements Iterable<DisseminationStrategy.Action>, Serializable {

    private static final long serialVersionUID = 7398182589384122556L;

    private final ArrayList<Action> actions;
    private long non_opportunistic_interval_millis = 2_000;

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

    public long getIntervalInMilliseconds() {

        return non_opportunistic_interval_millis;
    }

    public void setInterval(long non_opportunistic_interval, TimeUnit unit) {

        non_opportunistic_interval_millis = TimeUnit.MILLISECONDS.convert(non_opportunistic_interval, unit);
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

        return "DisseminationStrategy{" + "actions=" + actions + '}';
    }

    @Override
    public boolean equals(final Object o) {

        if (this == o) { return true; }
        if (!(o instanceof DisseminationStrategy)) { return false; }

        final DisseminationStrategy that = (DisseminationStrategy) o;

        if (non_opportunistic_interval_millis != that.non_opportunistic_interval_millis) {
            return false;
        }
        if (actions != null ? !actions.equals(that.actions) : that.actions != null) { return false; }

        return true;
    }

    @Override
    public int hashCode() {

        int result = actions != null ? actions.hashCode() : 0;
        result = 31 * result + (int) (non_opportunistic_interval_millis ^ non_opportunistic_interval_millis >>> 32);
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

        public CompletableFuture<Boolean> recipientsContain(Peer local, final PeerReference recipient) {

            return local.pull(recipient_selector)
                    .thenCompose(result -> CompletableFuture.completedFuture(result.contains(recipient)));
        }

        public void nonOpportunistically(final Peer local) {

            if (!opportunistic) {

                final List<? extends PeerReference> recipients = getRecipients(local);
                if (recipients != null && !recipients.isEmpty()) {
                    if (push) {
                        getPushData(local).thenAcceptAsync(data_to_push -> {

                            if (data_to_push != null && !data_to_push.isEmpty()) {
                                recipients.stream()
                                        .filter(recipient -> recipient != null)
                                        .forEach(recipient -> {
                                            local.getAsynchronousRemote(recipient)
                                                    .push(data_to_push);
                                        });
                            }
                        }, local.getExecutor());
                    }
                    else {
                        recipients.stream()
                                .filter(recipient -> recipient != null)
                                .forEach(recipient -> {
                                    local.getAsynchronousRemote(recipient)
                                            .pull(data_selector)
                                            .thenAcceptAsync(result -> local.push(result), local.getExecutor());
                                });
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

        public static class Builder {

            private boolean push;
            private Selector data_selector;
            private Selector recipient_selector;
            private boolean opportunistic;

            public Builder push() {

                push = true;
                return this;
            }

            public Builder pull() {

                push = true;
                return this;
            }

            public Builder opportunistically() {

                opportunistic = true;
                return this;
            }

            public Builder nonOpportunistically() {

                opportunistic = false;
                return this;
            }

            public Builder data(Selector data_selector) {

                this.data_selector = data_selector;
                return this;
            }

            public Builder recipient(Selector recipient_selector) {

                this.recipient_selector = recipient_selector;
                return this;
            }

            public Action build() {

                return new Action(push, opportunistic, data_selector, recipient_selector);
            }

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

        CompletableFuture<List<PeerReference>> getPushData(final Peer local) {

            return local.pull(data_selector);
        }

        public Object[] getArguments(Peer local) {

            return new Object[] {push ? getPushData(local) : data_selector};
        }

        List<? extends PeerReference> getRecipients(final Peer local) {

            return recipient_selector.select(local);
        }

        @Override
        public boolean equals(final Object o) {

            if (this == o) { return true; }
            if (!(o instanceof Action)) { return false; }

            final Action action = (Action) o;

            if (opportunistic != action.opportunistic) { return false; }
            if (push != action.push) { return false; }
            if (data_selector != null ? !data_selector.equals(action.data_selector) : action.data_selector != null) {
                return false;
            }
            if (recipient_selector != null ? !recipient_selector.equals(action.recipient_selector) : action.recipient_selector != null) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {

            int result = opportunistic ? 1 : 0;
            result = 31 * result + (push ? 1 : 0);
            result = 31 * result + (data_selector != null ? data_selector.hashCode() : 0);
            result = 31 * result + (recipient_selector != null ? recipient_selector.hashCode() : 0);
            return result;
        }
    }
}
