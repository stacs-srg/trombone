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

import java.net.InetSocketAddress;
import uk.ac.standrews.cs.shabdiz.AbstractApplicationManager;
import uk.ac.standrews.cs.shabdiz.ApplicationDescriptor;
import uk.ac.standrews.cs.shabdiz.job.Job;
import uk.ac.standrews.cs.shabdiz.job.Worker;
import uk.ac.standrews.cs.shabdiz.job.util.SerializableVoid;
import uk.ac.standrews.cs.trombone.PeerFactory;
import uk.ac.standrews.cs.trombone.PeerReference;
import uk.ac.standrews.cs.trombone.PeerRemote;

public class ExperimentPeerManager extends AbstractApplicationManager {

    private final Experiment experiment;

    public ExperimentPeerManager(final Experiment experiment) {

        this.experiment = experiment;
    }

    @Override
    public PeerReference deploy(final ApplicationDescriptor descriptor) throws Exception {

        final Worker worker = getApplicationDescriptorWorker(descriptor);
        final InetSocketAddress address = worker.submit(new Job<InetSocketAddress>() {

            @Override
            public InetSocketAddress call() throws Exception {

                // FIXME implement
                return null;
            }
        }).get();

        return PeerFactory.bind(address);
    }

    @Override
    public void kill(final ApplicationDescriptor descriptor) throws Exception {

        final Worker worker = getApplicationDescriptorWorker(descriptor);

        if (worker != null) {
            worker.submit(new Job<SerializableVoid>() {

                @Override
                public SerializableVoid call() throws Exception {

                    //FIXME implement
                    return null;
                }
            }).get();
        }
    }

    private Worker getApplicationDescriptorWorker(final ApplicationDescriptor descriptor) {

        final Worker worker = experiment.getApplicationDescriptorWorker(descriptor);
        if (worker == null) { throw new RuntimeException("no worker found"); }
        return worker;
    }

    @Override
    protected void attemptApplicationCall(final ApplicationDescriptor descriptor) throws Exception {

        final PeerReference reference = descriptor.getApplicationReference();
        final PeerRemote remote = PeerFactory.bind(reference);
        remote.getKey();
    }
}
