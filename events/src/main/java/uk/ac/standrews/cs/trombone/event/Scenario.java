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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import org.json.JSONObject;
import uk.ac.standrews.cs.shabdiz.util.Duration;
import uk.ac.standrews.cs.trombone.core.PeerConfiguration;
import uk.ac.standrews.cs.trombone.core.key.Key;
import uk.ac.standrews.cs.trombone.core.key.KeySupplier;
import uk.ac.standrews.cs.trombone.event.environment.Churn;
import uk.ac.standrews.cs.trombone.event.environment.Workload;
import uk.ac.standrews.cs.trombone.event.util.SequentialPortNumberSupplier;

/**
 * Presents an experiment scenario.
 *
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */

public class Scenario {

    private final AtomicInteger next_host_scenario_id = new AtomicInteger();
    private final AtomicInteger next_host_index;
    private final String name;
    private final long master_seed;
    private final TreeSet<HostScenario> host_scenarios = new TreeSet<>();
    private final Map<Integer, String> host_name_indices = new TreeMap<>();
    private Duration experiment_duration;
    private Duration observation_interval;
    private KeySupplier peer_key_supplier;
    private int lookup_retry_count;

    /**
     * Constructs a copy of the given scenario.
     *
     * @param scenario the scenario from which to construct a copy
     */
    public Scenario(Scenario scenario) {

        this(scenario.name, scenario.master_seed);
        scenario.setExperimentDuration(experiment_duration);
        scenario.setObservationInterval(observation_interval);
        scenario.setLookupRetryCount(lookup_retry_count);
        scenario.setPeerKeyProvider(peer_key_supplier);
        scenario.setObservationInterval(observation_interval);
        for (HostScenario host_scenario : host_scenarios) {
            scenario.addHost(host_scenario.getHostName(), host_scenario.getPeerCount(), host_scenario.getPort_number_provider(), host_scenario.getChurn(), host_scenario.getWorkload(), host_scenario.getConfiguration());
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

    public synchronized void addHost(String host, Integer peer_count, SequentialPortNumberSupplier port_number_provider, Churn churn, Workload workload, PeerConfiguration configuration) {

        final int host_index = getHostIndex(host);
        host_scenarios.add(new HostScenario(host_index, peer_count, port_number_provider.copy(), churn, workload, configuration));
    }

    public static void main(String[] args) throws IOException {

        final Scenario scenario = new Scenario("scenario", 123);

        scenario.addHost("host1", 100, new SequentialPortNumberSupplier(4500), Churn.NONE, Workload.NONE, null);

        new ObjectMapper(new JsonFactory()).writerWithDefaultPrettyPrinter()
                .writeValue(System.out, scenario);
    }

    public String getName() {

        return name;
    }

    public void substituteHostNames(Map<Integer, String> substitutes) {

        for (Map.Entry<Integer, String> entry : host_name_indices.entrySet()) {
            final Integer key = entry.getKey();
            if (substitutes.containsKey(key)) {
                final String substitute_host_name = substitutes.get(key);
                entry.setValue(substitute_host_name);
            }
        }
    }

    public HashMap<Integer, String> getHostIndices() {

        return new HashMap<>(host_name_indices);
    }

    private synchronized int getHostIndex(final String host_name) {

        final int index;

        for (Map.Entry<Integer, String> entry : host_name_indices.entrySet()) {
            if (host_name.equals(entry.getValue())) {
                return entry.getKey();
            }
        }

        index = next_host_index.getAndIncrement();
        host_name_indices.put(index, host_name);
        return index;
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

    public Supplier<Key> getPeerKeyProvider() {

        return peer_key_supplier;
    }

    public void setPeerKeyProvider(final KeySupplier peer_key_provider) {

        this.peer_key_supplier = peer_key_provider;
    }

    public long getMasterSeed() {

        return master_seed;
    }

    protected synchronized Set<Participant> getParticipants() {

        final TreeSet<Participant> participants = new TreeSet<>();

        int next_id = 1;
        for (final HostScenario host_scenario : host_scenarios) {
            for (int i = 0; i < host_scenario.peer_count; i++) {

                final Key peer_key = peer_key_supplier.get();
                final InetSocketAddress peer_address = new InetSocketAddress(host_scenario.getHostName(), host_scenario.getNextPort());
                final Participant participant = new Participant(next_id, peer_key, peer_address, host_scenario.churn, host_scenario.workload, host_scenario.configuration);
                participant.setHostIndex(host_scenario.host_index);
                participants.add(participant);
                next_id++;
            }
        }

        return participants;
    }

    public SortedSet<HostScenario> getHostScenarios() {

        return Collections.unmodifiableSortedSet(host_scenarios);
    }

    @Override
    public String toString() {

        return name;
    }

    public JSONObject toJson() {

        return new JSONObject(this);
    }

    public class HostScenario implements Comparable<HostScenario> {

        private final int peer_count;
        private final SequentialPortNumberSupplier port_number_provider;
        private final Churn churn;
        private final Workload workload;
        private final PeerConfiguration configuration;
        private final Integer id;
        private final Integer host_index;

        public String getHostName() {

            return host_name_indices.get(host_index);
        }

        public int getPeerCount() {

            return peer_count;
        }

        public SequentialPortNumberSupplier getPort_number_provider() {

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

        private HostScenario(Integer host_index, final int peer_count, final SequentialPortNumberSupplier port_number_provider, final Churn churn, final Workload workload, final PeerConfiguration configuration) {

            Objects.requireNonNull(host_index);
            Objects.requireNonNull(port_number_provider);
            Objects.requireNonNull(churn);
            Objects.requireNonNull(workload);
            //            Objects.requireNonNull(configuration, "configuration must not be null");

            id = next_host_scenario_id.incrementAndGet();
            this.host_index = host_index;
            this.peer_count = peer_count;
            this.port_number_provider = port_number_provider;
            this.churn = churn;
            this.workload = workload;
            this.configuration = configuration;
        }

        @Override
        public int compareTo(final HostScenario other) {

            return id.compareTo(other.id);
        }

        synchronized int getNextPort() {

            return port_number_provider.get();
        }

        @Override
        public int hashCode() {

            return id.hashCode();
        }

        @Override
        public boolean equals(final Object o) {

            if (this == o) { return true; }
            if (!(o instanceof HostScenario)) { return false; }
            final HostScenario that = (HostScenario) o;
            return id.equals(that.id) && getHostName().equals(that.getHostName());
        }

        @Override
        public String toString() {

            return "HostScenario{" + "host_index='" + host_index + '\'' + ", churn=" + churn + ", workload=" + workload + ", configuration=" + configuration + ", peer_count=" + peer_count + ", port_number_provider=" + port_number_provider + '}';
        }
    }
}
