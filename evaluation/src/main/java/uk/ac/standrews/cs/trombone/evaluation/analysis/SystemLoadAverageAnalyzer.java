package uk.ac.standrews.cs.trombone.evaluation.analysis;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class SystemLoadAverageAnalyzer extends XYCsvAnalyzer.Counter {

    public static final String SYSTEM_LOAD_AVERAGE_GAUGE_CSV = "/system_load_average_gauge.csv";

    public SystemLoadAverageAnalyzer(final Collection<Path> csv_files) {

        super("system_load_average_gauge", csv_files);
        setYAxisLabel("System Load Average");
        setChartTitle("System Load Average");
    }

    public SystemLoadAverageAnalyzer(final ScenarioAnalyzer scenario_analyzer) throws IOException {

        this(scenario_analyzer.getCsvsByName(SYSTEM_LOAD_AVERAGE_GAUGE_CSV));
    }

}
