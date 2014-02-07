package uk.ac.standrews.cs.trombone.evaluation.analysis;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class LookupIncorrectnessRetryCountAnalyzer extends XYCsvAnalyzer.Sampler {

    public static final String LOOKUP_INCORRECTNESS_RETRY_COUNT_SAMPLER_CSV = "/lookup_incorrectness_retry_count_sampler.csv";

    public LookupIncorrectnessRetryCountAnalyzer(Collection<Path> csv_repetitions) {

        super("lookup_incorrectness_retry_count_sampler", csv_repetitions);
        setYAxisLabel("Incorrect Lookup Retry Count");
        setChartTitle("Incorrect Lookup Retry Count");
    }

    public LookupIncorrectnessRetryCountAnalyzer(final ScenarioAnalyzer scenario_analyzer) throws IOException {

        this(scenario_analyzer.getCsvsByName(LOOKUP_INCORRECTNESS_RETRY_COUNT_SAMPLER_CSV));
    }
}
