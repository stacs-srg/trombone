define(['util', 'mark', 'config/theme', 'data', 'series/double', 'series/verses', 'series/single'], function (util, mark, theme, data, double, verses, single) {

    var observations = [
        {
            title: "Event Completion Rate",
            yAxis: {
                title: {
                    text: "Number of Events Executed Per Second"
                }
            },
            series_provider: double("event_completion_rate.csv")
        },
        {
            title: "Network Size",
            series_provider: double("available_peer_counter.csv"),
            yAxis: {
                title: {text: "Number of Available Peers "}
            }
        },
        {
            title: "Event Execution Duration Timer",
            series_provider: double("event_execution_duration_timer.csv"),
            yAxis: {
                title: {text: "Mean Event Execution Completion Time (ms)"}
            }
        },
        {
            title: "Event Execution Lag",
            series_provider: double("event_execution_lag_sampler.csv"),
            yAxis: {
                title: {text: "Event Execution Lag (ms)"}
            }
        },
        {
            title: "Event Executor Queue Size",
            series_provider: double("event_executor_queue_size.csv"),
            yAxis: {
                title: {text: "Number of Events Queued by Executor"}
            }
        },
        {
            title: "Event Scheduling Rate",
            series_provider: double("event_scheduling_rate.csv"),
            yAxis: {
                title: {text: "Number of Events Scheduled for Execution"}
            }

        },
        {
            title: "Join Failure Rate",
            series_provider: double("join_failure_rate.csv"),
            yAxis: {
                title: {text: "Number of Failed Joins"}
            }
        },
        {
            title: "Join Success Rate",
            series_provider: double("join_success_rate.csv"),
            yAxis: {
                title: {text: "Number of Successful Joins"}
            }
        },
        {
            title: "Lookup Correctness Delay",
            series_provider: double("lookup_correctness_delay_timer.csv"),
            yAxis: {
                title: {text: "Mean Correct Lookup Delay (ms)"}
            }
        },
        {
            title: "Lookup Correctness Hop Count",
            series_provider: double("lookup_correctness_hop_count_sampler.csv"),
            yAxis: {
                title: {text: "Mean Correct Lookup Hop Count"}
            }
        },
        {
            title: "Lookup Correctness Rate",
            series_provider: double("lookup_correctness_rate.csv"),
            chart: {type: "line"},
            yAxis: {
                title: {text: "Number of Correct Lookups Per Second"}
            }
        },
        {
            title: "Lookup Correctness Retry Count",
            series_provider: double("lookup_correctness_retry_count_sampler.csv"),
            yAxis: {
                title: {text: "Mean Correct Lookup Retry Count"}
            }
        },
        {
            title: "Lookup Execution Rate",
            series_provider: double("lookup_execution_rate.csv"),
            yAxis: {
                title: {text: "Number of Lookups Executed Per Second"}
            }
        },
        {
            title: "Lookup Failure Delay",
            series_provider: double("lookup_failure_delay_timer.csv"),
            yAxis: {
                title: {text: "Mean Failed Lookup Delay (ms)"}
            }
        },
        {
            title: "Lookup Failure Hop Count",
            series_provider: double("lookup_failure_hop_count_sampler.csv"),
            yAxis: {
                title: {text: "Mean Failed Lookup Hop Count"}
            }
        },
        {
            title: "Lookup Failure Rate",
            series_provider: double("lookup_failure_rate.csv"),
            yAxis: {
                title: {text: "Number of Failed Lookups per Second"}
            }
        },
        {
            title: "Lookup Failure Retry Count",
            series_provider: double("lookup_failure_retry_count_sampler.csv"),
            yAxis: {
                title: {text: "Mean Failed Lookup Retry Count"}
            }
        },
        {
            title: "Lookup Incorrectness Delay Timer",
            series_provider: double("lookup_incorrectness_delay_timer.csv"),
            chart: {type: "line"},
            yAxis: {
                title: {text: "Mean Incorrect Lookup Delay (ms)"}
            }
        },
        {
            title: "Lookup Incorrectness hop Count Sampler",
            series_provider: double("lookup_incorrectness_hop_count_sampler.csv"),
            chart: {type: "line"},
            yAxis: {
                title: {text: "Mean Incorrect Lookup Hop Count"}
            }
        },
        {
            title: "Lookup Incorrectness Rate",
            series_provider: double("lookup_incorrectness_rate.csv"),
            chart: {type: "line"},
            yAxis: {
                title: {text: "Number of Incorrect Lookups Per Second"}
            }
        },
        {
            title: "Lookup Incorrectness Retry Count Sampler",
            series_provider: double("lookup_incorrectness_retry_count_sampler.csv"),
            chart: {type: "line"},
            yAxis: {
                title: {text: "Mean Incorrect Lookup Retry Count"}
            }
        },
        {
            title: "JVM Heap + Non-heap Memory Usage",
            series_provider: double("memory_usage_gauge.csv", {
                0: util.convert.secondToHour,
                1: util.convert.byteToGigabyte,
                2: util.convert.byteToGigabyte,
                3: util.convert.byteToGigabyte
            }),
            yAxis: {
                title: {text: "Heap and Non-heap Memory usage (GB)"}
            }
        },
        {
            title: "Peer Arrival Rate",
            series_provider: double("peer_arrival_rate.csv"),
            chart: {type: "line"},
            yAxis: {
                title: {text: "Number of Peers Arrived Per Second"}
            }
        },
        {
            title: "Peer Departure Rate",
            series_provider: double("peer_departure_rate.csv"),
            chart: {type: "line"},
            yAxis: {
                title: {text: "Number of Peers Departed Per Second"}
            }
        },
        {
            title: "Generated Events Queue Size",
            series_provider: double("queue_size_gauge.csv"),
            chart: {type: "line"},
            yAxis: {
                title: {text: "Number of Generated Events"}
            }
        },
        {
            title: "State Size: Reachable per Alive Peer",
            series_provider: double("reachable_state_size_per_alive_peer_gauge.csv"),
            chart: {type: "line"},
            yAxis: {
                title: {text: "Number of Reachable State Per Alive Peer"}
            }
        },
        {
            title: "Sent Bytes Rate",
            series_provider: double("sent_bytes_rate.csv"),
            yAxis: {
                title: {text: "Bytes Sent Per Second"}
            }
        },
        {
            title: "Sent Bytes Per Alive Peer Per Second",
            series_provider: double("sent_bytes_per_alive_peer_per_second_gauge.csv"),
            yAxis: {
                title: {text: "Bytes Sent Per Alive node Per Second"}
            }
        },
        {
            title: "JVM System Load Average Over One Minute",
            series_provider: double("system_load_average_gauge.csv"),
            yAxis: {
                title: {text: "Load Average Over One Minute"}
            }
        },
        {
            title: "JVM Alive Thread Count",
            series_provider: double("thread_count_gauge.csv"),
            chart: {type: "line"},
            yAxis: {
                title: {text: "Number of Alive Threads"}
            }
        },
        {
            title: "JVM Thread CPU Usage",
            series_provider: double("thread_cpu_usage_gauge.csv", {
                0: util.convert.secondToHour,
                1: util.convert.toPercent,
                2: util.convert.toPercent,
                3: util.convert.toPercent
            }),

            yAxis: {
                title: {text: "% of CPU Time Consumed By Threads"},
                min: 0,
                max: 100
            }
        },
        {
            title: "State Size: Unreachable per Alive Peer",
            series_provider: double("unreachable_state_size_per_alive_peer_gauge.csv"),
            yAxis: {
                title: {text: "Number of Unreachable State Per Alive Peer"}
            }
        },
        {
            title: "Evolutionary Maintenance: Number of Environment Clusters",
            series_provider: double("evolutionary_maintenance_cluster_count_sampler.csv"),
            yAxis: {
                title: {text: "Number of Environment Clusters"}
            }
        },
        {
            title: "Evolutionary Maintenance: Size of Environment Clusters",
            series_provider: double("evolutionary_maintenance_cluster_size_sampler.csv"),
            yAxis: {
                title: {text: "Mean Size of Environment Clusters"}
            }
        },
        {
            title: "Evolutionary Maintenance: Fitness",
            series_provider: double("evolutionary_maintenance_fitness_sampler.csv"),
            yAxis: {
                title: {text: "Fitness"}
            }
        },
        {
            title: "Evolutionary Maintenance: Normalised Fitness",
            series_provider: double("evolutionary_maintenance_normalized_fitness_sampler.csv"),
            yAxis: {
                title: {text: "Normalised Fitness"}
            }
        },
        {
            title: "Evolutionary Maintenance: Weighted Fitness",
            series_provider: double("evolutionary_maintenance_weighted_fitness_sampler.csv"),
            yAxis: {
                title: {text: "Weighted Fitness"}
            }
        },
        {
            title: "Lookup Diagnostics Proportion",
            series_provider: single(
                [data("lookup_correctness_rate.csv", [1]), data("lookup_incorrectness_rate.csv", [1]), data("lookup_failure_rate.csv", [1])]
            ),
            chart: {type: "pie"}
        },
        {
            title: "Performance Vs. Cost: Successful Lookup",
            chart: {type: "scatter"},
            yAxis: {
                title: {text: "Mean Correct Lookup Delay (ms)"}
            },
            xAxis: {
                title: {text: "Bytes Sent Per Peer Per Second"},
                max: null
            },
            tooltip: {
                headerFormat: '<b>{series.name}</b><br>',
                pointFormat: '{point.x:.2f} Bytes/Peer/Second, {point.y:.2f} ms'
            },
            series_provider: verses(data("sent_bytes_per_alive_peer_per_second_gauge.csv", [1]), data("lookup_correctness_delay_timer.csv", [1]))
        },
        {
            title: "Performance Vs. Cost: Unsuccessful Lookup",
            series_provider: verses(data("sent_bytes_per_alive_peer_per_second_gauge.csv", [1]), {
                columnAverage: function (scenario_name) {
                    var inc = data("lookup_incorrectness_rate.csv", [1]).columnSum(scenario_name)[0];
                    var fail = data("lookup_failure_rate.csv", [1]).columnSum(scenario_name)[0];
                    var total = data("lookup_execution_rate.csv", [1]).columnSum(scenario_name)[0];
                    return [util.convert.toPercent((inc + fail) / total)];
                }
            }),
            chart: {type: "scatter"},
            yAxis: {
                title: {text: "% of unsuccessful lookups"},
                max: 100
            },
            xAxis: {
                title: {text: "Bytes Sent Per Peer Per Second" },
                max: null
            },

            tooltip: {
                headerFormat: '<b>{series.name}</b><br>',
                pointFormat: '{point.x:.2f} Bytes/Peer/Second, {point.y:.2f} %'
            }
        }
    ].sortBy("title");


    return {
        observations: observations,
        makeMenu: function () {

            $("#chart_list").html(mark.up(util.read("templates/sidebar.html"), {observations: observations}));
        }
    };
})
