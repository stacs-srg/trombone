package uk.ac.standrews.cs.trombone;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.mashti.jetson.FutureResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.shabdiz.util.Duration;
import uk.ac.standrews.cs.trombone.gossip.selector.Selector;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class Maintenance {

    private static final Logger LOGGER = LoggerFactory.getLogger(Maintenance.class);
    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(100);
    private static final Method PUSH;
    private static final Method PULL;
    private static final String PUSH_METHOD_NAME = "push";
    private static final String PULL_METHOD_NAME = "pull";

    static {
        try {
            PUSH = PeerRemote.class.getDeclaredMethod(PUSH_METHOD_NAME, PeerReference[].class);
            PULL = PeerRemote.class.getDeclaredMethod(PULL_METHOD_NAME, Selector.class, Integer.TYPE);
        }
        catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
    private final List<NonOpportunisticGossip> non_opportunistic_gossips;
    private final List<OpportunisticGossip> opportunistic_gossips;
    private final List<ScheduledFuture<?>> scheduled_non_opportunistic_gossips;
    private final Peer peer;
    private volatile boolean started;

    public Maintenance(Peer peer) {

        this.peer = peer;
        non_opportunistic_gossips = new ArrayList<NonOpportunisticGossip>();
        opportunistic_gossips = new ArrayList<OpportunisticGossip>();
        scheduled_non_opportunistic_gossips = new ArrayList<ScheduledFuture<?>>();
    }

    public synchronized void start() {

        if (!isStarted()) {
            for (NonOpportunisticGossip gossip : non_opportunistic_gossips) {
                schedule(gossip);
            }
            started = true;
        }
    }

    public synchronized void stop() {

        if (isStarted()) {
            for (ScheduledFuture<?> future : scheduled_non_opportunistic_gossips) {
                future.cancel(true);
            }
            scheduled_non_opportunistic_gossips.clear();
            started = false;
        }
    }

    public synchronized boolean isStarted() {

        return started;
    }

    private boolean schedule(final NonOpportunisticGossip gossip) {

        final Duration delay = gossip.getInterval();
        final long delay_length = delay.getLength();
        final TimeUnit delay_unit = delay.getTimeUnit();
        final ScheduledFuture<?> future = SCHEDULER.scheduleWithFixedDelay(gossip, delay_length, delay_length, delay_unit);
        return scheduled_non_opportunistic_gossips.add(future);
    }

    protected boolean addOpprotunisticGossip(OpportunisticGossip gossip) {

        return opportunistic_gossips.add(gossip);
    }

    protected synchronized boolean addNonOpprotunisticGossip(NonOpportunisticGossip gossip) {

        return non_opportunistic_gossips.add(gossip) && isStarted() ? schedule(gossip) : true;
    }

    protected List<OpportunisticGossip> getOpportunisticGossips() {

        return new CopyOnWriteArrayList<OpportunisticGossip>(opportunistic_gossips);
    }

    protected List<NonOpportunisticGossip> getNonOpportunisticGossips() {

        return new CopyOnWriteArrayList<NonOpportunisticGossip>(non_opportunistic_gossips);
    }

    class OpportunisticGossip {

        private final Selector selector;
        private final boolean push;
        private final int max_size;
        private final Method method;
        private final Object[] pull_arguments;

        OpportunisticGossip(Selector selector, boolean push, int max_size) {

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
                LOGGER.error("failure occured when constructing non-opportunistic maintenance", e);
                selection = null;
            }
            return new Object[]{selection};
        }
    }

    class NonOpportunisticGossip implements Runnable {

        private final Selector recipient_selector;
        private final OpportunisticGossip gossip;
        private final Duration interval;

        NonOpportunisticGossip(final Selector recipient_selector, OpportunisticGossip gossip, Duration interval) {

            this.recipient_selector = recipient_selector;
            this.gossip = gossip;
            this.interval = interval;
        }

        public Duration getInterval() {

            return interval;
        }

        @Override
        public void run() {

            try {
                final PeerReference[] select = recipient_selector.select(peer, 1);
                if (select != null && select.length > 0) {
                    final PeerReference recipient = select[0];
                    final PeerRemote remote = peer.getRemote(recipient);
                    PeerRemoteFactory.PeerClient client = (PeerRemoteFactory.PeerClient) Proxy.getInvocationHandler(remote);
                    client.writeRequest(gossip.get(client));
                }
            }
            catch (Exception e) {
                LOGGER.error("failure occured when executing non-opportunistic maintenance", e);
            }
        }
    }
}
