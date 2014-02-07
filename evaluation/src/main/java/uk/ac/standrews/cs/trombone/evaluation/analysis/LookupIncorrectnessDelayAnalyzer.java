package uk.ac.standrews.cs.trombone.evaluation.analysis;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class LookupIncorrectnessDelayAnalyzer extends XYCsvAnalyzer.Timer {

    private static final String LOOKUP_INCORRECTNESS_DELAY_TIMER_CSV = "/lookup_incorrectness_delay_timer.csv";

    public LookupIncorrectnessDelayAnalyzer(Collection<Path> csv_repetitions) {

        super("lookup_incorrectness_delay_timer", csv_repetitions);
        setYAxisLabel("Incorrect Lookup Delays");
        setChartTitle("Incorrect Lookup Delays (nanoseconds)");
    }

    public LookupIncorrectnessDelayAnalyzer(final ScenarioAnalyzer scenario_analyzer) throws IOException {

        this(scenario_analyzer.getCsvsByName(LOOKUP_INCORRECTNESS_DELAY_TIMER_CSV));
    }
}
