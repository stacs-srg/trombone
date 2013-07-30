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

import com.google.inject.AbstractModule;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.inject.Provider;
import uk.ac.standrews.cs.shabdiz.host.Host;
import uk.ac.standrews.cs.trombone.Peer;
import uk.ac.standrews.cs.trombone.churn.Churn;
import uk.ac.standrews.cs.trombone.evaluation.membership.MembershipService;
import uk.ac.standrews.cs.trombone.evaluation.provider.PeerConductorProvider;
import uk.ac.standrews.cs.trombone.evaluation.provider.SerializableProvider;
import uk.ac.standrews.cs.trombone.workload.Workload;

public abstract class Scenario extends AbstractModule {

    private List<Host> hosts;
    private Provider<PeerConductorProvider> conductor_provider;

    protected Scenario() {

        hosts = new ArrayList<Host>();
        conductor_provider = new Provider<PeerConductorProvider>() {

            @Override
            public PeerConductorProvider get() {

                return new PeerConductorProvider(getPeerProvider().get(), getChurnProvider().get(), getWorkloadProvider().get());
            }
        };
    }

    public boolean add(Host host) {

        return hosts.add(host);
    }

    public List<Host> getHosts() {

        return new CopyOnWriteArrayList<Host>(hosts);
    }

    public Set<Host> getUniqueHosts() {

        return new HashSet<Host>(hosts);
    }

    @Override
    protected void configure() {

    }

    Provider<PeerConductorProvider> getPeerConductorProvider() {

        return conductor_provider;
    }

    abstract MembershipService getMembershipService();

    abstract Provider<SerializableProvider<Long>> getSeedProvider();

    abstract Provider<SerializableProvider<Churn>> getChurnProvider();

    abstract Provider<SerializableProvider<Workload>> getWorkloadProvider();

    abstract Provider<SerializableProvider<Peer>> getPeerProvider();

}
