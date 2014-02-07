package uk.ac.standrews.cs.trombone.evaluation.analysis;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class PeerDepartureRateAnalyzer extends XYCsvAnalyzer.Rate {

    public static final String PEER_DEPARTURE_RATE_CSV = "/peer_departure_rate.csv";

    public PeerDepartureRateAnalyzer(Collection<Path> csv_repetitions) {

        super("peer_departure_rate", csv_repetitions);
        setYAxisLabel("Peer Departure Rate");
        setChartTitle("Peer Departure Rate per Second");
    }

    public PeerDepartureRateAnalyzer(final ScenarioAnalyzer scenario_analyzer) throws IOException {

        this(scenario_analyzer.getCsvsByName(PEER_DEPARTURE_RATE_CSV));
    }
}

