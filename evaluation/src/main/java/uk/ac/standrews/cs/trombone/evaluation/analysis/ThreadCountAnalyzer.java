package uk.ac.standrews.cs.trombone.evaluation.analysis;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class ThreadCountAnalyzer extends XYCsvAnalyzer.Counter {

    public static final String THREAD_COUNT_GAUGE_CSV = "/thread_count_gauge.csv";

    public ThreadCountAnalyzer(final Collection<Path> csv_files) {

        super("thread_count_gauge", csv_files);
        setYAxisLabel("Number of Threads");
        setChartTitle("Nubmer of Threads");
    }

    public ThreadCountAnalyzer(final ScenarioAnalyzer scenario_analyzer) throws IOException {

        this(scenario_analyzer.getCsvsByName(THREAD_COUNT_GAUGE_CSV));
    }
    
}
