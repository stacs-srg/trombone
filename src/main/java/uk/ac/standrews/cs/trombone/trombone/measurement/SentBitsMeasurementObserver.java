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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import uk.ac.standrews.cs.trombone.trombone.key.Key;
import uk.ac.standrews.cs.trombone.trombone.math.Statistics;
import uk.ac.standrews.cs.trombone.trombone.measurement.persistence.Persistor;

public class SentBitsMeasurementObserver implements MeasurementObserver<SentBitsMeasurementObserver.SentBitsDigest> {

    private final Persistor persistor;
    private final AtomicReference<ConcurrentHashMap<Key, AtomicLong>> bits_per_peer_reference;
    private final ReentrantLock bits_sent_lock = new ReentrantLock();

    protected SentBitsMeasurementObserver(final Persistor persistor) {

        this.persistor = persistor;
        bits_per_peer_reference = new AtomicReference<ConcurrentHashMap<Key, AtomicLong>>();
        refreshBitsPerPeerMap();
    }

    public SentBitsDigest newSentBitsMeasurement(final Key sender) {

        return new SentBitsDigest(sender);
    }

    @Override
    public boolean isObservable(final SentBitsDigest value) {

        return value != null && value.sender != null;
    }

    @Override
    public void notify(final SentBitsDigest value) {

        if (isObservable(value)) {
            final AtomicLong stats = getBitsSent(value.sender, bits_per_peer_reference.get());
            stats.addAndGet(value.sent_bytes);
        }
    }

    @Override
    public void flush() {

        final Statistics average_bits_per_peer = new Statistics();
        final ConcurrentHashMap<Key, AtomicLong> bits_per_peer = refreshBitsPerPeerMap();
        for (final AtomicLong bits_sent : bits_per_peer.values()) {
            average_bits_per_peer.addSample(bits_sent.get());
        }

        final StatisticalMeasurement digest = new StatisticalMeasurement(average_bits_per_peer, MeasurableAspect.SENT_BITS_PER_PEER);
        persistor.persist(digest);
    }

    private final AtomicLong getBitsSent(final Key key, final ConcurrentHashMap<Key, AtomicLong> bits_per_peer) {

        bits_sent_lock.lock();
        try {
            AtomicLong stats;
            if (bits_per_peer.containsKey(key)) {
                stats = bits_per_peer.get(key);
            }
            else {
                stats = new AtomicLong();
                bits_per_peer.put(key, stats);
            }
            return stats;
        }
        finally {
            bits_sent_lock.unlock();
        }
    }

    private ConcurrentHashMap<Key, AtomicLong> refreshBitsPerPeerMap() {

        return bits_per_peer_reference.getAndSet(new ConcurrentHashMap<Key, AtomicLong>());

    }

    public class SentBitsDigest {

        private final Key sender;
        private int sent_bytes = 0;

        private SentBitsDigest(final Key sender) {

            this.sender = sender;
        }

        public synchronized void incrememtBitsSent(final int bits) {

            if (bits > 0) {
                sent_bytes += bits;
            }
        }
    }
}
