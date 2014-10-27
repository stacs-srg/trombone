package uk.ac.standrews.cs.trombone.evaluation.analysis;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.math3.ml.clustering.Clusterer;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.json.JSONArray;
import org.mashti.gauge.Counter;
import org.mashti.gauge.Gauge;
import org.mashti.gauge.Metric;
import org.mashti.gauge.Rate;
import org.mashti.gauge.Sampler;
import org.mashti.gauge.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.shabdiz.util.Duration;
import uk.ac.standrews.cs.trombone.core.PeerConfiguration;
import uk.ac.standrews.cs.trombone.core.maintenance.EvaluatedDisseminationStrategy;
import uk.ac.standrews.cs.trombone.core.maintenance.EvolutionaryMaintenanceFactory;
import uk.ac.standrews.cs.trombone.core.maintenance.MaintenanceFactory;
import uk.ac.standrews.cs.trombone.core.maintenance.PerPointClusterer;
import uk.ac.standrews.cs.trombone.core.maintenance.RandomMaintenanceFactory;
import uk.ac.standrews.cs.trombone.core.maintenance.pfclust.PFClustClusterer;
import uk.ac.standrews.cs.trombone.evaluation.analysis.model.CombinedCounterRecord;
import uk.ac.standrews.cs.trombone.evaluation.analysis.model.CombinedGaugeRecord;
import uk.ac.standrews.cs.trombone.evaluation.analysis.model.CombinedRateRecord;
import uk.ac.standrews.cs.trombone.evaluation.analysis.model.CombinedSamplerRecord;
import uk.ac.standrews.cs.trombone.evaluation.analysis.model.CombinedTimerRecord;
import uk.ac.standrews.cs.trombone.evaluation.analysis.model.CounterRecord;
import uk.ac.standrews.cs.trombone.evaluation.analysis.model.CsvRecords;
import uk.ac.standrews.cs.trombone.evaluation.analysis.model.GaugeRecord;
import uk.ac.standrews.cs.trombone.evaluation.analysis.model.RateRecord;
import uk.ac.standrews.cs.trombone.evaluation.analysis.model.SamplerRecord;
import uk.ac.standrews.cs.trombone.evaluation.analysis.model.StatisticalAnalysis;
import uk.ac.standrews.cs.trombone.evaluation.analysis.model.TimerRecord;
import uk.ac.standrews.cs.trombone.evaluation.scenarios.Batch1EffectOfChurn;
import uk.ac.standrews.cs.trombone.evaluation.scenarios.Batch2EffectOfWorkload;
import uk.ac.standrews.cs.trombone.evaluation.scenarios.Batch3EffectOfTrainingDuration;
import uk.ac.standrews.cs.trombone.evaluation.scenarios.Batch4EffectOfClusteringAlgorithm;
import uk.ac.standrews.cs.trombone.evaluation.scenarios.Batch5EffectOfFeedback;
import uk.ac.standrews.cs.trombone.evaluation.scenarios.Batch6EffectOfFeedbackWithTraining;
import uk.ac.standrews.cs.trombone.evaluation.scenarios.Batch7EffectOfTrainingDurationOscillating;
import uk.ac.standrews.cs.trombone.evaluation.scenarios.Constants;
import uk.ac.standrews.cs.trombone.evaluation.util.FileSystemUtils;
import uk.ac.standrews.cs.trombone.evaluation.util.ScenarioUtils;
import uk.ac.standrews.cs.trombone.event.Scenario;
import uk.ac.standrews.cs.trombone.event.TromboneMetricSet;
import uk.ac.standrews.cs.trombone.event.environment.Churn;
import uk.ac.standrews.cs.trombone.event.environment.Workload;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public final class Analysis {

    private static final Logger LOGGER = LoggerFactory.getLogger(Analysis.class);
    private static final Pattern UNDERSCORE = Pattern.compile("_");
    public static final TromboneMetricSet TROMBONE_METRIC_SET = new TromboneMetricSet(null);
    private static final Class<? extends Metric>[] METRIC_TYPES = new Class[] {
            Gauge.class, Counter.class, Rate.class, Sampler.class, Timer.class
    };

    static final List<Scenario> SCENARIOS = new ArrayList<>();

    static {
                SCENARIOS.addAll(Batch1EffectOfChurn.getInstance()
                        .get());
                SCENARIOS.addAll(Batch2EffectOfWorkload.getInstance()
                        .get());
                SCENARIOS.addAll(Batch3EffectOfTrainingDuration.getInstance()
                        .get());
                SCENARIOS.addAll(Batch4EffectOfClusteringAlgorithm.getInstance()
                        .get());
                SCENARIOS.addAll(Batch5EffectOfFeedback.getInstance()
                        .get());

        SCENARIOS.addAll(Batch6EffectOfFeedbackWithTraining.getInstance()
                .get());
        SCENARIOS.addAll(Batch7EffectOfTrainingDurationOscillating.getInstance()
                .get());
    }

    public static void main(String[] args) throws IOException {

        final Path scenarios_path = ScenarioUtils.getResultsHome()
                .resolve("scenarios.csv");

        try (BufferedWriter writer = Files.newBufferedWriter(scenarios_path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

            writer.write("name,churn,workload,peer_configuration,experiment_duration,training_duration,clustering_algorithm,feedback_enabled");
            writer.newLine();

            for (Scenario scenario : SCENARIOS) {

                final Scenario.HostScenario hostScenario = scenario.getHostScenarios()
                        .stream()
                        .findFirst()
                        .get();

                writer.write(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"", scenario.getName(), shortName(hostScenario.getChurn()), shortName(hostScenario.getWorkload()), shortName(hostScenario.getPeerConfiguration()), scenario.getExperimentDuration(), scenario.getExperimentDuration()
                        .subtract(new Duration(4, TimeUnit.HOURS)), getClusteringAlgorithmName(hostScenario.getPeerConfiguration()), hostScenario.getPeerConfiguration()
                        .isApplicationFeedbackEnabled()));
                writer.newLine();
            }

        }

        try (BufferedWriter writer = Files.newBufferedWriter(ScenarioUtils.getResultsHome()
                .resolve("metrics.csv"), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

            writer.write("name,type");
            writer.newLine();

            for (Map.Entry<String, Metric> entry : TROMBONE_METRIC_SET.getMetrics()
                    .entrySet()) {

                final Metric metric = entry.getValue();
                writer.write(String.format("\"%s\",\"%s\"", entry.getKey(), getTypeName(metric)));
                writer.newLine();
            }
        }

                System.exit(0);

        for (Scenario scenario : SCENARIOS) {

            final String scenario_name = scenario.getName();
            final Path scenario_home = ScenarioUtils.getScenarioHome(scenario);
            final Path analysis_home = ScenarioUtils.getScenarioAnalysisHome(scenario_name);

            ScenarioUtils.saveScenarioAsJson(scenario, scenario_home);
            Files.createDirectories(analysis_home);

            final List<FileSystem> repetitions_filesystems = ScenarioUtils.getRepetitionsFileSystems(scenario_name);

            final long training_duration_buckets = scenario.getExperimentDuration()
                    .subtract(new Duration(4, TimeUnit.HOURS))
                    .convertTo(TimeUnit.SECONDS)
                    .getLength() / scenario.getObservationInterval()
                    .convertTo(TimeUnit.SECONDS)
                    .getLength();

            getMetricNamesByType(Gauge.class).stream()
                    .forEach(metric_name -> {
                        final List<Stream<GaugeRecord<Double>>> collect = getGaugeRecordStreamsAcrossRepetitions(repetitions_filesystems.stream(), metric_name);
                        final List<List<GaugeRecord<Double>>> repetition_records_as_list = collect.stream()
                                .map(ss -> ss.collect(Collectors.toList()))
                                .collect(Collectors.toList());
                        final Stream<CombinedGaugeRecord<Double>> stream = StatisticalAnalysis.combineGaugeRecords(repetition_records_as_list);
                        try {
                            final Path csv_path = analysis_home.resolve(metric_name + "_over_time.csv");
                            CsvRecords.writeCombinedGaugeRecordsToCsv(stream, csv_path);

                            final Path csv_overall_path = analysis_home.resolve(metric_name + "_overall.csv");

                            final List<Stream<GaugeRecord<Double>>> collect2 = repetition_records_as_list.stream()
                                    .map(l -> l.stream())
                                    .collect(Collectors.toList());
                            CsvRecords.writeOverallToCsv(csv_overall_path, record -> record.getValue(), collect2);

                            final Path csv_overall_path2 = analysis_home.resolve(metric_name + "_overall_before_training.csv");

                            final List<Stream<GaugeRecord<Double>>> collect1 = repetition_records_as_list.stream()
                                    .map(stream1 -> stream1.stream()
                                            .filter(recordd -> recordd.getTimestamp() < training_duration_buckets))
                                    .collect(Collectors.toList());

                            CsvRecords.writeOverallToCsv(csv_overall_path2, record -> record.getValue(), collect1);

                            final Path csv_overall_path3 = analysis_home.resolve(metric_name + "_overall_after_training.csv");
                            final List<Stream<GaugeRecord<Double>>> collect01 = repetition_records_as_list.stream()
                                    .map(stream1 -> stream1.stream()
                                            .filter(record -> record.getTimestamp() > training_duration_buckets))
                                    .collect(Collectors.toList());
                            CsvRecords.writeOverallToCsv(csv_overall_path3, record -> record.getValue(), collect01);
                        }
                        catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                    });

            getMetricNamesByType(Counter.class).stream()
                    .forEach(metric_name -> {
                        final List<Stream<CounterRecord>> collect = getCounterRecordStreamsAcrossRepetitions(repetitions_filesystems.stream(), metric_name);
                        final List<List<CounterRecord>> repetition_records_as_list = collect.stream()
                                .map(ss -> ss.collect(Collectors.toList()))
                                .collect(Collectors.toList());
                        final Stream<CombinedCounterRecord> stream = StatisticalAnalysis.combineCounterRecords(repetition_records_as_list);
                        try {
                            final Path csv_path = analysis_home.resolve(metric_name + "_over_time.csv");
                            CsvRecords.writeCombinedCounterRecordToCsv(stream, csv_path);

                            final Path csv_overall_path = analysis_home.resolve(metric_name + "_overall.csv");
                            final List<Stream<CounterRecord>> collect2 = repetition_records_as_list.stream()
                                    .map(l -> l.stream())
                                    .collect(Collectors.toList());
                            CsvRecords.writeOverallToCsv(csv_overall_path, record -> record.getCount(), collect2);

                            final Path csv_overall_path2 = analysis_home.resolve(metric_name + "_overall_before_training.csv");

                            final List<Stream<CounterRecord>> collect1 = repetition_records_as_list.stream()
                                    .map(stream1 -> stream1.stream()
                                            .filter(recordd -> recordd.getTimestamp() < training_duration_buckets))
                                    .collect(Collectors.toList());

                            CsvRecords.writeOverallToCsv(csv_overall_path2, record -> record.getCount(), collect1);

                            final Path csv_overall_path3 = analysis_home.resolve(metric_name + "_overall_after_training.csv");
                            final List<Stream<CounterRecord>> collect01 = repetition_records_as_list.stream()
                                    .map(stream1 -> stream1.stream()
                                            .filter(record -> record.getTimestamp() > training_duration_buckets))
                                    .collect(Collectors.toList());
                            CsvRecords.writeOverallToCsv(csv_overall_path3, record -> record.getCount(), collect01);
                        }
                        catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });

            getMetricNamesByType(Rate.class).stream()
                    .forEach(metric_name -> {
                        final List<Stream<RateRecord>> collect = getRateRecordStreamsAcrossRepetitions(repetitions_filesystems.stream(), metric_name);
                        final List<List<RateRecord>> repetition_records_as_list = collect.stream()
                                .map(ss -> ss.collect(Collectors.toList()))
                                .collect(Collectors.toList());
                        final Stream<CombinedRateRecord> stream = StatisticalAnalysis.combineRateRecords(repetition_records_as_list);
                        try {
                            final Path csv_path = analysis_home.resolve(metric_name + "_over_time.csv");
                            CsvRecords.writeCombinedRateRecordToCsv(stream, csv_path);

                            final Path csv_overall_path = analysis_home.resolve(metric_name + "_overall.csv");
                            final List<Stream<RateRecord>> collect2 = repetition_records_as_list.stream()
                                    .map(l -> l.stream())
                                    .collect(Collectors.toList());
                            CsvRecords.writeOverallToCsv(csv_overall_path, record -> record.getRate(), collect2);

                            final Path csv_overall_path2 = analysis_home.resolve(metric_name + "_overall_before_training.csv");

                            final List<Stream<RateRecord>> collect1 = repetition_records_as_list.stream()
                                    .map(stream1 -> stream1.stream()
                                            .filter(recordd -> recordd.getTimestamp() < training_duration_buckets))
                                    .collect(Collectors.toList());

                            CsvRecords.writeOverallToCsv(csv_overall_path2, record -> record.getRate(), collect1);

                            final Path csv_overall_path3 = analysis_home.resolve(metric_name + "_overall_after_training.csv");
                            final List<Stream<RateRecord>> collect01 = repetition_records_as_list.stream()
                                    .map(stream1 -> stream1.stream()
                                            .filter(record -> record.getTimestamp() > training_duration_buckets))
                                    .collect(Collectors.toList());
                            CsvRecords.writeOverallToCsv(csv_overall_path3, record -> record.getRate(), collect01);
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    });

            getMetricNamesByType(Sampler.class).stream()
                    .forEach(metric_name -> {
                        final List<Stream<SamplerRecord>> collect = getSamplerRecordStreamsAcrossRepetitions(repetitions_filesystems.stream(), metric_name);
                        final List<List<SamplerRecord>> repetition_records_as_list = collect.stream()
                                .map(ss -> ss.collect(Collectors.toList()))
                                .collect(Collectors.toList());
                        final Stream<CombinedSamplerRecord> stream = StatisticalAnalysis.combineSamplerRecords(repetition_records_as_list);
                        try {
                            final Path csv_path = analysis_home.resolve(metric_name + "_over_time.csv");
                            CsvRecords.writeCombinedSamplerRecordToCsv(stream, csv_path);

                            final Path csv_overall_path = analysis_home.resolve(metric_name + "_overall.csv");
                            final List<Stream<SamplerRecord>> collect2 = repetition_records_as_list.stream()
                                    .map(l -> l.stream())
                                    .collect(Collectors.toList());
                            CsvRecords.writeOverallToCsv(csv_overall_path, record -> record.getMean(), collect2);

                            final Path csv_overall_path2 = analysis_home.resolve(metric_name + "_overall_before_training.csv");

                            final List<Stream<SamplerRecord>> collect1 = repetition_records_as_list.stream()
                                    .map(stream1 -> stream1.stream()
                                            .filter(recordd -> recordd.getTimestamp() < training_duration_buckets))
                                    .collect(Collectors.toList());

                            CsvRecords.writeOverallToCsv(csv_overall_path2, record -> record.getMean(), collect1);

                            final Path csv_overall_path3 = analysis_home.resolve(metric_name + "_overall_after_training.csv");
                            final List<Stream<SamplerRecord>> collect01 = repetition_records_as_list.stream()
                                    .map(stream1 -> stream1.stream()
                                            .filter(record -> record.getTimestamp() > training_duration_buckets))
                                    .collect(Collectors.toList());
                            CsvRecords.writeOverallToCsv(csv_overall_path3, record -> record.getMean(), collect01);
                        }
                        catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });

            getMetricNamesByType(Timer.class).stream()
                    .forEach(metric_name -> {
                        final List<Stream<TimerRecord>> collect = getTimerRecordStreamsAcrossRepetitions(repetitions_filesystems.stream(), metric_name);
                        final List<List<TimerRecord>> repetition_records_as_list = collect.stream()
                                .map(ss -> ss.collect(Collectors.toList()))
                                .collect(Collectors.toList());
                        final Stream<CombinedTimerRecord> stream = StatisticalAnalysis.combineTimerRecords(repetition_records_as_list);
                        try {
                            final Path csv_path = analysis_home.resolve(metric_name + "_over_time.csv");
                            CsvRecords.writeCombinedTimerRecordToCsv(stream, csv_path);

                            final Path csv_overall_path = analysis_home.resolve(metric_name + "_overall.csv");
                            final List<Stream<TimerRecord>> collect2 = repetition_records_as_list.stream()
                                    .map(l -> l.stream())
                                    .collect(Collectors.toList());
                            CsvRecords.writeOverallToCsv(csv_overall_path, record -> record.getMean(), collect2);

                            final Path csv_overall_path2 = analysis_home.resolve(metric_name + "_overall_before_training.csv");

                            final List<Stream<TimerRecord>> collect1 = repetition_records_as_list.stream()
                                    .map(stream1 -> stream1.stream()
                                            .filter(recordd -> recordd.getTimestamp() < training_duration_buckets))
                                    .collect(Collectors.toList());

                            CsvRecords.writeOverallToCsv(csv_overall_path2, record -> record.getMean(), collect1);

                            final Path csv_overall_path3 = analysis_home.resolve(metric_name + "_overall_after_training.csv");
                            final List<Stream<TimerRecord>> collect01 = repetition_records_as_list.stream()
                                    .map(stream1 -> stream1.stream()
                                            .filter(record -> record.getTimestamp() > training_duration_buckets))
                                    .collect(Collectors.toList());
                            CsvRecords.writeOverallToCsv(csv_overall_path3, record -> record.getMean(), collect01);
                        }
                        catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });

            closeFileSystems(repetitions_filesystems);
        }
    }

    private static String getClusteringAlgorithmName(final PeerConfiguration peerConfiguration) {

        final MaintenanceFactory maintenance = peerConfiguration.getMaintenance();
        final Clusterer<EvaluatedDisseminationStrategy> clusterer;

        if (maintenance instanceof EvolutionaryMaintenanceFactory) {
            EvolutionaryMaintenanceFactory evolutionary = (EvolutionaryMaintenanceFactory) maintenance;
            clusterer = evolutionary.getClusterer();
        }
        else if (maintenance instanceof RandomMaintenanceFactory) {
            RandomMaintenanceFactory random = (RandomMaintenanceFactory) maintenance;
            clusterer = random.getClusterer();
        }
        else {
            clusterer = null;
        }

        if (clusterer != null) {
            if (clusterer instanceof KMeansPlusPlusClusterer) {
                return "KMeans++";
            }
            if (clusterer instanceof PFClustClusterer) {
                return "PFClust";
            }
            if (clusterer instanceof PerPointClusterer) {
                return "None";
            }
        }

        return "None";

    }

    private static String getTypeName(final Metric metric) {

        final Class<? extends Metric> metric_class = metric.getClass();
        for (Class<? extends Metric> metric_type : METRIC_TYPES) {
            if (metric_type.isAssignableFrom(metric_class)) {
                return metric_type.getSimpleName();
            }
        }

        return metric_class.getSimpleName();
    }

    private static String shortName(final Churn churn) {

        if (churn.equals(Churn.NONE)) {
            return "None";
        }
        if (churn.equals(Constants.CHURN_15_MIN)) {
            return "A";
        }
        if (churn.equals(Constants.CHURN_30_MIN)) {
            return "B";
        }
        if (churn.equals(Constants.CHURN_1_HOUR)) {
            return "C";
        }
        if (churn.equals(Constants.CHURN_OSCILLATING)) {
            return "Oscillating";
        }

        return churn.toString();
    }

    private static String shortName(final Workload workload) {

        if (workload.equals(Workload.NONE)) {
            return "None";
        }
        if (workload.equals(Constants.WORKLOAD_10_MIN)) {
            return "Light";
        }
        if (workload.equals(Constants.WORKLOAD_10_SEC)) {
            return "Medium";
        }
        if (workload.equals(Constants.WORKLOAD_1_SEC)) {
            return "Heavy";
        }
        if (workload.equals(Constants.WORKLOAD_OSCILLATING)) {
            return "Oscillating";
        }

        return workload.toString();
    }

    private static String shortName(final PeerConfiguration configuration) {

        final MaintenanceFactory maintenance = configuration.getMaintenance();
        if (configuration.equals(Constants.CHORD)) {
            return "Chord";
        }
        if (configuration.equals(Constants.TROMBONE_NO_MAINTENANCE)) {
            return "Trombone None";
        }
        if (configuration.equals(Constants.TROMBONE_STABILISATION)) {
            return "Trombone Fixed";
        }
        if (maintenance instanceof EvolutionaryMaintenanceFactory) {
            return "Trombone Adaptive GA";
        }
        if (maintenance instanceof RandomMaintenanceFactory) {
            return "Trombone Adaptive Random";
        }

        return configuration.toString();
    }

    private static void closeFileSystems(final List<FileSystem> repetitions_filesystems) throws IOException {

        final Iterator<FileSystem> fs_iterator = repetitions_filesystems.stream()
                .iterator();

        while (fs_iterator.hasNext()) {
            FileSystem system = fs_iterator.next();
            system.close();
        }
    }

    private static List<String> getMetricNamesByType(Class<? extends Metric> metric_type) {

        return new TromboneMetricSet(null).getMetrics()
                .entrySet()
                .stream()
                .filter(entry -> metric_type.isInstance(entry.getValue()))
                .map(entry -> entry.getKey())
                .collect(Collectors.toList());
    }

    private static List<Stream<GaugeRecord<Double>>> getGaugeRecordStreamsAcrossRepetitions(final Stream<FileSystem> repetitions_filesystems, final String metric_name) {

        return repetitions_filesystems.map(file_system -> {

            try {
                return CsvRecords.readGaugeRecords(file_system.getPath("1", metric_name + ".csv"));
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        })
                .collect(Collectors.toList());
    }

    private static List<Stream<CounterRecord>> getCounterRecordStreamsAcrossRepetitions(final Stream<FileSystem> repetitions_filesystems, final String metric_name) {

        return repetitions_filesystems.map(file_system -> {

            try {
                return CsvRecords.readCounterRecords(file_system.getPath("1", metric_name + ".csv"));
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        })
                .collect(Collectors.toList());
    }

    private static List<Stream<RateRecord>> getRateRecordStreamsAcrossRepetitions(final Stream<FileSystem> repetitions_filesystems, final String metric_name) {

        return repetitions_filesystems.map(file_system -> {

            try {
                return CsvRecords.readRateRecords(file_system.getPath("1", metric_name + ".csv"));
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        })
                .collect(Collectors.toList());
    }

    private static List<Stream<SamplerRecord>> getSamplerRecordStreamsAcrossRepetitions(final Stream<FileSystem> repetitions_filesystems, final String metric_name) {

        return repetitions_filesystems.map(file_system -> {

            try {
                return CsvRecords.readSamplerRecords(file_system.getPath("1", metric_name + ".csv"));
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        })
                .collect(Collectors.toList());
    }

    private static List<Stream<TimerRecord>> getTimerRecordStreamsAcrossRepetitions(final Stream<FileSystem> repetitions_filesystems, final String metric_name) {

        return repetitions_filesystems.map(file_system -> {

            try {
                return CsvRecords.readTimerRecords(file_system.getPath("1", metric_name + ".csv"));
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        })
                .collect(Collectors.toList());
    }

    private static void updateRepetitionsList(final Path path) throws IOException {

        final JSONArray repetitions_path = new JSONArray();
        final List<Path> repetitions = FileSystemUtils.getMatchingFiles(path, path.getFileSystem()
                .getPathMatcher("glob:**/repetitions/*.zip"));
        for (Path repetition : repetitions) {
            repetitions_path.put(repetition.getFileName()
                    .toString());
        }

        Files.write(path.resolve("repetitions/repetitions.json"), repetitions_path.toString(4)
                .getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
    }
}
