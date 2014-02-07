package uk.ac.standrews.cs.trombone.evaluation.analysis;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class PeerArrivalRateAnalyzer extends XYCsvAnalyzer.Rate {

    public static final String PEER_ARRIVAL_RATE = "/peer_arrival_rate.csv";

    public PeerArrivalRateAnalyzer(Collection<Path> csv_repetitions) {

        super("peer_arrival_rate", csv_repetitions);
        setYAxisLabel("Peer Arrival Rate");
        setChartTitle("Peer Arrival Rate per Second");
    }

    public PeerArrivalRateAnalyzer(final ScenarioAnalyzer scenario_analyzer) throws IOException {

        this(scenario_analyzer.getCsvsByName(PEER_ARRIVAL_RATE));
    }
}

