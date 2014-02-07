package uk.ac.standrews.cs.trombone.evaluation.analysis;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class JoinSuccessRateAnalyzer extends XYCsvAnalyzer.Rate {

    public static final String JOIN_SUCCESS_RATE_CSV = "/join_success_rate.csv";

    public JoinSuccessRateAnalyzer(Collection<Path> csv_repetitions) {

        super("join_success_rate", csv_repetitions);
        setYAxisLabel("Join Success Rate");
        setChartTitle("Join Success Rate");
    }

    public JoinSuccessRateAnalyzer(final ScenarioAnalyzer scenario_analyzer) throws IOException {

        this(scenario_analyzer.getCsvsByName(JOIN_SUCCESS_RATE_CSV));
    }

}
