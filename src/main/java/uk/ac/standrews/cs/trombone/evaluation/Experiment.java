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

import com.google.inject.Guice;
import com.google.inject.Injector;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import javax.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.shabdiz.ApplicationDescriptor;
import uk.ac.standrews.cs.shabdiz.ApplicationState;
import uk.ac.standrews.cs.shabdiz.host.Host;
import uk.ac.standrews.cs.shabdiz.job.Worker;
import uk.ac.standrews.cs.shabdiz.job.WorkerNetwork;
import uk.ac.standrews.cs.shabdiz.util.Duration;
import uk.ac.standrews.cs.trombone.evaluation.job.MultiplePeerConductorJob;
import uk.ac.standrews.cs.trombone.evaluation.provider.PeerConductorProvider;
import uk.ac.standrews.cs.trombone.util.TimeoutSupport;

public class Experiment implements Callable<File> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Experiment.class);
    private final String name;
    private final Scenario scenario;
    private final ExperimentPeerManager manager;
    private final WorkerNetwork worker_network;
    private final Map<Host, Worker> host_worker_map;
    private final MembershipServiceExposer membership_service_exposer;
    private final Injector injector;
    private final InetSocketAddress membership_service_address;
    private final TimeoutSupport timing;

    public Experiment(String name, final Scenario scenario) throws IOException {

        this.name = name;
        this.scenario = scenario;
        injector = Guice.createInjector(scenario);
        timing = new TimeoutSupport();

        membership_service_exposer = new MembershipServiceExposer(scenario.getMembershipService());
        membership_service_exposer.expose();
        membership_service_address = membership_service_exposer.getAddress();

        manager = new ExperimentPeerManager(this);
        worker_network = new WorkerNetwork();
        host_worker_map = new HashMap<Host, Worker>();
    }

    @Override
    public synchronized File call() {

        try {
            setUp();
            timing.startCountdown();
            deployPeerNetwork();
            awaitCompletion();
        }
        catch (final Exception e) {
            LOGGER.error("error occured while waiting for experiment to compelete", e);
        }
        finally {
            tearDown();
        }

        return null;
    }

    public Worker getApplicationDescriptorWorker(final ApplicationDescriptor descriptor) {

        final Host host = descriptor.getHost();
        return host_worker_map.get(host);
    }

    public String getName() {

        return name;
    }

    public Scenario getScenario() {

        return scenario;
    }

    private void awaitCompletion() throws InterruptedException {

        timing.awaitTimeout();
    }

    private void setUp() throws Exception {

        deployWorkerNetwork();
    }

    private void tearDown() {

        worker_network.shutdown();
    }

    private void deployPeerNetwork() throws Exception {

        final Map<Host, Integer> peers_per_host = countPeersPerHost();
        final Provider<PeerConductorProvider> conductor_provider = scenario.getPeerConductorProvider();

        for (Map.Entry<Host, Integer> entry : peers_per_host.entrySet()) {

            final Host host = entry.getKey();
            final Integer peer_count = entry.getValue();
            final MultiplePeerConductorJob conductor_job = new MultiplePeerConductorJob(conductor_provider.get(), membership_service_address, timing.getRemainingTime(), peer_count, new Duration(5, TimeUnit.SECONDS));
            final Worker worker = host_worker_map.get(host);
            worker.submit(conductor_job);
        }

    }

    private Map<Host, Integer> countPeersPerHost() {

        Map<Host, Integer> peers_per_host = new HashMap<Host, Integer>();

        final List<Host> hosts = scenario.getHosts();

        for (Host host : scenario.getUniqueHosts()) {
            int i = 0;
            for (Host h : hosts) {
                if (h.equals(host)) {
                    i++;
                }
            }
            peers_per_host.put(host, i);
        }
        return peers_per_host;
    }

    private void deployWorkerNetwork() throws Exception {

        //TODO do concurrently
        final Set<Host> unique_hosts = scenario.getUniqueHosts();

        for (Host host : unique_hosts) {
            worker_network.add(host);
        }
        worker_network.deployAll();
        worker_network.awaitAnyOfStates(ApplicationState.RUNNING);
        worker_network.setScanEnabled(false);

        for (ApplicationDescriptor descriptor : worker_network) {
            final Host host = descriptor.getHost();
            final Worker worker = descriptor.getApplicationReference();
            host_worker_map.put(host, worker);
        }
    }
}
