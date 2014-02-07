package uk.ac.standrews.cs.trombone.evaluation.analysis;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class LookupCorrectnessHopCountAnalyzer extends XYCsvAnalyzer.Sampler {

    public static final String LOOKUP_CORRECTNESS_HOP_COUNT_SAMPLER_CSV = "/lookup_correctness_hop_count_sampler.csv";

    public LookupCorrectnessHopCountAnalyzer(Collection<Path> csv_repetitions) {

        super("lookup_correctness_hop_count_sampler", csv_repetitions);
        setYAxisLabel("Correct Lookup Hop-count");
        setChartTitle("Correct Lookup Hop-count");
    }

    public LookupCorrectnessHopCountAnalyzer(final ScenarioAnalyzer scenario_analyzer) throws IOException {

        this(scenario_analyzer.getCsvsByName(LOOKUP_CORRECTNESS_HOP_COUNT_SAMPLER_CSV));
    }
}
