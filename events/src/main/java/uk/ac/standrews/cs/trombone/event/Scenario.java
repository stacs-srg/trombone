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
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import javax.inject.Provider;
import uk.ac.standrews.cs.shabdiz.util.Duration;
import uk.ac.standrews.cs.trombone.core.PeerConfigurator;
import uk.ac.standrews.cs.trombone.core.key.Key;
import uk.ac.standrews.cs.trombone.event.churn.Churn;
import uk.ac.standrews.cs.trombone.event.provider.PortNumberProvider;
import uk.ac.standrews.cs.trombone.event.workload.Workload;

public class Scenario {

    private final String name;
    private final long master_seed;
    private final Random random;
    private final ConcurrentHashMap<String, HostScenario> host_scenarios;
    private Duration experiment_duration;
    private Provider<Key> peer_key_provider;
    private Provider<Churn> churn_provider;
    private Provider<Workload> workload_provider;
    private PeerConfigurator configurator;

    public Scenario(String name, long master_seed) {

        this.name = name;
        this.master_seed = master_seed;
        random = new Random(master_seed);
        host_scenarios = new ConcurrentHashMap<>();
    }

    public Set<String> getHostNames() {

        return new CopyOnWriteArraySet<String>(host_scenarios.keySet());
    }

    public void addHost(String host, Integer peer_count, PortNumberProvider port_number_provider) {

        final HostScenario host_scenario = new HostScenario(host, peer_count, port_number_provider.copy());
        host_scenarios.put(host, host_scenario);
    }

    public final int getMaximumNetworkSize() {

        int max_network_size = 0;
        for (HostScenario host_scenario : host_scenarios.values()) {

            max_network_size += host_scenario.peer_count;
        }
        return max_network_size;
    }

    public void setExperimentDuration(final Duration experiment_duration) {

        this.experiment_duration = experiment_duration;
    }

    public Duration getExperimentDuration() {

        return experiment_duration;
    }

    public Integer getMaximumPeersOnHost(final String host) {

        return host_scenarios.get(host).peer_count;
    }

    public final long getExperimentDurationInNanos() {

        return getExperimentDuration().getLength(TimeUnit.NANOSECONDS);
    }

    public String getName() {

        return name;
    }

    public Provider<Key> getPeerKeyProvider() {

        return peer_key_provider;
    }

    public void setPeerKeyProvider(final Provider<Key> peer_key_provider) {

        this.peer_key_provider = peer_key_provider;
    }

    public Provider<Churn> getChurnProvider() {

        return churn_provider;
    }

    public void setChurnProvider(final Provider<Churn> churn_provider) {

        this.churn_provider = churn_provider;
    }

    public Provider<Workload> getWorkloadProvider() {

        return workload_provider;
    }

    public void setWorkloadProvider(final Provider<Workload> workload_provider) {

        this.workload_provider = workload_provider;
    }

    public PeerConfigurator getPeerConfigurator() {

        return configurator;
    }

    public void setPeerConfigurator(final PeerConfigurator configurator) {

        this.configurator = configurator;
    }

    public long generateSeed() {

        synchronized (random) {
            return random.nextLong();
        }
    }

    protected long getMasterSeed() {

        return master_seed;
    }

    Participant newParticipantOnHost(String host) {

        return new Participant(peer_key_provider.get(), new InetSocketAddress(host, nextPortByHost(host)), churn_provider.get(), workload_provider.get(), configurator);
    }

    private Integer nextPortByHost(final String host) {

        return host_scenarios.get(host).getNextPort();
    }

    private static class HostScenario {

        private final String host_name;
        private final int peer_count;
        private final PortNumberProvider port_number_provider;

        private HostScenario(String host_name, final Integer peer_count, final PortNumberProvider port_number_provider) {

            this.host_name = host_name;
            this.peer_count = peer_count;
            this.port_number_provider = port_number_provider;
        }

        synchronized int getNextPort() {

            return port_number_provider.get();
        }
    }
}
