package uk.ac.standrews.cs.trombone.evaluation.analysis;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class LookupExecutionRateAnalyzer extends XYCsvAnalyzer.Rate {

    private static final String LOOKUP_EXECUTION_RATE_CSV = "/lookup_execution_rate.csv";

    public LookupExecutionRateAnalyzer(Collection<Path> csv_repetitions) {

        super("lookup_execution_rate", csv_repetitions);
        setYAxisLabel("Lookup Execution Rate");
        setChartTitle("Lookup Execution Rate per Second");
    }

    public LookupExecutionRateAnalyzer(final ScenarioAnalyzer scenario_analyzer) throws IOException {

        this(scenario_analyzer.getCsvsByName(LOOKUP_EXECUTION_RATE_CSV));
    }
}
