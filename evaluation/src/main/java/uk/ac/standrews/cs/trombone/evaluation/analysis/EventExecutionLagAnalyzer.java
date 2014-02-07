package uk.ac.standrews.cs.trombone.evaluation.analysis;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class EventExecutionLagAnalyzer extends XYCsvAnalyzer.Sampler {

    public static final String EVENT_EXECUTION_LAG_SAMPLER_CSV = "/event_execution_lag_sampler.csv";

    public EventExecutionLagAnalyzer(Collection<Path> csv_repetitions) {

        super("event_execution_lag_sampler", csv_repetitions);
        setYAxisLabel("Event Execution Lag (ns)");
        setChartTitle("Event Execution Lag ");
    }

    public EventExecutionLagAnalyzer(final ScenarioAnalyzer scenario_analyzer) throws IOException {

        this(scenario_analyzer.getCsvsByName(EVENT_EXECUTION_LAG_SAMPLER_CSV));
    }

}
