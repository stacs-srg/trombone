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

import uk.ac.standrews.cs.trombone.trombone.Peer;

public class PeerStateSizeNotification {

    private final Peer peer;
    private final int size;

    public PeerStateSizeNotification(final Peer peer, final int size) {

        this.peer = peer;
        this.size = size;
    }

    public Peer getPeer() {

        return peer;
    }

    public int getSize() {

        return size;
    }
}
