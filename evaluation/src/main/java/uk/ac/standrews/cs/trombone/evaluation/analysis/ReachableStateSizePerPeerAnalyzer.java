package uk.ac.standrews.cs.trombone.evaluation.analysis;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class ReachableStateSizePerPeerAnalyzer extends XYCsvAnalyzer.Counter {

    public static final String REACHABLE_STATE_SIZE_PER_ALIVE_PEER_GAUGE_CSV = "/reachable_state_size_per_alive_peer_gauge.csv";

    public ReachableStateSizePerPeerAnalyzer(final Collection<Path> csv_files) {

        super("reachable_state_size_per_alive_peer_gauge", csv_files);
        setYAxisLabel("Reachable State Size Per Peer");
        setChartTitle("Reachable State Size Per Peer");
    }

    public ReachableStateSizePerPeerAnalyzer(final ScenarioAnalyzer scenario_analyzer) throws IOException {

        this(scenario_analyzer.getCsvsByName(REACHABLE_STATE_SIZE_PER_ALIVE_PEER_GAUGE_CSV));
    }
}
