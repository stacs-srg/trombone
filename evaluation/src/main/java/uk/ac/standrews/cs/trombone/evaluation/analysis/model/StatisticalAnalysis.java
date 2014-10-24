package uk.ac.standrews.cs.trombone.evaluation.analysis.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.commons.math3.stat.interval.ConfidenceInterval;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public final class StatisticalAnalysis {

    public static final double CONFIDENCE_LEVEL = 0.95;

    private StatisticalAnalysis() {

    }

    // combine sampler and timer https://www.statstodo.com/ComMeans_Pgm.php

    public static Stream<CombinedGaugeRecord<Double>> combineGaugeRecords(final Collection<List<GaugeRecord<Double>>> counter_records) {

        final List<Iterator<GaugeRecord<Double>>> iterators = counter_records.stream()
                .map(counter_record -> counter_record.iterator())
                .collect(Collectors.toList());

        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new Iterator<CombinedGaugeRecord<Double>>() {

            @Override
            public boolean hasNext() {

                for (Iterator<GaugeRecord<Double>> iterator : iterators) {
                    if (!iterator.hasNext()) {
                        return false;
                    }
                }

                return true;
            }

            @Override
            public CombinedGaugeRecord<Double> next() {

                final List<GaugeRecord<Double>> records = new ArrayList<>();
                final SummaryStatistics statistics = new SummaryStatistics();

                for (Iterator<GaugeRecord<Double>> iterator : iterators) {
                    final GaugeRecord<Double> record = iterator.next();
                    records.add(record);
                    statistics.addValue(record.getValue());
                }

                final double mean = statistics.getMean();
                final long timestamp = records.stream()
                        .mapToLong(GaugeRecord:: getTimestamp)
                        .findFirst()
                        .getAsLong();
                final Optional<ConfidenceInterval> ci = getConfidenceInterval(statistics, CONFIDENCE_LEVEL);
                final CombinedGaugeRecord<Double> combined_record = new CombinedGaugeRecord<>();
                combined_record.setTimestamp(timestamp);
                combined_record.setMeanValue(mean);
                combined_record.setValueConfidenceInterval(ci);
                return combined_record;
            }
        }, Spliterator.ORDERED), false);
    }

    public static Stream<CombinedCounterRecord> combineCounterRecords(final Collection<List<CounterRecord>> counter_records) {

        final List<Iterator<CounterRecord>> iterators = counter_records.stream()
                .map(counter_record -> counter_record.iterator())
                .collect(Collectors.toList());

        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new Iterator<CombinedCounterRecord>() {

            @Override
            public boolean hasNext() {

                for (Iterator<CounterRecord> iterator : iterators) {
                    if (!iterator.hasNext()) {
                        return false;
                    }
                }

                return true;
            }

            @Override
            public CombinedCounterRecord next() {

                final List<CounterRecord> records = new ArrayList<>();
                final SummaryStatistics statistics = new SummaryStatistics();

                for (Iterator<CounterRecord> iterator : iterators) {
                    final CounterRecord record = iterator.next();
                    records.add(record);
                    statistics.addValue(record.getCount());
                }

                final double mean = statistics.getMean();
                final long timestamp = records.stream()
                        .mapToLong(CounterRecord:: getTimestamp)
                        .findFirst()
                        .getAsLong();
                final Optional<ConfidenceInterval> ci = getConfidenceInterval(statistics, CONFIDENCE_LEVEL);
                final CombinedCounterRecord combined_record = new CombinedCounterRecord();
                combined_record.setTimestamp(timestamp);
                combined_record.setMeanCount(mean);
                combined_record.setCountConfidenceInterval(ci);
                return combined_record;
            }
        }, Spliterator.ORDERED), false);
    }

    public static Stream<CombinedRateRecord> combineRateRecords(final Collection<List<RateRecord>> rate_records) {

        final List<Iterator<RateRecord>> iterators = rate_records.stream()
                .map(rate_record -> rate_record.iterator())
                .collect(Collectors.toList());

        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new Iterator<CombinedRateRecord>() {

            @Override
            public boolean hasNext() {

                for (Iterator<RateRecord> iterator : iterators) {
                    if (!iterator.hasNext()) {
                        return false;
                    }
                }

                return true;
            }

            @Override
            public CombinedRateRecord next() {

                final List<RateRecord> records = new ArrayList<>();
                final SummaryStatistics count_statistics = new SummaryStatistics();
                final SummaryStatistics rate_statistics = new SummaryStatistics();

                for (Iterator<RateRecord> iterator : iterators) {
                    final RateRecord record = iterator.next();
                    records.add(record);
                    count_statistics.addValue(record.getCount());
                    rate_statistics.addValue(record.getRate());
                }

                final double mean = count_statistics.getMean();
                final long timestamp = records.stream()
                        .mapToLong(RateRecord:: getTimestamp)
                        .findFirst()
                        .getAsLong();

                final TimeUnit unit = records.stream()
                        .map(RateRecord:: getUnit)
                        .findFirst()
                        .get();

                final Optional<ConfidenceInterval> count_ci = getConfidenceInterval(count_statistics, CONFIDENCE_LEVEL);
                final Optional<ConfidenceInterval> rate_ci = getConfidenceInterval(rate_statistics, CONFIDENCE_LEVEL);
                final CombinedRateRecord combined_record = new CombinedRateRecord();
                combined_record.setTimestamp(timestamp);
                combined_record.setMeanCount(mean);
                combined_record.setMeanRate(rate_statistics.getMean());
                combined_record.setCountConfidenceInterval(count_ci);
                combined_record.setRateConfidenceInterval(rate_ci);
                combined_record.setUnit(unit);

                return combined_record;
            }
        }, Spliterator.ORDERED), false);
    }

    public static Stream<CombinedSamplerRecord> combineSamplerRecords(final Collection<List<SamplerRecord>> sampler_records) {

        final List<Iterator<SamplerRecord>> iterators = sampler_records.stream()
                .map(sampler_record -> sampler_record.iterator())
                .collect(Collectors.toList());

        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new Iterator<CombinedSamplerRecord>() {

            @Override
            public boolean hasNext() {

                for (Iterator<SamplerRecord> iterator : iterators) {
                    if (!iterator.hasNext()) {
                        return false;
                    }
                }

                return true;
            }

            @Override
            public CombinedSamplerRecord next() {

                final List<SamplerRecord> records = new ArrayList<>();
                final SummaryStatistics count_statistics = new SummaryStatistics();
                final SummaryStatistics min_statistics = new SummaryStatistics();
                final SummaryStatistics mean_statistics = new SummaryStatistics();
                final SummaryStatistics max_statistics = new SummaryStatistics();

                for (Iterator<SamplerRecord> iterator : iterators) {
                    final SamplerRecord record = iterator.next();
                    records.add(record);
                    count_statistics.addValue(record.getCount());
                    min_statistics.addValue(record.getMin());
                    mean_statistics.addValue(record.getMean());
                    max_statistics.addValue(record.getMax());
                }

                final long combined_count = records.stream()
                        .mapToLong(record -> {
                            return record.getCount();
                        })
                        .sum();

                final double combined_sums = records.stream()
                        .mapToDouble(record -> {
                            return record.getMean() * record.getCount();
                        })
                        .sum();
                final double combined_sum_squares = records.stream()
                        .mapToDouble(record -> {

                            return Math.pow(record.getStandardDeviation(), 2) * (record.getCount() - 1) + Math.pow(record.getMean() * record.getCount(), 2) / record.getCount();
                        })
                        .sum();

                final double combined_mean = combined_sums / combined_count;
                final double combined_sd = Math.sqrt((combined_sum_squares - Math.pow(combined_sums, 2) / combined_count) / (combined_count - 1));

                final double mean = count_statistics.getMean();
                final long timestamp = records.stream()
                        .mapToLong(SamplerRecord:: getTimestamp)
                        .findFirst()
                        .getAsLong();

                final Optional<ConfidenceInterval> count_ci = getConfidenceInterval(count_statistics, CONFIDENCE_LEVEL);
                final Optional<ConfidenceInterval> mean_ci = getConfidenceInterval(mean_statistics, CONFIDENCE_LEVEL);
                final CombinedSamplerRecord combined_record = new CombinedSamplerRecord();
                combined_record.setTimestamp(timestamp);
                combined_record.setMeanCount(mean);
                combined_record.setCountConfidenceInterval(count_ci);
                combined_record.setMeanConfidenceInterval(mean_ci);
                combined_record.setMaxOfMaxes(max_statistics.getMax());
                combined_record.setMinOfMins(min_statistics.getMin());
                combined_record.setMeanOfMeans(mean_statistics.getMean());
                combined_record.setOverallStandardDeviation(combined_sd);
                combined_record.setOverallMean(combined_mean);
                combined_record.setOverallConfidenceInterval(getConfidenceInterval(CONFIDENCE_LEVEL, combined_sd, combined_count, combined_mean));
                return combined_record;
            }
        }, Spliterator.ORDERED), false);
    }

    public static Stream<CombinedTimerRecord> combineTimerRecords(final Collection<List<TimerRecord>> timer_records) {

        final List<Iterator<TimerRecord>> iterators = timer_records.stream()
                .map(timer_record -> timer_record.iterator())
                .collect(Collectors.toList());

        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new Iterator<CombinedTimerRecord>() {

            @Override
            public boolean hasNext() {

                for (Iterator<TimerRecord> iterator : iterators) {
                    if (!iterator.hasNext()) {
                        return false;
                    }
                }

                return true;
            }

            @Override
            public CombinedTimerRecord next() {

                final List<TimerRecord> records = new ArrayList<>();
                final SummaryStatistics count_statistics = new SummaryStatistics();
                final SummaryStatistics min_statistics = new SummaryStatistics();
                final SummaryStatistics mean_statistics = new SummaryStatistics();
                final SummaryStatistics max_statistics = new SummaryStatistics();

                for (Iterator<TimerRecord> iterator : iterators) {
                    final TimerRecord record = iterator.next();
                    records.add(record);
                    count_statistics.addValue(record.getCount());
                    min_statistics.addValue(record.getMin());
                    mean_statistics.addValue(record.getMean());
                    max_statistics.addValue(record.getMax());
                }

                final long combined_count = records.stream()
                        .mapToLong(record -> {
                            return record.getCount();
                        })
                        .sum();

                final double combined_sums = records.stream()
                        .mapToDouble(record -> {
                            return record.getMean() * record.getCount();
                        })
                        .sum();
                final double combined_sum_squares = records.stream()
                        .mapToDouble(record -> {

                            return Math.pow(record.getStandardDeviation(), 2) * (record.getCount() - 1) + Math.pow(record.getMean() * record.getCount(), 2) / record.getCount();
                        })
                        .sum();

                final double combined_mean = combined_sums / combined_count;
                final double combined_sd = Math.sqrt((combined_sum_squares - Math.pow(combined_sums, 2) / combined_count) / (combined_count - 1));

                final double mean = count_statistics.getMean();
                final long timestamp = records.stream()
                        .mapToLong(TimerRecord:: getTimestamp)
                        .findFirst()
                        .getAsLong();

                final TimeUnit unit = records.stream()
                        .map(TimerRecord:: getUnit)
                        .findFirst()
                        .get();

                final Optional<ConfidenceInterval> count_ci = getConfidenceInterval(count_statistics, CONFIDENCE_LEVEL);
                final Optional<ConfidenceInterval> mean_ci = getConfidenceInterval(mean_statistics, CONFIDENCE_LEVEL);
                final CombinedTimerRecord combined_record = new CombinedTimerRecord();
                combined_record.setTimestamp(timestamp);
                combined_record.setMeanCount(mean);
                combined_record.setCountConfidenceInterval(count_ci);
                combined_record.setMeanConfidenceInterval(mean_ci);
                combined_record.setMaxOfMaxes(max_statistics.getMax());

                combined_record.setMinOfMins(min_statistics.getMin());
                combined_record.setMeanOfMeans(mean_statistics.getMean());
                combined_record.setOverallStandardDeviation(combined_sd);
                combined_record.setOverallMean(combined_mean);
                combined_record.setOverallConfidenceInterval(getConfidenceInterval(CONFIDENCE_LEVEL, combined_sd, combined_count, combined_mean));
                combined_record.setUnit(unit);
                return combined_record;
            }
        }, Spliterator.ORDERED), false);
    }

    public static Optional<ConfidenceInterval> getConfidenceInterval(SummaryStatistics statistics, double confidence_level) {

        final double standardDeviation = statistics.getStandardDeviation();
        final long sample_size = statistics.getN();
        final double mean = statistics.getMean();

        return getConfidenceInterval(confidence_level, standardDeviation, sample_size, mean);
    }

    private static Optional<ConfidenceInterval> getConfidenceInterval(final double confidence_level, final double standardDeviation, final long sample_size, final double mean) {

        if (sample_size > 1 && standardDeviation > 0) {

            final long degreesOfFreedom = sample_size - 1;
            final TDistribution distribution = new TDistribution(degreesOfFreedom);
            final double critical_value = distribution.inverseCumulativeProbability(1.0 - (1 - confidence_level) / 2);
            final double interval = critical_value * standardDeviation / Math.sqrt(sample_size);

            return Optional.of(new ConfidenceInterval(mean - interval, mean + interval, confidence_level));
        }
        return Optional.empty();
    }
}
