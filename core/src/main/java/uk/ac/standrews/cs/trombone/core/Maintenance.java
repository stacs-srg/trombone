package uk.ac.standrews.cs.trombone.core;

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
import org.mashti.jetson.util.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.trombone.core.gossip.selector.FirstReachable;
import uk.ac.standrews.cs.trombone.core.gossip.selector.LastReachable;
import uk.ac.standrews.cs.trombone.core.gossip.selector.Selector;
import uk.ac.standrews.cs.trombone.core.gossip.selector.Self;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class Maintenance {

    private static final Logger LOGGER = LoggerFactory.getLogger(Maintenance.class);
    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(100, new NamedThreadFactory("maintenance_", true));
    private static final Method PUSH;
    private static final Method PULL;

    static {
        try {
            PUSH = PeerRemote.class.getDeclaredMethod("push", PeerReference[].class);
            PULL = PeerRemote.class.getDeclaredMethod("pull", Selector.class);
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

    public Maintenance(final Peer peer) {

        this.peer = peer;
        non_opportunistic_gossips = new ArrayList<NonOpportunisticGossip>();

        //        BigInteger b = new BigInteger(peer.getKey().getValue());
        //        final byte[] value = b.add(BigInteger.ONE).toByteArray();
        //        final Key peer_1 = new Key(value);
                non_opportunistic_gossips.add(new NonOpportunisticGossip(FirstReachable.getInstance(), new OpportunisticGossip(LastReachable.getInstance(), false), 1, TimeUnit.SECONDS));
        //        non_opportunistic_gossips.add(new NonOpportunisticGossip(First.getInstance(), new OpportunisticGossip(new LookupSelector(peer_1), false), 500, TimeUnit.MILLISECONDS));
                non_opportunistic_gossips.add(new NonOpportunisticGossip(FirstReachable.getInstance(), new OpportunisticGossip(Self.getInstance(), true), 500, TimeUnit.MILLISECONDS));
        //        non_opportunistic_gossips.add(new NonOpportunisticGossip(First.getInstance(), new OpportunisticGossip(Last.getInstance(), false), 500, TimeUnit.MILLISECONDS));
        //        non_opportunistic_gossips.add(new NonOpportunisticGossip(new RandomSelector(5), new OpportunisticGossip(new RandomSelector(5), true), 1, TimeUnit.SECONDS));
        //        non_opportunistic_gossips.add(new NonOpportunisticGossip(new RandomSelector(5), new OpportunisticGossip(new RandomSelector(5), false), 1, TimeUnit.SECONDS));
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

    protected boolean addOpportunisticGossip(OpportunisticGossip gossip) {

        return opportunistic_gossips.add(gossip);
    }

    protected synchronized boolean addNonOpportunisticGossip(NonOpportunisticGossip gossip) {

        return non_opportunistic_gossips.add(gossip) && isStarted() ? schedule(gossip) : true;
    }

    protected List<OpportunisticGossip> getOpportunisticGossips() {

        return new CopyOnWriteArrayList<OpportunisticGossip>(opportunistic_gossips);
    }

    protected List<NonOpportunisticGossip> getNonOpportunisticGossips() {

        return new CopyOnWriteArrayList<NonOpportunisticGossip>(non_opportunistic_gossips);
    }

    private boolean schedule(final NonOpportunisticGossip gossip) {

        final long delay_length = gossip.getInterval();
        final TimeUnit delay_unit = gossip.getIntervalUnit();
        final ScheduledFuture<?> future = SCHEDULER.scheduleWithFixedDelay(gossip, delay_length, delay_length, delay_unit);
        return scheduled_non_opportunistic_gossips.add(future);
    }

    class OpportunisticGossip {

        private final Selector selector;
        private final boolean push;
        private final Method method;
        private final Object[] pull_arguments;

        OpportunisticGossip(Selector selector, boolean push) {

            this.selector = selector;
            this.push = push;
            if (push) {
                method = PUSH;
                pull_arguments = null;
            }
            else {
                method = PULL;
                pull_arguments = new Object[] {selector};
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
                selection = selector.select(peer);
            }
            catch (Exception e) {
                LOGGER.error("failure occurred when constructing non-opportunistic maintenance", e);
                selection = null;
            }
            return new Object[] {selection};
        }
    }

    class NonOpportunisticGossip implements Runnable {

        private final Selector recipient_selector;
        private final OpportunisticGossip gossip;
        private final long interval;
        private final TimeUnit interval_unit;

        NonOpportunisticGossip(final Selector recipient_selector, OpportunisticGossip gossip, long interval, TimeUnit interval_unit) {

            this.recipient_selector = recipient_selector;
            this.gossip = gossip;
            this.interval = interval;
            this.interval_unit = interval_unit;
        }

        @Override
        public void run() {

            try {
                final PeerReference[] select = recipient_selector.select(peer);
                if (select != null && select.length > 0) {
                    final PeerReference recipient = select[0];
                    if (recipient != null) {
                        final PeerRemote remote = peer.getRemote(recipient);
                        PeerRemoteFactory.PeerClient client = (PeerRemoteFactory.PeerClient) Proxy.getInvocationHandler(remote);
                        client.writeRequest(gossip.get(client));
                    }
                }
            }
            catch (Exception e) {
                LOGGER.debug("failure occurred when executing non-opportunistic maintenance", e);
            }
        }

        long getInterval() {

            return interval;
        }

        TimeUnit getIntervalUnit() {

            return interval_unit;
        }
    }
}
