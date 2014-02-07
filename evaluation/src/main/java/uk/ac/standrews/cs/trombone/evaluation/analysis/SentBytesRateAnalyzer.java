package uk.ac.standrews.cs.trombone.evaluation.analysis;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class SentBytesRateAnalyzer extends XYCsvAnalyzer.Rate {

    private static final String SENT_BYTES_RATE_CSV = "/sent_bytes_rate.csv";

    public SentBytesRateAnalyzer(Collection<Path> csv_repetitions) {

        super("sent_bytes_rate", csv_repetitions);
        setYAxisLabel("Sent Bytes Rate");
        setChartTitle("Sent Bytes Rate per Second");
    }

    public SentBytesRateAnalyzer(final ScenarioAnalyzer scenario_analyzer) throws IOException {

        this(scenario_analyzer.getCsvsByName(SENT_BYTES_RATE_CSV));
    }
}

