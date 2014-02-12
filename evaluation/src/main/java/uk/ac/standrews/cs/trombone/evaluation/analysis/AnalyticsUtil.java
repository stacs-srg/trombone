package uk.ac.standrews.cs.trombone.evaluation.analysis;

import com.google.common.util.concurrent.AtomicDouble;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang.WordUtils;
import org.mashti.sina.distribution.statistic.Statistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.cellprocessor.ParseDouble;
import org.supercsv.cellprocessor.ParseLong;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.cellprocessor.ift.DoubleCellProcessor;
import org.supercsv.cellprocessor.ift.LongCellProcessor;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;
import org.supercsv.util.CsvContext;
import uk.ac.standrews.cs.shabdiz.util.ArrayUtil;
import uk.ac.standrews.cs.trombone.evaluation.util.FileSystemUtils;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
final class AnalyticsUtil {

    public static final Integer[] GAUGE_CSV_COLUMN_INDICES = new Integer[] {0, 1};
    public static final String GROUP_DELIMITER = "@";
    static final long ONE_SECOND_IN_NANOS = TimeUnit.NANOSECONDS.convert(1, TimeUnit.SECONDS);
    static final NumberConverter NANOSECOND_TO_SECOND = new NanosecondToSecond();
    static final CellProcessor RELATIVE_TIME_IN_SECONDS_PROCESSOR = new ParseRelativeTime(new ConvertTime(TimeUnit.NANOSECONDS, TimeUnit.SECONDS));
    static final CellProcessor DOUBLE_PROCESSOR = new ParseDouble();
    static final CellProcessor NANOSECONDS_TO_MILLISECONDS_PROCESSOR = new NanosecondProcessor(TimeUnit.MILLISECONDS);
    static final CellProcessor LONG_PROCESSOR = new ParseLong();
    static final CellProcessor[] DEFAULT_GAUGE_CSV_PROCESSORS = new CellProcessor[] {RELATIVE_TIME_IN_SECONDS_PROCESSOR, DOUBLE_PROCESSOR};
    private static final NumberConverter AS_IS_CONVERTER = new AsIsConverter();
    private static final Logger LOGGER = LoggerFactory.getLogger(AnalyticsUtil.class);

    private AnalyticsUtil() {

    }

    static void unshardSamplerCsv(Collection<Path> csvs, Writer result) throws IOException {

        final List<CsvListReader> readers = getCsvListReaders(csvs, false);
        final String[] header = getHeader(readers);

        final CsvListWriter result_writer = new CsvListWriter(result, CsvPreference.STANDARD_PREFERENCE);
        result_writer.writeHeader(header);

        // header: time,count,min,mean,max,standard_deviation,0.1th_p,1th_p,2th_p,5th_p,25th_p,50th_p,75th_p,95th_p,98th_p,99th_p,99.9th_p

        final Statistics time = new Statistics();
        long counter = 0;
        final Statistics max = new Statistics(true);
        final Statistics min = new Statistics(true);
        final CombinedStandardDeviation stdev = new CombinedStandardDeviation(true);
        final Statistics percentile = new Statistics(true);

        while (!readers.isEmpty()) {

            final Iterator<CsvListReader> readers_iterator = readers.iterator();
            while (readers_iterator.hasNext()) {
                final CsvListReader reader = readers_iterator.next();
                final List<Object> row = reader.read(LONG_PROCESSOR, LONG_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR);

                if (row != null) {
                    time.addSample((Long) row.get(0));
                    counter += (Long) row.get(1);
                    min.addSample((Double) row.get(2));
                    max.addSample((Double) row.get(4));
                    stdev.add((Double) row.get(5), (Double) row.get(3), (long) row.get(1));
                    percentile.addSample((Double) row.get(6));
                    percentile.addSample((Double) row.get(7));
                    percentile.addSample((Double) row.get(8));
                    percentile.addSample((Double) row.get(9));
                    percentile.addSample((Double) row.get(10));
                    percentile.addSample((Double) row.get(11));
                    percentile.addSample((Double) row.get(12));
                    percentile.addSample((Double) row.get(13));
                    percentile.addSample((Double) row.get(14));
                    percentile.addSample((Double) row.get(15));
                    percentile.addSample((Double) row.get(16));
                }
                else {
                    reader.close();
                    readers_iterator.remove();
                }
            }

            result_writer.write(time.getMean().longValue(), counter, min.getMin(), stdev.wa.getWeightedAverage(), max.getMax(), stdev.getCombinedStandardDeviation(), percentile.getPercentile(0.1), percentile.getPercentile(1), percentile.getPercentile(2), percentile.getPercentile(5), percentile.getPercentile(25), percentile.getPercentile(50), percentile.getPercentile(75), percentile.getPercentile(95), percentile.getPercentile(98), percentile.getPercentile(99), percentile.getPercentile(99.9));
            time.reset();
            counter = 0;
            min.reset();
            max.reset();
            percentile.reset();
            stdev.reset();

        }
        result_writer.flush();
        result_writer.close();
    }

    static void unshardTimerCsv(Collection<Path> csvs, Writer result) throws IOException {

        final List<CsvListReader> readers = getCsvListReaders(csvs, false);
        final String[] header = getHeader(readers);

        final CsvListWriter result_writer = new CsvListWriter(result, CsvPreference.STANDARD_PREFERENCE);
        result_writer.writeHeader(header);

        // header: time,count,min,mean,max,standard_deviation,0.1th_p,1th_p,2th_p,5th_p,25th_p,50th_p,75th_p,95th_p,98th_p,99th_p,99.9th_p,unit

        final Statistics time = new Statistics();
        long counter = 0;
        final Statistics max = new Statistics(true);
        final Statistics min = new Statistics(true);
        final CombinedStandardDeviation stdev = new CombinedStandardDeviation(true);
        final Statistics percentile = new Statistics(true);
        String unit = null;

        while (!readers.isEmpty()) {

            final Iterator<CsvListReader> readers_iterator = readers.iterator();
            while (readers_iterator.hasNext()) {
                final CsvListReader reader = readers_iterator.next();
                final List<Object> row = reader.read(LONG_PROCESSOR, LONG_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR, DOUBLE_PROCESSOR, null);

                if (row != null) {
                    time.addSample((Long) row.get(0));
                    counter += (Long) row.get(1);
                    min.addSample((Double) row.get(2));
                    max.addSample((Double) row.get(4));
                    stdev.add((Double) row.get(5), (Double) row.get(3), (long) row.get(1));
                    percentile.addSample((Double) row.get(6));
                    percentile.addSample((Double) row.get(7));
                    percentile.addSample((Double) row.get(8));
                    percentile.addSample((Double) row.get(9));
                    percentile.addSample((Double) row.get(10));
                    percentile.addSample((Double) row.get(11));
                    percentile.addSample((Double) row.get(12));
                    percentile.addSample((Double) row.get(13));
                    percentile.addSample((Double) row.get(14));
                    percentile.addSample((Double) row.get(15));
                    percentile.addSample((Double) row.get(16));
                    if (unit == null) {
                        unit = (String) row.get(17);
                    }
                    else if (!unit.equals(row.get(17))) {
                        throw new IOException("un-matching row unit. expected " + unit);
                    }

                }
                else {
                    reader.close();
                    readers_iterator.remove();
                }
            }

            result_writer.write(time.getMean().longValue(), counter, min.getMin(), stdev.wa.getWeightedAverage(), max.getMax(), stdev.getCombinedStandardDeviation(), percentile.getPercentile(0.1), percentile.getPercentile(1), percentile.getPercentile(2), percentile.getPercentile(5), percentile.getPercentile(25), percentile.getPercentile(50), percentile.getPercentile(75), percentile.getPercentile(95), percentile.getPercentile(98), percentile.getPercentile(99), percentile.getPercentile(99.9), unit);
            time.reset();
            counter = 0;
            min.reset();
            max.reset();
            percentile.reset();
            stdev.reset();
            unit = null;

        }
        result_writer.flush();
        result_writer.close();

    }

    static void unshardRateCsv(Collection<Path> csvs, Writer result) throws IOException {

        final List<CsvListReader> readers = getCsvListReaders(csvs, false);
        final String[] header = getHeader(readers);

        final CsvListWriter result_writer = new CsvListWriter(result, CsvPreference.STANDARD_PREFERENCE);
        result_writer.writeHeader(header);

        // header: time,count,rate,rate_unit

        final Statistics time_sampler = new Statistics();
        long counter = 0;
        double rate = 0;
        String unit = null;

        while (!readers.isEmpty()) {

            final Iterator<CsvListReader> readers_iterator = readers.iterator();
            while (readers_iterator.hasNext()) {
                final CsvListReader reader = readers_iterator.next();
                final List<Object> row = reader.read(LONG_PROCESSOR, LONG_PROCESSOR, DOUBLE_PROCESSOR, null);

                if (row != null) {
                    time_sampler.addSample((Long) row.get(0));
                    counter += (Long) row.get(1);
                    rate += (Double) row.get(2);

                    if (unit == null) {
                        unit = (String) row.get(3);
                    }
                    else if (!unit.equals(row.get(3))) {
                        throw new IOException("un-matching row unit. expected " + unit);
                    }
                }
                else {
                    reader.close();
                    readers_iterator.remove();
                }
            }

            result_writer.write(time_sampler.getMean().longValue(), counter, rate, unit);
            time_sampler.reset();
            counter = 0;
            rate = 0;
            unit = null;

        }
        result_writer.flush();
        result_writer.close();

    }

    static void unshardCountCsv(Collection<Path> csvs, Writer result) throws IOException {

        final List<CsvListReader> readers = getCsvListReaders(csvs, false);
        final String[] header = getHeader(readers);

        final CsvListWriter result_writer = new CsvListWriter(result, CsvPreference.STANDARD_PREFERENCE);
        result_writer.writeHeader(header);

        final Statistics time_sampler = new Statistics();
        double counter = 0;

        while (!readers.isEmpty()) {

            final Iterator<CsvListReader> readers_iterator = readers.iterator();
            while (readers_iterator.hasNext()) {
                final CsvListReader reader = readers_iterator.next();
                final List<Object> row = reader.read(LONG_PROCESSOR, DOUBLE_PROCESSOR);

                if (row != null) {
                    time_sampler.addSample((Long) row.get(0));
                    final Double aDouble = (Double) row.get(1);
                    counter += aDouble.isNaN()? 0 : aDouble;
                }
                else {
                    reader.close();
                    readers_iterator.remove();
                }
            }

            result_writer.write(time_sampler.getMean().longValue(), counter);
            time_sampler.reset();
            counter = 0;
        }
        result_writer.flush();
        result_writer.close();
    }

    static class WeightedAverage {

        private final AtomicDouble a = new AtomicDouble();
        private final AtomicLong b = new AtomicLong();

        public void add(double mean, long sample_size) {

            a.addAndGet(mean * sample_size);
            b.addAndGet(sample_size);
        }

        public double getWeightedAverage() {

            return a.get() / b.get();
        }

        public void reset() {

            a.set(0);
            b.set(0);
        }
    }

    static class CombinedStandardDeviation {

        private final WeightedAverage wa = new WeightedAverage();
        private final AtomicDouble a = new AtomicDouble();
        private final boolean skip_nan;

        public CombinedStandardDeviation(final boolean skip_nan) {

            this.skip_nan = skip_nan;
        }

        public void add(double st_dev, double mean, long sample_size) {

            if (sample_size == 0) {
                return;
            }

            if (skip_nan && Double.isNaN(st_dev)) {
                st_dev = 0;
            }

            wa.add(mean, sample_size);
            a.addAndGet((sample_size - 1) * Math.pow(st_dev, 2) + sample_size * Math.pow(mean, 2));
        }

        public double getCombinedStandardDeviation() {

            return Math.sqrt((a.get() - wa.b.get() * Math.pow(wa.getWeightedAverage(), 2)) / (wa.b.get() - 1));
        }

        public double getWeightedAverage() {

            return wa.getWeightedAverage();
        }

        public void reset() {

            a.set(0);
            wa.reset();
        }

        public Long getSampleSize() {

            return wa.b.get();

        }
    }

    public static void unshard(Collection<Path> raw_zip_files) throws IOException {

        for (Path raw_zip_file : raw_zip_files) {
            try (FileSystem fileSystem = FileSystemUtils.newZipFileSystem(raw_zip_file, false)) {

                final Path host_1 = fileSystem.getPath("1");
                final List<Path> csv_list = FileSystemUtils.getMatchingFiles(host_1, fileSystem.getPathMatcher("glob:/1/*.csv"));

                for (Path path : csv_list) {
                    final Path csv_file = path.getFileName();
                    final List<Path> all_csvs = FileSystemUtils.getMatchingFiles(fileSystem.getPath(fileSystem.getSeparator()), fileSystem.getPathMatcher("glob:/[0-9]*/" + csv_file));

                    final String csv_file_name = path.toString();
                    if (csv_file_name.contains("_counter") || csv_file_name.contains("_gauge") || csv_file_name.contains("_size")) {

                        unshardCountCsv(all_csvs, Files.newBufferedWriter(csv_file, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING));
                    }
                    else if (csv_file_name.contains("_rate")) {

                        unshardRateCsv(all_csvs, Files.newBufferedWriter(csv_file, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING));
                    }
                    else if (csv_file_name.contains("_sampler")) {

                        unshardSamplerCsv(all_csvs, Files.newBufferedWriter(csv_file, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING));
                    }
                    else if (csv_file_name.contains("_timer")) {

                        unshardTimerCsv(all_csvs, Files.newBufferedWriter(csv_file, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING));
                    }
                    else {
                        LOGGER.warn("unknown csv type : {}. Skipped.", csv_file);
                    }
                }

            }
        }
    }

    private static String[] getHeader(final List<CsvListReader> readers) throws IOException {

        String[] header = null;

        for (CsvListReader csv_reader : readers) {
            if (header == null) {
                header = csv_reader.getHeader(true);
            }
            else if (header != null && !Arrays.equals(csv_reader.getHeader(true), header)) {
                throw new IOException("cannot combine un-matching headers");
            }
        }

        if (header == null) {
            throw new IOException("no csv header");
        }
        return header;
    }

    static Statistics getPropertyStatistics(String property_key, Properties[] properties_collection) {

        return getPropertyStatistics(property_key, properties_collection, AS_IS_CONVERTER);
    }

    static Statistics getPropertyStatistics(String property_key, Properties[] properties_collection, NumberConverter converter) {

        final Statistics statistics = new Statistics();

        for (final Properties properties : properties_collection) {
            if (isSuccessful(properties)) {
                final String property_as_string = properties.getProperty(property_key);
                final Double property = Double.parseDouble(property_as_string);
                statistics.addSample(converter.convert(property));
            }
            else {
                LOGGER.warn("ignored unsuccessful properties {}", properties);
            }
        }

        return statistics;
    }

    static Map<String, Statistics> getPropertyStatistics(String property_key, Properties[] properties_collection, NumberConverter converter, String[] group_by_property) {

        final Map<String, Statistics> statistics_map = new LinkedHashMap<String, Statistics>();

        for (final Properties properties : properties_collection) {
            if (isSuccessful(properties)) {
                final String property_as_string = properties.getProperty(property_key);
                final Double property = Double.parseDouble(property_as_string);

                final StringBuilder key_builder = new StringBuilder();
                for (String group : group_by_property) {
                    key_builder.append(properties.getProperty(group));
                    key_builder.append(GROUP_DELIMITER);
                }

                final String key = key_builder.toString();

                final Statistics statistics;
                if (statistics_map.containsKey(key)) {
                    statistics = statistics_map.get(key);
                }
                else {
                    statistics = new Statistics();
                    statistics_map.put(key, statistics);
                }

                statistics.addSample(converter.convert(property));
            }
            else {
                LOGGER.warn("ignored unsuccessful properties {}", properties);
            }
        }
        return statistics_map;

    }

    static Collection<File> getFilesByName(File search_path, String name) {

        return FileUtils.listFiles(search_path, new FileFilterByName(name), TrueFileFilter.INSTANCE);
    }

    static boolean isSuccessful(Properties properties) {

        //FIXME implement
        return true;
    }

    static Collection<Statistics[]> getCombinedGaugeCsvStatistics(final Collection<Path> csv_files, CellProcessor[] processors) throws IOException {

        return getCombinedCsvStatistics(csv_files, processors, GAUGE_CSV_COLUMN_INDICES, true);
    }

    static Collection<Statistics[]> getCombinedCsvStatistics(final Collection<Path> csv_files, CellProcessor[] processors, Integer[] column_indices, boolean header) throws IOException {

        final List<Statistics[]> point_statistics = new ArrayList<Statistics[]>();
        final List<CsvListReader> readers = getCsvListReaders(csv_files, header);

        while (!readers.isEmpty()) {

            final Iterator<CsvListReader> readers_iterator = readers.iterator();
            final Statistics[] combined_statistics = new Statistics[column_indices.length];
            while (readers_iterator.hasNext()) {
                final CsvListReader reader = readers_iterator.next();
                final List<Object> row = reader.read(processors);
                if (row != null) {

                    int i = 0;
                    for (Integer index : column_indices) {
                        if (combined_statistics[i] == null) {
                            combined_statistics[i] = new Statistics();
                        }
                        combined_statistics[i].addSample((Number) row.get(index));
                        i++;
                    }
                }
                else {
                    reader.close();
                    readers_iterator.remove();
                }
            }
            if (combined_statistics[0] != null) {
                point_statistics.add(combined_statistics);
            }
        }

        return point_statistics;
    }

    static Collection<CombinedStandardDeviation> getCombinedTimerCsvStatistics(final Collection<Path> csv_files, CellProcessor[] processors) throws IOException {

        final List<CombinedStandardDeviation> point_statistics = new ArrayList<>();
        final List<CsvListReader> readers = getCsvListReaders(csv_files, true);

        while (!readers.isEmpty()) {

            final Iterator<CsvListReader> readers_iterator = readers.iterator();
            final CombinedStandardDeviation combinedStandardDeviation = new CombinedStandardDeviation(true);
            while (readers_iterator.hasNext()) {
                final CsvListReader reader = readers_iterator.next();
                final List<Object> row = reader.read(processors);
                if (row != null) {
                    long count = (long) row.get(1);
                    double mean = (double) row.get(3);
                    double stdev = (double) row.get(5);
                    combinedStandardDeviation.add(stdev, mean, count);
                }
                else {
                    reader.close();
                    readers_iterator.remove();
                }
            }
            point_statistics.add(combinedStandardDeviation);
        }

        return point_statistics;
    }

    static Statistics getAggregatedCsvStatistic(final Collection<Path> csv_files, CellProcessor[] processors, Integer column_index, boolean header) throws IOException {

        final Statistics statistics = new Statistics();
        for (Path csv : csv_files) {
            final CsvListReader reader = getCsvListReader(csv, header);
            try {
                List<Object> row;
                while ((row = reader.read(processors)) != null) {
                    statistics.addSample((Number) row.get(column_index));
                }
            }
            finally {
                IOUtils.closeQuietly(reader);
            }
        }

        return statistics;
    }

    private static List<CsvListReader> getCsvListReaders(final Collection<Path> csv_files, final boolean header) throws IOException {

        final List<CsvListReader> readers = new ArrayList<CsvListReader>();
        for (Path csv : csv_files) {

            final CsvListReader reader = getCsvListReader(csv, header);
            readers.add(reader);
        }
        return readers;
    }

    private static List<CsvListReader> getCsvListReaders(final Collection<Reader> readers) {

        final List<CsvListReader> csv_readers = new ArrayList<CsvListReader>();
        for (Reader reader : readers) {

            final CsvListReader csv_reader = new CsvListReader(reader, CsvPreference.STANDARD_PREFERENCE);
            csv_readers.add(csv_reader);
        }
        return csv_readers;
    }

    private static CsvListReader getCsvListReader(final Path csv, boolean header) throws IOException {

        final CsvListReader reader = new CsvListReader(Files.newBufferedReader(csv, StandardCharsets.UTF_8), CsvPreference.STANDARD_PREFERENCE);
        if (header) {
            reader.getHeader(true);
        }
        return reader;
    }

    static String decorateManagerAsApplicationName(final String manager) {

        final String tidy_manager_name = manager.replaceAll("_", " ").replaceAll("\\..*", "");
        return tidy_manager_name.contains("Chord") ? "Chord" : manager.contains("Echo") ? "Echo" : WordUtils.capitalize(tidy_manager_name);

    }

    static String decorateManagerAsDeploymentStrategy(final String manager) {

        return splitCamelCase(manager.substring(manager.indexOf(".") + 1));

    }

    static String splitCamelCase(String s) {

        return s.replaceAll(String.format("%s|%s|%s", "(?<=[A-Z])(?=[A-Z][a-z])", "(?<=[^A-Z])(?=[A-Z])", "(?<=[A-Za-z])(?=[^A-Za-z])"), " ");
    }

    static String decoratePoolSize(final String pool_size) {

        final int size = Integer.parseInt(pool_size);
        return size != Integer.MAX_VALUE ? pool_size : "Maximum";
    }

    static String decorateKillPortion(final String kill_portion) {

        return kill_portion + "%";
    }

    static File[] listSubDirectoriesExcluding(final File results_path, final String... excludes) {

        return results_path.listFiles(new FileFilter() {

            @Override
            public boolean accept(final File file) {

                return file.isDirectory() && !ArrayUtil.contains(file.getName(), excludes);
            }
        });
    }

    interface NumberConverter {

        Number convert(Number value);
    }

    private static class NanosecondToSecond implements NumberConverter {

        @Override
        public Number convert(final Number value) {

            return value.doubleValue() / ONE_SECOND_IN_NANOS;
        }
    }

    private static class FileFilterByName implements IOFileFilter {

        private final String file_name;

        private FileFilterByName(String file_name) {

            this.file_name = file_name;
        }

        @Override
        public boolean accept(final File file) {

            return file.isFile() && matchesFileName(file.getName());
        }

        @Override
        public boolean accept(final File dir, final String name) {

            return matchesFileName(name);
        }

        private boolean matchesFileName(final String name) {

            return name.equals(file_name);
        }
    }

    private static class AsIsConverter implements NumberConverter {

        @Override
        public Number convert(final Number value) {

            return value;
        }
    }

    static class ParseRelativeTime extends ParseLong implements LongCellProcessor {

        private Long first_time;

        ParseRelativeTime(final LongCellProcessor next) {

            super(next);
        }

        @Override
        public synchronized Object execute(final Object value, final CsvContext context) {

            final Long time = (Long) super.execute(value, context);
            if (first_time == null) {
                first_time = time;
            }
            return time - first_time;
        }
    }

    static class ConvertTime extends CellProcessorAdaptor implements LongCellProcessor {

        private final TimeUnit source_unit;
        private final TimeUnit target_unit;

        ConvertTime(TimeUnit source_unit, TimeUnit target_unit) {

            this.source_unit = source_unit;
            this.target_unit = target_unit;
        }

        @Override
        public Object execute(final Object value, final CsvContext context) {

            return next.execute(target_unit.convert((Long) value, source_unit), context);
        }
    }

    private static class NanosecondProcessor extends ParseDouble {

        private NanosecondProcessor(final TimeUnit unit) {

            super(new DoubleCellProcessor() {

                final long conversion_rate = TimeUnit.NANOSECONDS.convert(1, unit);

                @Override
                public Object execute(final Object value, final CsvContext context) {

                    return ((double) value) / conversion_rate;
                }
            });

        }
    }
}

