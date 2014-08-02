package uk.ac.standrews.cs.trombone.evaluation.analysis;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import org.mashti.sina.distribution.statistic.Statistics;
import org.supercsv.cellprocessor.ift.CellProcessor;

import static uk.ac.standrews.cs.trombone.evaluation.analysis.AnalyticsUtil.DOUBLE_PROCESSOR;
import static uk.ac.standrews.cs.trombone.evaluation.analysis.AnalyticsUtil.LONG_PROCESSOR;
import static uk.ac.standrews.cs.trombone.evaluation.analysis.AnalyticsUtil.NANOSECONDS_TO_MILLISECONDS_PROCESSOR;
import static uk.ac.standrews.cs.trombone.evaluation.analysis.AnalyticsUtil.RELATIVE_TIME_IN_SECONDS_PROCESSOR;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public abstract class XYCsvAnalyzer {

    private final String name;
    protected final Collection<Path> csv_repetitions;
    protected Collection<Statistics[]> rows_statistics;

    private XYCsvAnalyzer(String name, Collection<Path> csv_repetitions) {

        this.name = name;
        this.csv_repetitions = csv_repetitions;
    }

    public String getName() {

        return name;
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

            return new CellProcessor[] {
                    RELATIVE_TIME_IN_SECONDS_PROCESSOR, null, DOUBLE_PROCESSOR, null
            };
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

            return new CellProcessor[] {
                    RELATIVE_TIME_IN_SECONDS_PROCESSOR, LONG_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR,
                    DOUBLE_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR
            };
        }

        @Override
        protected Integer[] getCsvColumnIndices() {
            // FIXME UNSHARD
            return new Integer[] {0, 3};
        }

    }

    public static class Timer extends Sampler {

        protected Timer(final String name, final Collection<Path> csv_repetitions) {

            super(name, csv_repetitions);
        }

        @Override
        protected CellProcessor[] getCellProcessors() {

            return new CellProcessor[] {
                    RELATIVE_TIME_IN_SECONDS_PROCESSOR, LONG_PROCESSOR, NANOSECONDS_TO_MILLISECONDS_PROCESSOR, NANOSECONDS_TO_MILLISECONDS_PROCESSOR, NANOSECONDS_TO_MILLISECONDS_PROCESSOR,
                    NANOSECONDS_TO_MILLISECONDS_PROCESSOR, NANOSECONDS_TO_MILLISECONDS_PROCESSOR, NANOSECONDS_TO_MILLISECONDS_PROCESSOR, NANOSECONDS_TO_MILLISECONDS_PROCESSOR,
                    NANOSECONDS_TO_MILLISECONDS_PROCESSOR, NANOSECONDS_TO_MILLISECONDS_PROCESSOR, NANOSECONDS_TO_MILLISECONDS_PROCESSOR, NANOSECONDS_TO_MILLISECONDS_PROCESSOR,
                    NANOSECONDS_TO_MILLISECONDS_PROCESSOR, NANOSECONDS_TO_MILLISECONDS_PROCESSOR, NANOSECONDS_TO_MILLISECONDS_PROCESSOR, NANOSECONDS_TO_MILLISECONDS_PROCESSOR, null
            };
        }

    }
}
