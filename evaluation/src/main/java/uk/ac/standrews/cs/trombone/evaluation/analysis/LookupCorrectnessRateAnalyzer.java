package uk.ac.standrews.cs.trombone.evaluation.analysis;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class LookupCorrectnessRateAnalyzer extends XYCsvAnalyzer.Rate {

    private static final String LOOKUP_CORRECTNESS_RATE_CSV = "/lookup_correctness_rate.csv";

    public LookupCorrectnessRateAnalyzer(Collection<Path> csv_repetitions) {

        super("lookup_correctness_rate", csv_repetitions);
        setYAxisLabel("Correct Lookups Rate");
        setChartTitle("Correct Lookups per Second");
    }

    public LookupCorrectnessRateAnalyzer(final ScenarioAnalyzer scenario_analyzer) throws IOException {

        this(scenario_analyzer.getCsvsByName(LOOKUP_CORRECTNESS_RATE_CSV));
    }
}
