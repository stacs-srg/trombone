package uk.ac.standrews.cs.trombone.evaluation.analysis;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class LookupIncorrectnessRateAnalyzer extends XYCsvAnalyzer.Rate {

    private static final String LOOKUP_INCORRECTNESS_RATE_CSV = "/lookup_incorrectness_rate.csv";

    public LookupIncorrectnessRateAnalyzer(Collection<Path> csv_repetitions) {

        super("lookup_incorrectness_rate", csv_repetitions);
        setYAxisLabel("Incorrect Lookups Rate");
        setChartTitle("Incorrect Lookups per Second");
    }

    public LookupIncorrectnessRateAnalyzer(final ScenarioAnalyzer scenario_analyzer) throws IOException {

        this(scenario_analyzer.getCsvsByName(LOOKUP_INCORRECTNESS_RATE_CSV));
    }
}

