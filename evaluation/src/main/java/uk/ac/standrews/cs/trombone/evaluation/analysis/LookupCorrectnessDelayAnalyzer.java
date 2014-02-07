package uk.ac.standrews.cs.trombone.evaluation.analysis;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class LookupCorrectnessDelayAnalyzer extends XYCsvAnalyzer.Timer {

    private static final String LOOKUP_CORRECTNESS_DELAY_TIMER_CSV = "/lookup_correctness_delay_timer.csv";

    public LookupCorrectnessDelayAnalyzer(Collection<Path> csv_repetitions) {

        super("lookup_correctness_delay_timer", csv_repetitions);
        setYAxisLabel("Correct Lookup Delays");
        setChartTitle("Correct Lookup Delays (nanoseconds)");
    }

    public LookupCorrectnessDelayAnalyzer(final ScenarioAnalyzer scenario_analyzer) throws IOException {

        this(scenario_analyzer.getCsvsByName(LOOKUP_CORRECTNESS_DELAY_TIMER_CSV));
    }
}
