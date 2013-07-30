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
import java.util.EventObject;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Provider;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import uk.ac.standrews.cs.shabdiz.host.Host;
import uk.ac.standrews.cs.shabdiz.util.Duration;
import uk.ac.standrews.cs.trombone.churn.Churn;
import uk.ac.standrews.cs.trombone.evaluation.provider.PeerProvider;
import uk.ac.standrews.cs.trombone.evaluation.provider.PortNumberProvider;

public abstract class Scenario2 implements Serializable {

    private final long master_seed;
    private final RandomGenerator random;

    protected Scenario2(long master_seed) {

        this.master_seed = master_seed;
        random = newRandomGenerator(master_seed);
    }

    public long getMasterSeed() {

        return master_seed;
    }

    public abstract Map<Long, List<EventObject>> getEvents();

    public abstract Set<Host> getHosts();

    public abstract PortNumberProvider getPortNumberProvider();

    public final int getMaximumNetworkSize() {

        int max_network_size = 0;
        for (Integer count : getMaximumPeersPerHost().values()) {

            max_network_size += count;
        }
        return max_network_size;
    }

    public abstract Duration getExperimentDuration();

    public long generateSeed() {

        synchronized (random) {
            return random.nextLong();
        }
    }

    public abstract Map<Host, Integer> getMaximumPeersPerHost();

    public abstract PeerProvider getPeerProvider();

    public abstract Provider<Churn> getChurnProvider();

    protected RandomGenerator newRandomGenerator(long seed) {

        return new Well19937c(seed);
    }

}
