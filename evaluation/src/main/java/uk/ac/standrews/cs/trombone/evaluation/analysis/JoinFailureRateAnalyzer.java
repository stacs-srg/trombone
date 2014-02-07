package uk.ac.standrews.cs.trombone.evaluation.analysis;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class JoinFailureRateAnalyzer extends XYCsvAnalyzer.Rate {

    public static final String JOIN_FAILURE_RATE_CSV = "/join_failure_rate.csv";

    public JoinFailureRateAnalyzer(Collection<Path> csv_repetitions) {

        super("join_failure_rate", csv_repetitions);
        setYAxisLabel("Join Failure Rate");
        setChartTitle("Join Failure Rate");
    }

    public JoinFailureRateAnalyzer(final ScenarioAnalyzer scenario_analyzer) throws IOException {

        this(scenario_analyzer.getCsvsByName(JOIN_FAILURE_RATE_CSV));
    }

}
