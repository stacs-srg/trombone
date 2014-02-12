package uk.ac.standrews.cs.trombone.evaluation.analysis;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class ThreadCpuUsageAnalyzer extends XYCsvAnalyzer.Counter {

    public static final String THREAD_CPU_USAGE_GAUGE_CSV = "/thread_cpu_usage_gauge.csv";

    public ThreadCpuUsageAnalyzer(final Collection<Path> csv_files) {

        super("thread_cpu_usage_gauge", csv_files);
        setYAxisLabel("CPU usage  (%)");
        setChartTitle("CPU usage");
    }

    public ThreadCpuUsageAnalyzer(final ScenarioAnalyzer scenario_analyzer) throws IOException {

        this(scenario_analyzer.getCsvsByName(THREAD_CPU_USAGE_GAUGE_CSV));
    }

}
