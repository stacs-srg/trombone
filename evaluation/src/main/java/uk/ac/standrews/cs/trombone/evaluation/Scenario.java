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

import java.io.Serializable;
import java.util.Collection;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import uk.ac.standrews.cs.shabdiz.util.Duration;

public class Scenario implements Serializable {

    private static final int DEFAULT_LOOKUP_RETRY_COUNT = 5;
    private static final long serialVersionUID = 5082585779371037794L;
    private final String name;
    private final long master_seed;
    private final Duration experiment_duration;
    private final Random random;
    private final ConcurrentHashMap<String, Integer> peers_per_host;
    private int lookup_retry_count = DEFAULT_LOOKUP_RETRY_COUNT;

    protected Scenario(String name, long master_seed, final Duration experiment_duration) {

        this.name = name;
        this.master_seed = master_seed;
        this.experiment_duration = experiment_duration;
        random = new Random(master_seed);
        peers_per_host = new ConcurrentHashMap<String, Integer>();
    }

    public Set<String> getHostNames() {

        return new CopyOnWriteArraySet<String>(peers_per_host.keySet());
    }

    public Integer setPeersPerHost(String host, Integer peer_count) {

        return peers_per_host.putIfAbsent(host, peer_count);
    }

    public void setPeersPerHosts(Collection<String> hosts, Integer peer_count) {

        for (String host : hosts) {
            peers_per_host.putIfAbsent(host, peer_count);
        }
    }

    public final int getMaximumNetworkSize() {

        int max_network_size = 0;
        for (Integer count : peers_per_host.values()) {

            max_network_size += count;
        }
        return max_network_size;
    }

    public Duration getExperimentDuration() {

        return experiment_duration;
    }

    public Integer getMaximumPeersOnHost(final String host) {

        return peers_per_host.get(host);
    }

    public final long getExperimentDurationInNanos() {

        return getExperimentDuration().getLength(TimeUnit.NANOSECONDS);
    }

    public void setLookupRetryCount(int lookup_retry_count) {

        this.lookup_retry_count = lookup_retry_count;
    }

    public int getLookupRetryCount() {

        return lookup_retry_count;
    }

    public String getName() {

        return name;
    }

    protected long getMasterSeed() {

        return master_seed;
    }

    protected long generateSeed() {

        synchronized (random) {
            return random.nextLong();
        }
    }

    Participant newParticipantOnHost(String host) {

        return null;
    }
}
