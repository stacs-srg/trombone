package uk.ac.standrews.cs.trombone.evaluation.analysis;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class EventExecutionDurationTimerAnalyzer extends XYCsvAnalyzer.Timer {

    public static final String EVENT_EXECUTION_DURATION_TIMER_CSV = "/event_execution_duration_timer.csv";

    public EventExecutionDurationTimerAnalyzer(Collection<Path> csv_repetitions) {

        super("event_execution_duration_timer", csv_repetitions);
        setYAxisLabel("Event Execution Duration (ns)");
        setChartTitle("Event Execution Duration");
    }

    public EventExecutionDurationTimerAnalyzer(final ScenarioAnalyzer scenario_analyzer) throws IOException {

        this(scenario_analyzer.getCsvsByName(EVENT_EXECUTION_DURATION_TIMER_CSV));
    }
}
