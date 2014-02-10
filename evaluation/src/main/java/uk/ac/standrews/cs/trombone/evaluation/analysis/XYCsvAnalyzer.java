package uk.ac.standrews.cs.trombone.evaluation.analysis;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYErrorRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.YIntervalSeries;
import org.jfree.data.xy.YIntervalSeriesCollection;
import org.mashti.sight.ChartExportUtils;
import org.mashti.sight.PlainChartTheme;
import org.mashti.sina.distribution.statistic.Statistics;
import org.supercsv.cellprocessor.ift.CellProcessor;
import uk.ac.standrews.cs.shabdiz.util.Duration;
import uk.ac.standrews.cs.trombone.evaluation.scenarios.Constants;

import static uk.ac.standrews.cs.trombone.evaluation.analysis.AnalyticsUtil.DOUBLE_PROCESSOR;
import static uk.ac.standrews.cs.trombone.evaluation.analysis.AnalyticsUtil.LONG_PROCESSOR;
import static uk.ac.standrews.cs.trombone.evaluation.analysis.AnalyticsUtil.RELATIVE_TIME_IN_SECONDS_PROCESSOR;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public abstract class XYCsvAnalyzer implements Analyzer {

    private static final String DEFAULT_X_AXIS_LABEL = "Time Through the Experiment (s)";
    private static final boolean DEFAULT_SHOW_ERROR_BARS = true;
    private static final int DEFAULT_CSV_WIDTH = 1024;
    private static final int DEFAULT_CSV_HEIGHT = 768;
    private final String name;
    protected final Collection<Path> csv_repetitions;
    private Duration report_interval;
    private String y_axis_label;
    private String x_axis_label;
    private String chart_title;
    private boolean show_error_bars;
    private XYPlot plot;
    private YIntervalSeriesCollection xy_dataset;
    private YIntervalSeries series;
    protected Collection<Statistics[]> rows_statistics;

    private XYCsvAnalyzer(String name, Collection<Path> csv_repetitions) {

        this.name = name;
        this.csv_repetitions = csv_repetitions;
        x_axis_label = DEFAULT_X_AXIS_LABEL;
        show_error_bars = DEFAULT_SHOW_ERROR_BARS;
        report_interval = Constants.OBSERVATION_INTERVAL;
    }

    public String getChartTitle() {

        return chart_title;
    }

    public void setChartTitle(final String chart_title) {

        this.chart_title = chart_title;
    }

    public String getYAxisLabel() {

        return y_axis_label;
    }

    public void setYAxisLabel(final String y_axis_label) {

        this.y_axis_label = y_axis_label;
    }

    public String getXAxisLabel() {

        return x_axis_label;
    }

    public void setXAxisLabel(final String x_axis_label) {

        this.x_axis_label = x_axis_label;
    }

    public void setReportInterval(final Duration report_interval) {

        this.report_interval = report_interval;
    }

    public Duration getReportInterval() {

        return report_interval;
    }

    public void setShowErrorBars(final boolean show_error_bars) {

        this.show_error_bars = show_error_bars;
    }

    public boolean getShowErrorBars() {

        return show_error_bars;
    }

    @Override
    public String getName() {

        return name;
    }

    @Override
    public JFreeChart getChart() throws IOException {

        final JFreeChart chart = new JFreeChart(chart_title, getPlot());
        PlainChartTheme.applyTheme(chart);
        return chart;
    }

    public void saveAsSVG(final File destination_directory) throws IOException {

        saveAsSVG(destination_directory, DEFAULT_CSV_WIDTH, DEFAULT_CSV_HEIGHT);
    }

    public void saveAsSVG(final File destination_directory, int width, int height) throws IOException {

        final JFreeChart chart = getChart();
        ChartExportUtils.saveAsSVG(chart, width, height, new File(destination_directory, getName() + ".svg"));
    }

    protected synchronized XYPlot getPlot() throws IOException {

        if (plot == null) {
            final ValueAxis x_axis = getXAxis();
            final ValueAxis y_axis = getYAxis();
            final XYItemRenderer error_renderer = getXYItemRenderer();
            final XYDataset dataset = getXYDataset();
            plot = new XYPlot(dataset, x_axis, y_axis, error_renderer);
            final ValueAxis range_axis = plot.getRangeAxis();
            range_axis.setLowerBound(0);
        }
        return plot;
    }

    private XYDataset getXYDataset() throws IOException {

        if (xy_dataset == null) {
            xy_dataset = new YIntervalSeriesCollection();
            xy_dataset.addSeries(getYIntervalSeries());
        }
        return xy_dataset;
    }

    protected ValueAxis getYAxis() {

        return new NumberAxis(y_axis_label);
    }

    protected ValueAxis getXAxis() {

        return new NumberAxis(x_axis_label);
    }

    private synchronized XYItemRenderer getXYItemRenderer() {

        if (show_error_bars) {
            final XYErrorRenderer error_renderer = new XYErrorRenderer();
            error_renderer.setBaseLinesVisible(true);
            error_renderer.setBaseShapesVisible(false);
            error_renderer.setDrawYError(true);
            return error_renderer;
        }
        else {
            return null;
        }
    }

    protected abstract CellProcessor[] getCellProcessors();

    protected abstract Integer[] getCsvColumnIndices();

    protected synchronized Collection<Statistics[]> getStatistics() throws IOException {

        if (rows_statistics == null) {
            final CellProcessor[] processors = getCellProcessors();
            rows_statistics = AnalyticsUtil.getCombinedCsvStatistics(csv_repetitions, processors, getCsvColumnIndices(), true);
        }
        return rows_statistics;
    }

    protected synchronized YIntervalSeries getYIntervalSeries() throws IOException {

        if (series == null) {
            series = new YIntervalSeries(getName());
            final Collection<Statistics[]> rows_statistics = getStatistics();
            final long bucket_size = report_interval.getLength(TimeUnit.SECONDS);
            long time_bucket = 0;
            for (Statistics[] row_statistics : rows_statistics) {
                final double mean = row_statistics[1].getMean().doubleValue();
                final double ci = row_statistics[1].getConfidenceInterval95Percent().doubleValue();
                final double low = mean - ci;
                final double high = mean + ci;
                series.add(time_bucket, Double.isNaN(mean) ? 0 : mean, Double.isNaN(low) ? 0 : low, Double.isNaN(high) ? 0 : high);
                time_bucket += bucket_size;
            }
        }
        return series;
    }

    public static class Counter extends XYCsvAnalyzer {

        protected Counter(final String name, final Collection<Path> csv_repetitions) {

            super(name, csv_repetitions);
        }

        @Override
        protected CellProcessor[] getCellProcessors() {

            return new CellProcessor[] {RELATIVE_TIME_IN_SECONDS_PROCESSOR, DOUBLE_PROCESSOR};
        }

        @Override
        protected Integer[] getCsvColumnIndices() {

            return new Integer[] {0, 1};
        }
    }

    public static class Rate extends XYCsvAnalyzer {

        protected Rate(final String name, final Collection<Path> csv_repetitions) {

            super(name, csv_repetitions);
        }

        @Override
        protected CellProcessor[] getCellProcessors() {

            return new CellProcessor[] {RELATIVE_TIME_IN_SECONDS_PROCESSOR, null, DOUBLE_PROCESSOR, null};
        }

        @Override
        protected Integer[] getCsvColumnIndices() {

            return new Integer[] {0, 2};
        }
    }

    public static class Sampler extends XYCsvAnalyzer {

        protected Sampler(final String name, final Collection<Path> csv_repetitions) {

            super(name, csv_repetitions);
        }

        @Override
        protected CellProcessor[] getCellProcessors() {

            return new CellProcessor[] {RELATIVE_TIME_IN_SECONDS_PROCESSOR, LONG_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR};
        }

        @Override
        protected Integer[] getCsvColumnIndices() {
            // FIXME UNSHARD
            return new Integer[] {0, 3};
        }
    }

    public static class Timer extends XYCsvAnalyzer {

        protected Timer(final String name, final Collection<Path> csv_repetitions) {

            super(name, csv_repetitions);
        }

        @Override
        protected CellProcessor[] getCellProcessors() {

            return new CellProcessor[] {RELATIVE_TIME_IN_SECONDS_PROCESSOR, LONG_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR, null};
        }

        @Override
        protected Integer[] getCsvColumnIndices() {

            return new Integer[] {0, 3};
        }

    }
}