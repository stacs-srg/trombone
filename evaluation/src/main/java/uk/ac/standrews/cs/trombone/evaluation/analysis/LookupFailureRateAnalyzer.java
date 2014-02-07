package uk.ac.standrews.cs.trombone.evaluation.analysis;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class LookupFailureRateAnalyzer extends XYCsvAnalyzer.Rate {

    private static final String LOOKUP_FAILURE_RATE_CSV = "/lookup_failure_rate.csv";

    public LookupFailureRateAnalyzer(Collection<Path> csv_repetitions) {

        super("lookup_failure_rate", csv_repetitions);
        setYAxisLabel("Lookup Failure Rate");
        setChartTitle("Lookup Failure Rate per Second");
    }

    public LookupFailureRateAnalyzer(final ScenarioAnalyzer scenario_analyzer) throws IOException {

        this(scenario_analyzer.getCsvsByName(LOOKUP_FAILURE_RATE_CSV));
    }
}
