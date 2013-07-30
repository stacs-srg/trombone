package uk.ac.standrews.cs.trombone.evaluation.job;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import java.io.File;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import org.apache.commons.io.FileUtils;
import uk.ac.standrews.cs.shabdiz.job.Job;
import uk.ac.standrews.cs.shabdiz.util.Duration;
import uk.ac.standrews.cs.trombone.evaluation.MembershipServiceExposer;
import uk.ac.standrews.cs.trombone.evaluation.PeerConductor;
import uk.ac.standrews.cs.trombone.evaluation.membership.MembershipService;
import uk.ac.standrews.cs.trombone.evaluation.provider.PeerConductorProvider;
import uk.ac.standrews.cs.trombone.metric.LookupFailureRateMeter;
import uk.ac.standrews.cs.trombone.metric.LookupSuccessDelayTimer;
import uk.ac.standrews.cs.trombone.metric.LookupSuccessHopCountHistogram;
import uk.ac.standrews.cs.trombone.metric.LookupSuccessRetryCountHistogram;
import uk.ac.standrews.cs.trombone.metric.PeerExposureChangeMeter;
import uk.ac.standrews.cs.trombone.metric.PeerMembershipChangeMeter;
import uk.ac.standrews.cs.trombone.metric.PeerStateSizeGauge;
import uk.ac.standrews.cs.trombone.metric.SentBytesMeter;
import uk.ac.standrews.cs.trombone.metric.core.CsvReporter;
import uk.ac.standrews.cs.trombone.metric.core.MetricRegistry;
import uk.ac.standrews.cs.trombone.util.TimeoutSupport;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class MultiplePeerConductorJob implements Job<File> {

    private static final long serialVersionUID = -4494691063017253898L;
    private final PeerConductorProvider conductor_provider;
    private final TimeoutSupport timing;
    private final InetSocketAddress membership_service_address;
    private final int peer_count;
    private final Duration report_interval;
    private final Set<ListenableFuture<?>> scheduled_conductors;

    public MultiplePeerConductorJob(PeerConductorProvider conductor_provider, InetSocketAddress membership_service_address, Duration timeout, int peer_count, Duration report_interval) {

        this.conductor_provider = conductor_provider;
        this.membership_service_address = membership_service_address;
        this.peer_count = peer_count;
        this.report_interval = report_interval;
        timing = new TimeoutSupport(timeout);
        scheduled_conductors = new HashSet<ListenableFuture<?>>();
    }

    @Override
    public File call() throws Exception {

        final MetricRegistry registry = new MetricRegistry("registry");
        registry.register("lookup.failure.rate", LookupFailureRateMeter.getGlobal());
        registry.register("lookup.success.delay", LookupSuccessDelayTimer.getGlobal());
        registry.register("lookup.success.hop_count", LookupSuccessHopCountHistogram.getGlobal());
        registry.register("lookup.success.retry_count", LookupSuccessRetryCountHistogram.getGlobal());
        registry.register("peer.exposed_count", PeerExposureChangeMeter.getGlobalExposedPeersCounter());
        registry.register("peer.arrival", PeerMembershipChangeMeter.getGlobalPeerArrivalMeter());
        registry.register("peer.departure", PeerMembershipChangeMeter.getGlobalDeparturesMeter());
        registry.register("peer.arrived_count", PeerMembershipChangeMeter.getArrivedPeersCounter());
        registry.register("peer.state_size", PeerStateSizeGauge.getTotalPeerStateSizeHistogram());
        registry.register("bytes.sent.per_exposed_peer", SentBytesMeter.getSentBytesPerExposedPeerGauge());
        registry.register("bytes.sent.total", SentBytesMeter.getTotalSentBytesMeter());

        final File test = new File("/Users/masih/Desktop", "test");
        FileUtils.forceMkdir(test);
        final CsvReporter reporter = new CsvReporter(registry, test);

        reporter.start(report_interval.getLength(), report_interval.getTimeUnit());
        final ListeningExecutorService executor = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
        try {
            timing.startCountdown();
            conductPeers(executor);
            timing.awaitTimeout();
        }
        finally {
            reporter.stop();
            executor.shutdownNow();
            cancelScheduledConductors();
        }
        return test;
    }

    private void conductPeers(final ListeningExecutorService executor) {

        final MembershipService membership_service = MembershipServiceExposer.bind(membership_service_address);
        for (int i = 0; i < peer_count; i++) {
            executeNewConductor(executor, membership_service);
        }
    }

    private void cancelScheduledConductors() {

        for (final ListenableFuture<?> pending_conductor : scheduled_conductors) {
            pending_conductor.cancel(true);
        }
        scheduled_conductors.clear();
    }

    private void executeNewConductor(final ListeningExecutorService executor, final MembershipService membership_service) {

        final PeerConductor conductor = conductor_provider.get();
        conductor.setMembershipService(membership_service);
        conductor.setTimeout(timing.getRemainingTime());
        final ListenableFuture<?> submit = executor.submit(conductor);
        scheduled_conductors.add(submit);

        Futures.addCallback(submit, new FutureCallback<Object>() {

            @Override
            public void onSuccess(final Object result) {

                //
                //                if (!timing.isTimedOut()) {
                //                    executeNewConductor(executor, membership_service);
                //                }
            }

            @Override
            public void onFailure(final Throwable t) {

                t.printStackTrace();
            }
        }, executor);
    }
}
