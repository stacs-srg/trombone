package uk.ac.standrews.cs.trombone.evaluation.job;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import java.io.File;
import java.util.concurrent.Executors;
import org.apache.commons.io.FileUtils;
import uk.ac.standrews.cs.shabdiz.job.Job;
import uk.ac.standrews.cs.shabdiz.util.Duration;
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
import uk.ac.standrews.cs.trombone.metric.core.Sampler;
import uk.ac.standrews.cs.trombone.util.TimeoutSupport;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class ExperimentEventExecutorJob implements Job<File> {

    private static final long serialVersionUID = -4494691063017253898L;
    private final TimeoutSupport timing;
    private final Duration report_interval;
    Sampler event_execution_lag_metric = new Sampler();

    public ExperimentEventExecutorJob(Duration report_interval, File a) {

        this.report_interval = report_interval;
        timing = new TimeoutSupport();
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
        registry.register("event.execution.lag", event_execution_lag_metric);

        final File test = new File("/Users/masih/Desktop", "test");
        FileUtils.forceMkdir(test);
        final CsvReporter reporter = new CsvReporter(registry, test);

        reporter.start(report_interval.getLength(), report_interval.getTimeUnit());
        final ListeningExecutorService executor = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
        try {
            timing.startCountdown();
            executeEvents(executor);
            timing.awaitTimeout();
        }
        finally {
            reporter.stop();
            executor.shutdownNow();
        }
        return test;
    }

    private void executeEvents(final ListeningExecutorService executor) {

    }
}
