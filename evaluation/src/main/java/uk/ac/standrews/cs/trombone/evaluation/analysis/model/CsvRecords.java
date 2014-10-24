package uk.ac.standrews.cs.trombone.evaluation.analysis.model;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.commons.math3.stat.interval.ConfidenceInterval;

import static java.lang.Double.parseDouble;
import static java.lang.Long.parseLong;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public final class CsvRecords {

    private static final Pattern COMMA = Pattern.compile("[,]");

    private CsvRecords() {

    }

    public static <R extends Record> void writeOverallToCsv(final Path path, final ToDoubleFunction<R> to_double, final List<Stream<R>> repetitions) throws IOException {

        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE)) {
            writer.write("mean,ci_lower,ci_upper");
            writer.newLine();

            final SummaryStatistics statistics = new SummaryStatistics();
            for (Stream<R> original_record : repetitions) {
                original_record.mapToDouble(to_double)
                        .filter(value -> !Double.isNaN(value))
                        .forEach(statistics:: addValue);
            }

            writer.write(String.format("%.2f,%s", statistics.getMean(), toCsvRecord(StatisticalAnalysis.getConfidenceInterval(statistics, StatisticalAnalysis.CONFIDENCE_LEVEL))));
            writer.newLine();
        }
    }

    private static GaugeRecord<Double> toGaugeRecord(final String csv_record) {

        final String[] value_matcher = splitCsvRecord(csv_record);
        final GaugeRecord<Double> record = new GaugeRecord<>();
        record.setTimestamp(parseLong(value_matcher[0]));
        record.setValue(parseDouble(value_matcher[1]));
        return record;
    }

    private static CounterRecord toCounterRecord(final String csv_record) {

        final String[] value_matcher = splitCsvRecord(csv_record);
        final CounterRecord record = new CounterRecord();
        setCounterRecordFields(value_matcher, record);
        return record;
    }

    private static void setCounterRecordFields(final String[] value_matcher, final CounterRecord record) {

        record.setTimestamp(parseLong(value_matcher[0]));
        record.setCount(parseLong(value_matcher[1]));
    }

    private static String[] splitCsvRecord(final String csv_record) {

        Objects.requireNonNull(csv_record);
        return COMMA.split(csv_record.trim());
    }

    private static RateRecord toRateRecord(final String csv_record) {

        final String[] value_matcher = splitCsvRecord(csv_record);
        final RateRecord record = new RateRecord();
        setCounterRecordFields(value_matcher, record);
        record.setRate(parseDouble(value_matcher[2]));
        record.setUnit(TimeUnit.valueOf(value_matcher[3].replace("calls/", "")));
        return record;
    }

    private static SamplerRecord toSamplerRecord(final String csv_record) {

        final String[] value_matcher = splitCsvRecord(csv_record);
        final SamplerRecord record = new SamplerRecord();
        setSamplerRecordFields(value_matcher, record);
        return record;
    }

    private static void setSamplerRecordFields(final String[] value_matcher, final SamplerRecord record) {

        setCounterRecordFields(value_matcher, record);
        record.setMin(parseDouble(value_matcher[2]));
        record.setMean(parseDouble(value_matcher[3]));
        record.setMax(parseDouble(value_matcher[4]));
        record.setStandardDeviation(parseDouble(value_matcher[5]));
        record.setPercentileZeroPointOne(parseDouble(value_matcher[6]));
        record.setPercentileOne(parseDouble(value_matcher[7]));
        record.setPercentileTwo(parseDouble(value_matcher[8]));
        record.setPercentileFive(parseDouble(value_matcher[9]));
        record.setPercentileTwentyFive(parseDouble(value_matcher[10]));
        record.setPercentileFifty(parseDouble(value_matcher[11]));
        record.setPercentileSeventyFive(parseDouble(value_matcher[12]));
        record.setPercentileNinetyFive(parseDouble(value_matcher[13]));
        record.setPercentileNinetyEight(parseDouble(value_matcher[14]));
        record.setPercentileNinetyNine(parseDouble(value_matcher[15]));
        record.setPercentileNinetyNinePointNine(parseDouble(value_matcher[16]));
    }

    private static TimerRecord toTimerRecord(final String csv_record) {

        final String[] value_matcher = splitCsvRecord(csv_record);
        final TimerRecord record = new TimerRecord();
        setSamplerRecordFields(value_matcher, record);
        record.setUnit(TimeUnit.valueOf(value_matcher[17]));
        return record;
    }

    public static Stream<CounterRecord> readCounterRecords(Path path) throws IOException {

        return Files.newBufferedReader(path)
                .lines()
                .skip(1)
                .map(CsvRecords:: toCounterRecord);
    }

    public static Stream<GaugeRecord<Double>> readGaugeRecords(Path path) throws IOException {

        return Files.newBufferedReader(path)
                .lines()
                .skip(1)
                .map(CsvRecords:: toGaugeRecord);
    }

    public static Stream<RateRecord> readRateRecords(Path path) throws IOException {

        return Files.newBufferedReader(path)
                .lines()
                .skip(1)
                .map(CsvRecords:: toRateRecord);
    }

    public static Stream<SamplerRecord> readSamplerRecords(Path path) throws IOException {

        return Files.newBufferedReader(path)
                .lines()
                .skip(1)
                .map(CsvRecords:: toSamplerRecord);
    }

    public static Stream<TimerRecord> readTimerRecords(Path path) throws IOException {

        return Files.newBufferedReader(path)
                .lines()
                .skip(1)
                .map(CsvRecords:: toTimerRecord);
    }

    private static <R extends Record> void writeCsv(Stream<R> records, Function<R, String> record_converter, String header, Path path) throws IOException {

        try (final BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

            writer.write(header);
            writer.newLine();

            final Iterator<R> iterator = records.iterator();
            while (iterator.hasNext()) {
                final R record = iterator.next();
                writer.write(record_converter.apply(record));
                writer.newLine();
            }

            writer.close();
        }
    }

    public static void writeCombinedCounterRecordToCsv(Stream<CombinedCounterRecord> records, Path path) throws IOException {

        writeCsv(records, CsvRecords:: combinedCounterRecordToCsv, "time," + "time,count_mean,count_ci_lower,count_ci_upper", path);
    }

    public static void writeCombinedGaugeRecordsToCsv(Stream<CombinedGaugeRecord<Double>> records, Path path) throws IOException {

        writeCsv(records, CsvRecords:: combinedGaugeRecordToCsv, "time,value_mean,value_ci_lower,value_ci_upper", path);
    }

    public static void writeCombinedRateRecordToCsv(Stream<CombinedRateRecord> records, Path path) throws IOException {

        writeCsv(records, CsvRecords:: combinedRateRecordToCsv, "time,count_mean,count_ci_lower,count_ci_upper,rate_mean,rate_ci_lower,rate_ci_upper", path);
    }

    public static void writeCombinedSamplerRecordToCsv(Stream<CombinedSamplerRecord> records, Path path) throws IOException {

        writeCsv(records, CsvRecords:: combinedSamplerRecordToCsv, "time,count_mean,count_ci_lower,count_ci_upper,min_of_mins,max_of_maxes,mean_of_means,mean_ci_lower,mean_ci_upper,overall_mean,overall_sd,overall_ci_lower,overall_ci_upper", path);
    }

    public static void writeCombinedTimerRecordToCsv(Stream<CombinedTimerRecord> records, Path path) throws IOException {

        writeCsv(records, CsvRecords:: combinedTimerRecordToCsv, "time," +
                "count_mean,count_ci_lower,count_ci_upper,min_of_mins," +
                "max_of_maxes,mean_of_means,mean_ci_lower,mean_ci_upper," +
                "overall_mean,overall_sd,overall_ci_lower,overall_ci_upper, " +
                "unit", path);
    }

    private static String combinedGaugeRecordToCsv(final CombinedGaugeRecord<Double> record) {

        return String.format("%d,%.2f,%s", record.getTimestamp(), record.getMeanValue(), toCsvRecord(record.getValueConfidenceInterval()));
    }

    private static String combinedRateRecordToCsv(final CombinedRateRecord record) {

        return String.format("%s,%.2f,%s,%s", combinedCounterRecordToCsv(record), record.getMeanRate(), toCsvRecord(record.getRateConfidenceInterval()), record.getUnit());
    }

    private static String combinedSamplerRecordToCsv(final CombinedSamplerRecord record) {

        return String.format("%s,%.2f,%.2f,%.2f,%s,%.2f,%.2f,%s", combinedCounterRecordToCsv(record), record.getMinOfMins(), record.getMaxOfMaxes(), record.getMeanOfMeans(), toCsvRecord(record.getMeanConfidenceInterval()), record.getOverallMean(), record.getOverallStandardDeviation(), toCsvRecord(record.getOverallConfidenceInterval()));
    }

    private static String combinedTimerRecordToCsv(final CombinedTimerRecord record) {

        return String.format("%s,%.2f,%.2f,%.2f,%s,%.2f,%.2f,%s,%s", combinedCounterRecordToCsv(record), record.getMinOfMins(), record.getMaxOfMaxes(), record.getMeanOfMeans(), toCsvRecord(record.getMeanConfidenceInterval()), record.getOverallMean(), record.getOverallStandardDeviation(), toCsvRecord(record.getOverallConfidenceInterval()), record.getUnit());
    }

    private static String combinedCounterRecordToCsv(final CombinedCounterRecord record) {

        return String.format("%d,%.2f,%s", record.getTimestamp(), record.getMeanCount(), toCsvRecord(record.getCountConfidenceInterval()));
    }

    private static String toCsvRecord(Optional<ConfidenceInterval> optional_ci) {

        double ci_lower = 0;
        double ci_upper = 0;

        if (optional_ci.isPresent()) {
            final ConfidenceInterval ci = optional_ci.get();
            ci_lower = ci.getLowerBound();
            ci_upper = ci.getUpperBound();
        }

        return String.format("%.2f,%.2f", ci_lower, ci_upper);
    }

}
