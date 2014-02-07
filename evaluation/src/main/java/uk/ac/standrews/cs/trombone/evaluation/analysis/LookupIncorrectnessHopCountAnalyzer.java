package uk.ac.standrews.cs.trombone.evaluation.analysis;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class LookupIncorrectnessHopCountAnalyzer extends XYCsvAnalyzer.Sampler {

    public static final String LOOKUP_INCORRECTNESS_HOP_COUNT_SAMPLER_CSV = "/lookup_incorrectness_hop_count_sampler.csv";

    public LookupIncorrectnessHopCountAnalyzer(Collection<Path> csv_repetitions) {

        super("lookup_incorrectness_hop_count_sampler", csv_repetitions);
        setYAxisLabel("Incorrect Lookup Hop-count");
        setChartTitle("Incorrect Lookup Hop-count");
    }

    public LookupIncorrectnessHopCountAnalyzer(final ScenarioAnalyzer scenario_analyzer) throws IOException {

        this(scenario_analyzer.getCsvsByName(LOOKUP_INCORRECTNESS_HOP_COUNT_SAMPLER_CSV));
    }
}
