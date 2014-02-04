package uk.ac.standrews.cs.trombone.evaluation.analysis;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.jfree.chart.JFreeChart;
import org.mashti.sight.ChartExportUtils;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class LookupExecutionRateAnalyzer extends GaugeCsvAnalyzer {

    protected LookupExecutionRateAnalyzer() {

        super("lookup_execution_rate", AnalyticsUtil.getFilesByName(new File("results/PlatformJustificationMultipleHost/repetitions"), "lookup_execution_rate.csv"), "Time through experiment", "Lookup Execution Rate");
    }

    public static void main(String[] args) throws IOException {

        saveAsSVG(new File("/Users/masih/Desktop"), new LookupExecutionRateAnalyzer());
    }

    static void saveAsSVG(final File destination_directory, Analyzer analyzer) throws IOException {

        final JFreeChart chart = analyzer.getChart();
        final File analysis_dir = makeAnalysisDirectory(destination_directory);
        ChartExportUtils.saveAsSVG(chart, 1024, 768, new File(analysis_dir, analyzer.getName() + ".svg"));
    }

    private static File makeAnalysisDirectory(final File parent) throws IOException {

        final File analysis_dir = new File(parent, "analysis");
        if (!analysis_dir.isDirectory()) {
            FileUtils.forceMkdir(analysis_dir);
        }
        return analysis_dir;
    }
}
