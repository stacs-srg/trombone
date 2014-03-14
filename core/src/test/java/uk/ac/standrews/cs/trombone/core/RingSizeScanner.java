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

package uk.ac.standrews.cs.trombone.core;

import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.mashti.jetson.exception.RPCException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.shabdiz.ApplicationDescriptor;
import uk.ac.standrews.cs.shabdiz.ApplicationNetwork;
import uk.ac.standrews.cs.shabdiz.ApplicationState;
import uk.ac.standrews.cs.shabdiz.Scanner;
import uk.ac.standrews.cs.shabdiz.util.Duration;
import uk.ac.standrews.cs.trombone.core.selector.First;
import uk.ac.standrews.cs.trombone.core.selector.Last;

class RingSizeScanner extends Scanner {

    private static final Logger LOGGER = LoggerFactory.getLogger(RingSizeScanner.class);
    private static final String RING_SIZE_PROPERTY_NAME = "ring_size";
    private static final Duration DELAY = new Duration(1, TimeUnit.SECONDS);
    private static final Duration TIMEOUT = new Duration(3, TimeUnit.MINUTES);
    private static final First FIRST_REACHABLE = new First(1, true);
    private static final Last LAST_REACHABLE = new Last(1, true);
    private final AtomicInteger ring_size;

    protected RingSizeScanner() {

        super(DELAY, TIMEOUT, false);
        ring_size = new AtomicInteger();
    }

    public static int cycleLengthFrom(final PeerReference application_reference, final boolean forwards) throws InterruptedException {

        if (application_reference == null) { return 0; }

        final Set<PeerReference> nodes_encountered = new HashSet<PeerReference>(); // Record the nodes that have already been encountered.
        int cycle_length = 0;
        PeerReference node = application_reference;

        while (!Thread.currentThread().isInterrupted()) {

            cycle_length++;
            final PeerRemote proxy = PeerFactory.bind(node);
            try {
                node = forwards ? proxy.pull(FIRST_REACHABLE).get(0) : proxy.pull(LAST_REACHABLE).get(0);
            }
            catch (final RPCException e) {

                LOGGER.debug("error traversing the ring ", e);
                LOGGER.error("error traversing the ring - current size {}, forwards? {}", cycle_length, forwards);
                return 0; // Error traversing the ring, so it is broken.
            }

            if (node == null) { return 0; } // If the node is null, then the cycle is broken.
            if (node.getKey().equals(application_reference.getKey())) { return cycle_length; } // If the node is the start node, then a cycle has been found.
            if (nodes_encountered.contains(node)) { return cycle_length; } // If the node is not the start node and it has already been encountered, then there is a cycle but it doesn't contain the start node.
            nodes_encountered.add(node);
        }

        throw new InterruptedException();
    }

    @Override
    public void scan(final ApplicationNetwork network) {

        final PeerReference peer_reference = getFirstRunningPeer(network);
        try {
            final int ring_size_forwards = cycleLengthFrom(peer_reference, true);
            final int ring_size_backwards = cycleLengthFrom(peer_reference, false);
            if (isRingStable(ring_size_forwards, ring_size_backwards)) {
                final int new_ring_size = ring_size_forwards;
                final int old_ring_size = ring_size.getAndSet(new_ring_size);
                fireRingSizeChange(old_ring_size, new_ring_size);
            }
            LOGGER.info("cycle length forwards  \t {}", ring_size_forwards);
            LOGGER.info("cycle length backwards  \t {}", ring_size_backwards);
        }
        catch (final Exception e) {
            LOGGER.warn("interrupted while scanning ring size", e);
        }
    }

    public void addRingSizeChangeListener(final PropertyChangeListener listener) {

        addPropertyChangeListener(RING_SIZE_PROPERTY_NAME, listener);
    }

    public void removeRingSizeChangeListener(final PropertyChangeListener listener) {

        removePropertyChangeListener(RING_SIZE_PROPERTY_NAME, listener);
    }

    private boolean isRingStable(final int ring_size_forwards, final int ring_size_backwards) {

        return ring_size_forwards == ring_size_backwards;
    }

    private void fireRingSizeChange(final int old_ring_size, final int new_ring_size) {

        firePropertyChange(RING_SIZE_PROPERTY_NAME, old_ring_size, new_ring_size);
    }

    private PeerReference getFirstRunningPeer(final ApplicationNetwork network) {

        for (final ApplicationDescriptor descriptor : network) {
            if (descriptor.isInAnyOfStates(ApplicationState.RUNNING)) { return descriptor.getApplicationReference(); }
        }
        return null;
    }
}
