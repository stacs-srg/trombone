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
package uk.ac.standrews.cs.trombone.trombone.measurement;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Basic;
import javax.persistence.Entity;
import uk.ac.standrews.cs.trombone.trombone.Peer;

public class PeerArrivalDepartureNotification {

    private boolean arrived;
    private transient Peer peer;

    public PeerArrivalDepartureNotification(final Peer peer, final boolean arrived) {

        this.peer = peer;
        this.arrived = arrived;
    }

    public Peer getPeer() {

        return peer;
    }

    public boolean isArrived() {

        return arrived;
    }
}
