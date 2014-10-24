package uk.ac.standrews.cs.trombone.event;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import org.apache.commons.math3.stat.descriptive.SynchronizedDescriptiveStatistics;
import org.mashti.gauge.Counter;
import org.mashti.gauge.Gauge;
import org.mashti.gauge.MetricSet;
import org.mashti.gauge.Rate;
import org.mashti.gauge.Sampler;
import org.mashti.gauge.Timer;
import org.mashti.gauge.jvm.GarbageCollectorCpuUsageGauge;
import org.mashti.gauge.jvm.MemoryUsageGauge;
import org.mashti.gauge.jvm.SystemLoadAverageGauge;
import org.mashti.gauge.jvm.ThreadCountGauge;
import org.mashti.gauge.jvm.ThreadCpuUsageGauge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.trombone.core.Peer;
import uk.ac.standrews.cs.trombone.core.PeerMetric;
import uk.ac.standrews.cs.trombone.core.PeerReference;
import uk.ac.standrews.cs.trombone.core.maintenance.DisseminationStrategy;
import uk.ac.standrews.cs.trombone.core.maintenance.EvaluatedDisseminationStrategy;
import uk.ac.standrews.cs.trombone.core.maintenance.EvolutionaryMaintenance;
import uk.ac.standrews.cs.trombone.core.maintenance.Maintenance;
import uk.ac.standrews.cs.trombone.core.maintenance.StrategicMaintenance;
import uk.ac.standrews.cs.trombone.core.state.PeerState;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public final class TromboneMetricSet extends MetricSet {

    private static final Logger LOGGER = LoggerFactory.getLogger(TromboneMetricSet.class);

    final Rate lookup_execution_rate = new Rate();
    final Rate lookup_failure_rate = new Rate();
    final Sampler lookup_failure_hop_count_sampler = new Sampler();
    final Sampler lookup_failure_retry_count_sampler = new Sampler();
    final Timer lookup_failure_delay_timer = new Timer();
    final Rate lookup_correctness_rate = new Rate();
    final Sampler lookup_correctness_hop_count_sampler = new Sampler();
    final Sampler lookup_correctness_retry_count_sampler = new Sampler();
    final Timer lookup_correctness_delay_timer = new Timer();
    final Rate lookup_incorrectness_rate = new Rate();
    final Sampler lookup_incorrectness_hop_count_sampler = new Sampler();
    final Sampler lookup_incorrectness_retry_count_sampler = new Sampler();
    final Timer lookup_incorrectness_delay_timer = new Timer();
    final Counter available_peer_counter = new Counter();
    final Rate peer_arrival_rate = new Rate();
    final Rate peer_departure_rate = new Rate();
    final Sampler event_execution_lag_sampler = new Sampler();
    final Timer event_execution_duration_timer = new Timer();
    final Rate event_scheduling_rate = new Rate();
    final Rate event_completion_rate = new Rate();
    final Rate join_failure_rate = new Rate();
    final Rate join_success_rate = new Rate();

    private final Gauge<Double> state_correctness_counter = new Gauge<Double>() {

        @Override
        public Double get() {

            double correct_state_size = 0;
            double state_size = 0;
            for (Participant participant : event_executor.event_reader.getParticipants()) {
                Peer peer = participant.getPeer();
                if (peer.isExposed()) {

                    for (PeerReference reference : peer.getPeerState()
                            .getReferences()) {
                        if (event_executor.event_reader.isAlive(reference)) {

                            correct_state_size++;
                        }
                        state_size++;
                    }
                }
            }

            return correct_state_size / state_size;
        }
    };
    private final Gauge<Double> state_completeness_counter = new Gauge<Double>() {

        @Override
        public Double get() {

            double correct_state_size = 0;
            for (Participant participant : event_executor.event_reader.getParticipants()) {
                Peer peer = participant.getPeer();
                if (peer.isExposed()) {

                    for (PeerReference reference : peer.getPeerState()
                            .getReferences()) {
                        if (event_executor.event_reader.isAlive(reference)) {

                            correct_state_size++;
                        }
                    }
                }
            }

            final long available_peer_count = available_peer_counter.get();
            return available_peer_count != 0 ? correct_state_size / available_peer_count / available_peer_count : 0;
        }
    };

    private final Gauge<Double> lookup_correctness_ratio = new Gauge<Double>() {

        @Override
        public Double get() {

            return (double) lookup_correctness_rate.getCount() / lookup_execution_rate.getCount();
        }
    };
    private final Gauge<Double> sent_bytes_per_alive_peer_per_second_gauge = new Gauge<Double>() {

        @Override
        public Double get() {

            final long available_peer_count = available_peer_counter.get();
            return available_peer_count != 0 ? PeerMetric.getGlobalSentBytesRate()
                    .getRate() / available_peer_count : 0;
        }
    };
    private final ThreadCountGauge thread_count_gauge = new ThreadCountGauge();
    private final SystemLoadAverageGauge system_load_average_gauge = new SystemLoadAverageGauge();
    private final ThreadCpuUsageGauge thread_cpu_usage_gauge = new ThreadCpuUsageGauge();
    private final GarbageCollectorCpuUsageGauge gc_cpu_usage_gauge = new GarbageCollectorCpuUsageGauge();
    private final MemoryUsageGauge memory_usage_gauge = new MemoryUsageGauge();
    private final Gauge<Integer> event_executor_queue_size = new Gauge<Integer>() {

        @Override
        public Integer get() {

            return event_executor.task_executor.getQueue()
                    .size();
        }
    };
    private final Gauge<Double> state_size_per_alive_peer_gauge = new Gauge<Double>() {

        @Override
        public Double get() {

            double number_of_reachable_state = 0;
            for (Participant participant : event_executor.event_reader.getParticipants()) {
                Peer peer = participant.getPeer();
                if (peer.isExposed()) {
                    final PeerState state = peer.getPeerState();
                    number_of_reachable_state += state.size();
                }
            }

            final long available_peer_count = available_peer_counter.get();
            return available_peer_count != 0 ? number_of_reachable_state / available_peer_count : 0;
        }
    };

    private final Sampler strategy_uniformity_sampler = new Sampler() {

        @Override
        protected SynchronizedDescriptiveStatistics get() {

            final HashMultiset<DisseminationStrategy> strategies = HashMultiset.create();
            for (Participant participant : event_executor.event_reader.getParticipants()) {
                Peer peer = participant.getPeer();
                if (peer.isExposed()) {
                    final Maintenance maintenance = peer.getMaintenance();
                    if (maintenance instanceof StrategicMaintenance) {
                        StrategicMaintenance strategicMaintenance = (StrategicMaintenance) maintenance;
                        strategies.add(strategicMaintenance.getDisseminationStrategy());
                    }
                }
            }
            final SynchronizedDescriptiveStatistics statistics = new SynchronizedDescriptiveStatistics(10000);
            for (Multiset.Entry<DisseminationStrategy> entry : strategies.entrySet()) {
                statistics.addValue(entry.getCount());
            }
            return statistics;
        }

        @Override
        public SynchronizedDescriptiveStatistics getAndReset() {

            return get();
        }

        @Override
        public void update(final double sample) {

            LOGGER.warn("update is unsupported by this sampler");
        }
    };

    private final Sampler generated_strategy_uniformity_sampler = new Sampler() {

        @Override
        protected SynchronizedDescriptiveStatistics get() {

            final HashMultiset<DisseminationStrategy> strategies = HashMultiset.create();
            for (Participant participant : event_executor.event_reader.getParticipants()) {
                Peer peer = participant.getPeer();
                if (peer.isExposed()) {

                    final Maintenance maintainer = peer.getMaintenance();
                    if (maintainer instanceof EvolutionaryMaintenance) {
                        EvolutionaryMaintenance evolutionary_maintainer = (EvolutionaryMaintenance) maintainer;

                        for (EvaluatedDisseminationStrategy evaluated_strategy : evolutionary_maintainer.getEvaluatedStrategies()) {
                            strategies.add(evaluated_strategy.getStrategy());
                        }
                    }
                }
            }
            final SynchronizedDescriptiveStatistics statistics = new SynchronizedDescriptiveStatistics(10000);
            for (Multiset.Entry<DisseminationStrategy> entry : strategies.entrySet()) {
                statistics.addValue(entry.getCount());
            }
            return statistics;
        }

        @Override
        public SynchronizedDescriptiveStatistics getAndReset() {

            return get();
        }

        @Override
        public void update(final double sample) {

            LOGGER.warn("update is unsupported by this sampler");
        }
    };

    private final Gauge<Integer> queue_size_gauge = new Gauge<Integer>() {

        @Override
        public Integer get() {

            return event_executor.runnable_events.size();
        }
    };
    private final EventExecutor event_executor;

    public TromboneMetricSet(EventExecutor event_executor) {

        this.event_executor = event_executor;
        putMetric("lookup_correctness_ratio", lookup_correctness_ratio);
        putMetric("lookup_execution_rate", lookup_execution_rate);
        putMetric("lookup_failure_rate", lookup_failure_rate);
        putMetric("lookup_failure_hop_count_sampler", lookup_failure_hop_count_sampler);
        putMetric("lookup_failure_retry_count_sampler", lookup_failure_retry_count_sampler);
        putMetric("lookup_failure_delay_timer", lookup_failure_delay_timer);
        putMetric("lookup_correctness_rate", lookup_correctness_rate);
        putMetric("lookup_correctness_hop_count_sampler", lookup_correctness_hop_count_sampler);
        putMetric("lookup_correctness_retry_count_sampler", lookup_correctness_retry_count_sampler);
        putMetric("lookup_correctness_delay_timer", lookup_correctness_delay_timer);
        putMetric("lookup_incorrectness_rate", lookup_incorrectness_rate);
        putMetric("lookup_incorrectness_hop_count_sampler", lookup_incorrectness_hop_count_sampler);
        putMetric("lookup_incorrectness_retry_count_sampler", lookup_incorrectness_retry_count_sampler);
        putMetric("lookup_incorrectness_delay_timer", lookup_incorrectness_delay_timer);
        putMetric("available_peer_counter", available_peer_counter);
        putMetric("peer_arrival_rate", peer_arrival_rate);
        putMetric("peer_departure_rate", peer_departure_rate);
        putMetric("sent_bytes_per_alive_peer_per_second_gauge", sent_bytes_per_alive_peer_per_second_gauge);
        putMetric("sent_bytes_rate", PeerMetric.getGlobalSentBytesRate());
        putMetric("event_executor_queue_size", event_executor_queue_size);
        putMetric("event_execution_lag_sampler", event_execution_lag_sampler);
        putMetric("event_execution_duration_timer", event_execution_duration_timer);
        putMetric("event_scheduling_rate", event_scheduling_rate);
        putMetric("event_completion_rate", event_completion_rate);
        putMetric("join_failure_rate", join_failure_rate);
        putMetric("join_success_rate", join_success_rate);
        putMetric("queue_size_gauge", queue_size_gauge);
        putMetric("state_size_per_alive_peer_gauge", state_size_per_alive_peer_gauge);
        putMetric("thread_count_gauge", thread_count_gauge);
        putMetric("system_load_average_gauge", system_load_average_gauge);
        putMetric("thread_cpu_usage_gauge", thread_cpu_usage_gauge);
        putMetric("gc_cpu_usage_gauge", gc_cpu_usage_gauge);
        putMetric("memory_usage_gauge", memory_usage_gauge);
        putMetric("evolutionary_maintenance_cluster_count_sampler", EvolutionaryMaintenance.CLUSTER_COUNT_SAMPLER);
        putMetric("evolutionary_maintenance_cluster_size_sampler", EvolutionaryMaintenance.CLUSTER_SIZE_SAMPLER);
        putMetric("evolutionary_maintenance_fitness_sampler", EvolutionaryMaintenance.FITNESS_SAMPLER);
        putMetric("evolutionary_maintenance_normalized_fitness_sampler", EvolutionaryMaintenance.NORMALIZED_FITNESS_SAMPLER);
        putMetric("evolutionary_maintenance_weighted_fitness_sampler", EvolutionaryMaintenance.WEIGHTED_FITNESS_SAMPLER);
        putMetric("rpc_error_rate", PeerMetric.getGlobalRPCErrorRate());
        putMetric("reconfiguration_rate", StrategicMaintenance.RECONFIGURATION_RATE);
        putMetric("strategy_action_size_sampler", EvolutionaryMaintenance.STRATEGY_ACTION_SIZE_SAMPLER);
        putMetric("strategy_uniformity_sampler", strategy_uniformity_sampler);
        putMetric("generated_strategy_uniformity_sampler", generated_strategy_uniformity_sampler);
        putMetric("state_correctness_counter", state_correctness_counter);
        putMetric("state_completeness_counter", state_completeness_counter);

    }

}
