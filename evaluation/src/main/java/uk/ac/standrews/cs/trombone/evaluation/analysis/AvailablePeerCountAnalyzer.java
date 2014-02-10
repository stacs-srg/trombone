package uk.ac.standrews.cs.trombone.evaluation.analysis;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class AvailablePeerCountAnalyzer extends XYCsvAnalyzer.Counter {

    private static final String AVAILABLE_PEER_COUNTER_CSV = "/available_peer_counter.csv";

    public AvailablePeerCountAnalyzer(final Collection<Path> csv_files) {

        super("available_peer_count", csv_files);
        setYAxisLabel("Number of Available Peers");
        setChartTitle("Number of Available Peers");
    }

    public AvailablePeerCountAnalyzer(final ScenarioAnalyzer scenario_analyzer) throws IOException {

        this(scenario_analyzer.getCsvsByName(AVAILABLE_PEER_COUNTER_CSV));
    }

}
