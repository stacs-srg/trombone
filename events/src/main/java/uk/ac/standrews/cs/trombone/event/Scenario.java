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

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import uk.ac.standrews.cs.shabdiz.util.Duration;
import uk.ac.standrews.cs.trombone.core.Key;
import uk.ac.standrews.cs.trombone.core.PeerConfiguration;
import uk.ac.standrews.cs.trombone.event.environment.Churn;
import uk.ac.standrews.cs.trombone.event.environment.RandomKeySupplier;
import uk.ac.standrews.cs.trombone.event.environment.Workload;

/**
 * Presents an experiment scenario.
 *
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class Scenario {

    private static final RuntimeMXBean RUNTIME_MX_BEAN = ManagementFactory.getRuntimeMXBean();

    private final AtomicInteger next_host_index;
    private final String name;
    private final long master_seed;
    private final Map<Integer, HostScenario> host_scenarios = new TreeMap<>();
    private Duration experiment_duration;
    private Duration observation_interval;
    private Supplier<Key> peer_key_supplier;
    private int lookup_retry_count;

    /**
     * Constructs a copy of the given scenario.
     *
     * @param scenario the scenario from which to construct a copy
     */
    public Scenario(Scenario scenario) {

        this(scenario.name, scenario.master_seed);
        experiment_duration = scenario.experiment_duration;
        observation_interval = scenario.observation_interval;
        lookup_retry_count = scenario.lookup_retry_count;
        peer_key_supplier = scenario.peer_key_supplier;
        observation_interval = scenario.observation_interval;

        for (HostScenario host_scenario : scenario.host_scenarios.values()) {
            addHost(host_scenario.getHostName(), host_scenario.getPeerCount(), host_scenario.getPortNumberSupplier(), host_scenario.getChurn(), host_scenario.getWorkload(), host_scenario.getPeerConfiguration());
        }
    }

    /**
     * Instantiates a new Scenario.
     *
     * @param name the name of the scenario
     * @param master_seed the master seed
     */
    public Scenario(String name, long master_seed) {

        this.name = name;
        this.master_seed = master_seed;
        next_host_index = new AtomicInteger(1);
    }

    public synchronized void addHost(String host_name, Integer peer_count, Supplier<Integer> port_number_provider, Churn churn, Workload workload, PeerConfiguration configuration) {

        final Optional<HostScenario> host_scenario_by_host_name = getByHostName(host_name);
        final int host_index = host_scenario_by_host_name.isPresent() ? host_scenario_by_host_name.get().index : next_host_index.getAndIncrement();

        final HostScenario host_scenario = new HostScenario(host_index);
        host_scenario.setChurn(churn);
        host_scenario.setWorkload(workload);
        host_scenario.setPeerConfiguration(configuration);
        host_scenario.setHostName(host_name);
        host_scenario.setPeerCount(peer_count);
        host_scenario.setPortNumberSupplier(port_number_provider);

        host_scenarios.put(host_index, host_scenario);
    }

    @JsonIgnore
    public HashMap<Integer, String> getHostIndices() {

        HashMap<Integer, String> index_host_name_map = new HashMap<>();

        for (Map.Entry<Integer, HostScenario> entry : host_scenarios.entrySet()) {
            index_host_name_map.put(entry.getKey(), entry.getValue()
                    .getHostName());
        }

        return index_host_name_map;
    }

    public Collection<HostScenario> getHostScenarios() {

        return host_scenarios.values();
    }

    private Optional<HostScenario> getByHostName(final String host) {

        return host_scenarios.values()
                .stream()
                .filter(host_scenario -> host_scenario.getHostName()
                        .equals(host))
                .findFirst();
    }

    public String getName() {

        return name;
    }

    public Properties getSystemProperties() {

        final Properties properties = new Properties();
        properties.putAll(System.getProperties());
        properties.put("jvm.arguments", RUNTIME_MX_BEAN.getInputArguments());
        return properties;
    }

    public void substituteHostNames(Map<Integer, String> substitutes) {

        for (Map.Entry<Integer, String> entry : substitutes.entrySet()) {
            final Integer key = entry.getKey();
            if (host_scenarios.containsKey(key)) {
                final String substitute_host_name = substitutes.get(key);
                host_scenarios.get(key)
                        .setHostName(substitute_host_name);
            }
        }
    }

    public synchronized final int getMaximumNetworkSize() {

        int max_network_size = 0;
        for (HostScenario host_scenario : host_scenarios.values()) {

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

    public Supplier<Key> getPeerKeyProvider() {

        return peer_key_supplier;
    }

    public void setPeerKeyProvider(final RandomKeySupplier peer_key_provider) {

        peer_key_supplier = peer_key_provider;
    }

    public long getMasterSeed() {

        return master_seed;
    }

    protected synchronized Set<Participant> getParticipants() {

        final TreeSet<Participant> participants = new TreeSet<>();

        int next_id = 1;
        for (final HostScenario host_scenario : host_scenarios.values()) {
            for (int i = 0; i < host_scenario.peer_count; i++) {

                final Key peer_key = peer_key_supplier.get();
                final InetSocketAddress peer_address = new InetSocketAddress(host_scenario.getHostName(), host_scenario.getNextPort());
                final Participant participant = new Participant(next_id, peer_key, peer_address, host_scenario.churn, host_scenario.workload, host_scenario.peer_configuration);
                participant.setHostIndex(host_scenario.getIndex());
                participants.add(participant);
                next_id++;
            }
        }

        return participants;
    }

    @Override
    public String toString() {

        return name;
    }

    public static class HostScenario implements Comparable<HostScenario> {

        private final Integer index;
        private int peer_count;
        private Churn churn;
        private Workload workload;
        private PeerConfiguration peer_configuration;
        private Supplier<Integer> port_number_supplier;
        private String host_name;

        public HostScenario(Integer index) {

            this.index = index;
        }

        public String getHostName() {

            return host_name;
        }

        public int getPeerCount() {

            return peer_count;
        }

        public Supplier<Integer> getPortNumberSupplier() {

            return port_number_supplier;
        }

        public Churn getChurn() {

            return churn;
        }

        public Workload getWorkload() {

            return workload;
        }

        public PeerConfiguration getPeerConfiguration() {

            return peer_configuration;
        }

        public void setHostName(final String host_name) {

            this.host_name = host_name;
        }

        public void setPeerCount(final int peer_count) {

            this.peer_count = peer_count;
        }

        public void setPortNumberSupplier(final Supplier<Integer> port_number_supplier) {

            this.port_number_supplier = port_number_supplier;
        }

        public void setChurn(final Churn churn) {

            this.churn = churn;
        }

        public void setWorkload(final Workload workload) {

            this.workload = workload;
        }

        public void setPeerConfiguration(final PeerConfiguration peer_configuration) {

            this.peer_configuration = peer_configuration;
        }

        public Integer getIndex() {

            return index;
        }

        synchronized int getNextPort() {

            return port_number_supplier.get();
        }

        @Override
        public int compareTo(final HostScenario other) {

            return index.compareTo(other.index);
        }

        @Override
        public int hashCode() {

            return index.hashCode();
        }

        @Override
        public boolean equals(final Object o) {

            if (this == o) { return true; }
            if (!(o instanceof HostScenario)) { return false; }
            final HostScenario that = (HostScenario) o;
            return index.equals(that.index) && getHostName().equals(that.getHostName());
        }
    }
}
