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

package uk.ac.standrews.cs.trombone.event;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Provider;
import uk.ac.standrews.cs.shabdiz.util.Duration;
import uk.ac.standrews.cs.trombone.core.PeerConfiguration;
import uk.ac.standrews.cs.trombone.core.key.Key;
import uk.ac.standrews.cs.trombone.core.key.KeyProvider;
import uk.ac.standrews.cs.trombone.core.util.Named;
import uk.ac.standrews.cs.trombone.event.churn.Churn;
import uk.ac.standrews.cs.trombone.event.churn.Workload;
import uk.ac.standrews.cs.trombone.event.provider.SequentialPortNumberProvider;

/**
 * Presents an experiment scenario.
 *
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class Scenario implements Named {

    private final String name;
    private final byte[] master_seed;
    private final TreeSet<HostScenario> host_scenarios = new TreeSet<>();
    private Duration experiment_duration;
    private Duration observation_interval;
    private KeyProvider peer_key_provider;
    private int lookup_retry_count;

    /**
     * Instantiates a new Scenario.
     *
     * @param name the name of the scenario
     * @param master_seed the master seed
     */
    public Scenario(String name, byte[] master_seed) {

        this.name = name;
        this.master_seed = master_seed;
    }

    public synchronized void addHost(String host, Integer peer_count, SequentialPortNumberProvider port_number_provider, Churn churn, Workload workload, PeerConfiguration configuration) {

        host_scenarios.add(new HostScenario(host, peer_count, port_number_provider.copy(), churn, workload, configuration));
    }

    public synchronized final int getMaximumNetworkSize() {

        int max_network_size = 0;
        for (HostScenario host_scenario : host_scenarios) {

            max_network_size += host_scenario.peer_count;
        }
        return max_network_size;
    }

    public void setExperimentDuration(final Duration experiment_duration) {

        this.experiment_duration = experiment_duration;
    }

    public void setObservationInterval(final Duration observation_interval) {

        this.observation_interval = observation_interval;
    }

    public Duration getExperimentDuration() {

        return experiment_duration;
    }

    public Duration getObservationInterval() {

        return observation_interval;
    }

    public int getLookupRetryCount() {

        return lookup_retry_count;
    }

    public void setLookupRetryCount(final int lookup_retry_count) {

        this.lookup_retry_count = lookup_retry_count;
    }

    protected final long getExperimentDurationInNanos() {

        return getExperimentDuration().getLength(TimeUnit.NANOSECONDS);
    }

    @Override
    public String getName() {

        return name;
    }

    public Provider<Key> getPeerKeyProvider() {

        return peer_key_provider;
    }

    public void setPeerKeyProvider(final KeyProvider peer_key_provider) {

        this.peer_key_provider = peer_key_provider;
    }

    public byte[] getMasterSeed() {

        return master_seed;
    }

    protected synchronized Set<Participant> getParticipants() {

        final TreeSet<Participant> participants = new TreeSet<>();

        int next_id = 1;
        for (final HostScenario host_scenario : host_scenarios) {
            for (int i = 0; i < host_scenario.peer_count; i++) {

                final Key peer_key = peer_key_provider.get();
                final InetSocketAddress peer_address = InetSocketAddress.createUnresolved(host_scenario.host_name, host_scenario.getNextPort());
                final Participant participant = new Participant(next_id, peer_key, peer_address, host_scenario.churn, host_scenario.workload, host_scenario.configuration);
                participants.add(participant);
                next_id++;
            }
        }

        return participants;
    }

    public synchronized void setHostScenarios(final TreeSet<HostScenario> host_scenarios) {

        this.host_scenarios.clear();
        this.host_scenarios.addAll(host_scenarios);
    }

    public SortedSet<HostScenario> getHostScenarios() {

        return Collections.unmodifiableSortedSet(host_scenarios);
    }

    @Override
    public String toString() {

        return name;
    }

    public static class HostScenario implements Comparable<HostScenario> {

        private static final AtomicInteger NEXT_ID = new AtomicInteger();
        private final String host_name;
        private final int peer_count;
        private final SequentialPortNumberProvider port_number_provider;
        private final Churn churn;
        private final Workload workload;
        private final PeerConfiguration configuration;
        private final int id;

        public String getHostName() {

            return host_name;
        }

        public int getPeerCount() {

            return peer_count;
        }

        public SequentialPortNumberProvider getPort_number_provider() {

            return port_number_provider;
        }

        public Churn getChurn() {

            return churn;
        }

        public Workload getWorkload() {

            return workload;
        }

        public PeerConfiguration getConfiguration() {

            return configuration;
        }

        public HostScenario(String host_name, final int peer_count, final SequentialPortNumberProvider port_number_provider, final Churn churn, final Workload workload, final PeerConfiguration configuration) {

            Objects.requireNonNull(host_name);
            Objects.requireNonNull(port_number_provider);
            Objects.requireNonNull(churn);
            Objects.requireNonNull(workload);
            Objects.requireNonNull(configuration, "configuration must not be null");

            id = NEXT_ID.incrementAndGet();
            this.host_name = host_name;
            this.peer_count = peer_count;
            this.port_number_provider = port_number_provider;
            this.churn = churn;
            this.workload = workload;
            this.configuration = configuration;
        }

        @Override
        public int compareTo(final HostScenario other) {

            final int name_comparison = host_name.compareTo(other.host_name);
            final int peer_count_comparison = Integer.compare(peer_count, other.peer_count);
            return name_comparison != 0 ? name_comparison : peer_count_comparison != 0 ? peer_count_comparison : Integer.compare(id, other.id);
        }

        synchronized int getNextPort() {

            return port_number_provider.get();
        }

        @Override
        public int hashCode() {

            int result = host_name.hashCode();
            result = 31 * result + peer_count;
            result = 31 * result + port_number_provider.hashCode();
            result = 31 * result + churn.hashCode();
            result = 31 * result + workload.hashCode();
            result = 31 * result + configuration.hashCode();
            result = 31 * result + id;
            return result;
        }

        @Override
        public boolean equals(final Object o) {

            if (this == o) { return true; }
            if (!(o instanceof HostScenario)) { return false; }

            final HostScenario that = (HostScenario) o;

            if (id != that.id) { return false; }
            if (peer_count != that.peer_count) { return false; }
            if (!churn.equals(that.churn)) { return false; }
            if (!configuration.equals(that.configuration)) { return false; }
            if (!host_name.equals(that.host_name)) { return false; }
            if (!port_number_provider.equals(that.port_number_provider)) { return false; }
            if (!workload.equals(that.workload)) { return false; }

            return true;
        }

        @Override
        public String toString() {

            final StringBuilder sb = new StringBuilder("HostScenario{");
            sb.append("host_name='").append(host_name).append('\'');
            sb.append(", churn=").append(churn);
            sb.append(", workload=").append(workload);
            sb.append(", configuration=").append(configuration);
            sb.append(", peer_count=").append(peer_count);
            sb.append(", port_number_provider=").append(port_number_provider);
            sb.append('}');
            return sb.toString();
        }
    }
}
