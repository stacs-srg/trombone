/*
 * Copyright 2013 Masih Hajiarabderkani
 *
 * This file is part of Trombone.
 *
 * Trombone is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Trombone is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Trombone.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.ac.standrews.cs.trombone.evaluation;

import com.google.common.util.concurrent.AbstractFuture;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.mashti.jetson.exception.RPCException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.shabdiz.util.Duration;
import uk.ac.standrews.cs.trombone.Peer;
import uk.ac.standrews.cs.trombone.PeerReference;
import uk.ac.standrews.cs.trombone.churn.Churn;
import uk.ac.standrews.cs.trombone.evaluation.membership.MembershipService;
import uk.ac.standrews.cs.trombone.util.TimeoutSupport;
import uk.ac.standrews.cs.trombone.util.Timeoutable;
import uk.ac.standrews.cs.trombone.workload.Workload;

import static uk.ac.standrews.cs.trombone.churn.Churn.Availability;

public class PeerConductor extends AbstractFuture<Void> implements Timeoutable, Runnable {

    //TODO parameterize pool size
    private static final ScheduledExecutorService CHURN_EXECUTOR = Executors.newScheduledThreadPool(50);
    private static final ScheduledExecutorService WORKLOAD_EXECUTOR = Executors.newScheduledThreadPool(100);
    private static final Logger LOGGER = LoggerFactory.getLogger(PeerConductor.class);
    private final Peer peer;
    private final Churn churn;
    private final Workload workload;
    private final LinkedBlockingDeque<ScheduledFuture> scheduled_work_queue;
    private final TimeoutSupport timing;
    private volatile MembershipService membership_service;

    public PeerConductor(final Peer peer, final Churn churn, final Workload workload) {

        this.peer = peer;
        this.churn = churn;
        this.workload = workload;
        timing = new TimeoutSupport();
        scheduled_work_queue = new LinkedBlockingDeque<ScheduledFuture>();
        configure();
    }

    @Override
    public void run() {

        if (timing.startCountdown()) {
            attemptStart();
        }
        else {
            LOGGER.warn("peer conductor is already conducting a peer.");
        }
    }

    @Override
    public void setTimeout(final Duration timeout) {

        timing.setTimeout(timeout);
    }

    public void setMembershipService(MembershipService membership_service) {

        this.membership_service = membership_service;
    }

    @Override
    public boolean isDone() {

        return timing.isTimedOut() || super.isDone();
    }

    private void configure() {

        unexposePeer();
        peer.addExposureChangeListener(new PeerConductorExposureChangeListener());
        peer.addMembershipChangeListener(new PeerConductorMembershipChangeListener());
    }

    private void unexposePeer() {

        try {
            peer.unexpose();
        }
        catch (IOException e) {
            LOGGER.error("failed to unexpose peer", e);
        }
    }

    private void attemptStart() {

        try {
            start();
        }
        catch (final IOException e) {
            LOGGER.error("failed to start availability cycle", e);
            departPermanantly();
        }
    }

    private void attemptStartWithDelay(final long duration_nanos) {

        CHURN_EXECUTOR.schedule(new Runnable() {

            @Override
            public void run() {

                try {
                    attemptStart();
                }
                catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        }, duration_nanos, TimeUnit.NANOSECONDS);
    }

    private void start() throws IOException {

        final Availability availability = churn.getAvailabilityAt(System.nanoTime());
        final long duration = availability.getDurationInNanos();
        final boolean exposed = availability.isAvailable();

        setPeerExposure(exposed);

        if (!timing.exceedsRemainingTimeInNanos(duration)) {
            attemptStartWithDelay(duration);
        }
        else {
            if (exposed) {
                departPermanantlyWithDelay(timing.getRemainingTimeInNanos());
            }
            else {
                departPermanantly();
            }
        }
    }

    private void departPermanantlyWithDelay(final long delay_in_nanos) {

        CHURN_EXECUTOR.schedule(new Runnable() {

            @Override
            public void run() {

                departPermanantly();

            }
        }, delay_in_nanos, TimeUnit.NANOSECONDS);
    }

    private void setPeerExposure(final boolean exposed) throws IOException {

        if (exposed) {
            peer.expose();
        }
        else {
            peer.unexpose();
        }
    }

    private void departPermanantly() {

        try {
            peer.unexpose();
            set(null);
        }
        catch (IOException e) {
            setException(e);
        }
    }

    private boolean awaitSuccessfulJoin() {

        boolean successfully_joined = false;
        while (!isInterrupted() && peer.isExposed() && !successfully_joined && !timing.isTimedOut()) {
            successfully_joined = attemptJoin() && peer.isExposed() && !timing.isTimedOut();
        }
        return successfully_joined;
    }

    private boolean attemptJoin() {

        boolean successful;

        try {
            final PeerReference known_peer = getKnownMember();
            peer.join(known_peer);
            successful = true;
        }
        catch (final RPCException e) {
            successful = false;
        }
        return successful;
    }

    private PeerReference getKnownMember() throws RPCException {

        final PeerReference peer_reference = peer.getSelfReference();
        if (membership_service == null) { return peer_reference; }
        return membership_service.getMember(peer_reference);
    }

    private void executeWorkloadWhileExposed() {

        final ScheduledFuture next_scheduled_work = scheduleNextWork();
        queueScheduledWork(next_scheduled_work);
    }

    private ScheduledFuture scheduleNextWork() {

        final Workload.Lookup task = workload.getLookupAt(System.nanoTime()); // FIXME nextTask time
        final Duration interval = task.getInterval();
        final ScheduledFuture scheduled_work = WORKLOAD_EXECUTOR.schedule(new Runnable() {

            @Override
            public void run() {

                if (!isInterrupted() && peer.isExposed() && !isDone()) {
                    executeWorkloadWhileExposed();
                    try {
                        peer.lookup(task.getTarget(), task.getRetryThreshold());
                    }
                    catch (RPCException e) {
                        //            e.printStackTrace();
                    }
                }
                purgeScheduledWorkQueue();
            }
        }, interval.getLength(), interval.getTimeUnit());
        return scheduled_work;
    }

    private void purgeScheduledWorkQueue() {

        final ScheduledFuture first_queued_work = scheduled_work_queue.pollLast();
        if (first_queued_work != null && !first_queued_work.isDone()) {
            scheduled_work_queue.offerLast(first_queued_work);
        }
    }

    private void queueScheduledWork(final ScheduledFuture scheduled_work) {

        scheduled_work_queue.offerFirst(scheduled_work);
    }

    private void cancelScheduledWorkloads() {

        assert !peer.isExposed();
        final Iterator<ScheduledFuture> work_queue_iterator = scheduled_work_queue.iterator();
        while (work_queue_iterator.hasNext()) {
            final ScheduledFuture scheduled_workload = work_queue_iterator.next();
            scheduled_workload.cancel(true);
            work_queue_iterator.remove();
        }
    }

    private static boolean isInterrupted() {

        return Thread.currentThread().isInterrupted();
    }

    private final class PeerConductorExposureChangeListener implements PropertyChangeListener {

        @Override
        public synchronized void propertyChange(final PropertyChangeEvent event) {

            assert Boolean.class.isInstance(event.getNewValue());
            final boolean exposed = Boolean.class.cast(event.getNewValue());
            if (exposed) {
                if (awaitSuccessfulJoin()) {
                    executeWorkloadWhileExposed();
                }
            }
            else {
                cancelScheduledWorkloads();
            }
        }
    }

    private final class PeerConductorMembershipChangeListener implements PropertyChangeListener {

        @Override
        public synchronized void propertyChange(final PropertyChangeEvent event) {

            assert Boolean.class.isInstance(event.getNewValue());
            final PeerReference peer_reference = peer.getSelfReference();
            final boolean joined = Boolean.class.cast(event.getNewValue());
            try {
                if (membership_service != null) {
                    if (joined) {
                        membership_service.notifyArrival(peer_reference);
                    }
                    else {
                        membership_service.notifyDeparture(peer_reference);
                    }
                }
            }
            catch (RPCException e) {
                LOGGER.warn("failed to notify membership service of change", e);
            }
        }
    }

}
