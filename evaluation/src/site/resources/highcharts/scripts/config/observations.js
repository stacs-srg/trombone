define(['util', 'mark'], function (util, mark) {
    var observations = [
        {
            title: "Event Completion Rate",
            file_name: "event_completion_rate.csv",
            options: {
                vAxis: {
                    title: "Number of Events Executed Per Second"
                }
            }
        },
        {
            title: "Network Size",
            file_name: "available_peer_counter.csv",
            chart_type: "LineChart",
            options: {
                title: "Network Size",
                vAxis: {
                    title: "Number of Available Peers "
                }
            }
        },
        {
            title: "Event Execution Duration Timer",
            file_name: "event_execution_duration_timer.csv",
            options: {
                vAxis: {
                    title: "Mean Event Execution Completion Time (ms)"
                }
            }
        },
        {
            title: "Event Execution Lag",
            file_name: "event_execution_lag_sampler.csv",
            options: {
                vAxis: {
                    title: "Event Execution Lag (ms)"
                }
            }
        },
        {
            title: "Event Executor Queue Size",
            file_name: "event_executor_queue_size.csv",
            options: {
                vAxis: {
                    title: "Number of Events Queued by Executor"
                }
            }
        },
        {
            title: "Event Scheduling Rate",
            file_name: "event_scheduling_rate.csv",
            options: {
                vAxis: {
                    title: "Number of Events Scheduled for Execution"
                }
            }

        },
        {
            title: "Join Failure Rate",
            file_name: "join_failure_rate.csv",
            options: {
                vAxis: {
                    title: "Number of Failed Joins"
                }
            }
        },
        {
            title: "Join Success Rate",
            file_name: "join_success_rate.csv",
            options: {
                vAxis: {
                    title: "Number of Successful Joins"
                }
            }
        },
        {
            title: "Lookup Correctness Delay",
            file_name: "lookup_correctness_delay_timer.csv",
            options: {
                vAxis: {
                    title: "Mean Correct Lookup Delay (ms)"
                }
            }
        },
        {
            title: "Lookup Correctness Hop Count",
            file_name: "lookup_correctness_hop_count_sampler.csv",
            options: {
                vAxis: {
                    title: "Mean Correct Lookup Hop Count"
                }
            }
        },
        {
            title: "Lookup Correctness Rate",
            file_name: "lookup_correctness_rate.csv",
            chart_type: "LineChart",
            options: {
                vAxis: {
                    title: "Number of Correct Lookups Per Second"
                }
            }
        },
        {
            title: "Lookup Correctness Retry Count",
            file_name: "lookup_correctness_retry_count_sampler.csv",
            options: {
                vAxis: {
                    title: "Mean Correct Lookup Retry Count"
                }
            }
        },
        {
            title: "Lookup Execution Rate",
            file_name: "lookup_execution_rate.csv",
            options: {
                vAxis: {
                    title: "Number of Lookups Executed Per Second"
                }
            }
        },
        {
            title: "Lookup Failure Delay",
            file_name: "lookup_failure_delay_timer.csv",
            options: {
                vAxis: {
                    title: "Mean Failed Lookup Delay (ms)"
                }
            }
        },
        {
            title: "Lookup Failure Hop Count",
            file_name: "lookup_failure_hop_count_sampler.csv",
            options: {
                vAxis: {
                    title: "Mean Failed Lookup Hop Count"
                }
            }
        },
        {
            title: "Lookup Failure Rate",
            file_name: "lookup_failure_rate.csv",
            options: {
                vAxis: {
                    title: "Number of Failed Lookups per Second"
                }
            }
        },
        {
            title: "Lookup Failure Retry Count",
            file_name: "lookup_failure_retry_count_sampler.csv",
            options: {
                vAxis: {
                    title: "Mean Failed Lookup Retry Count"
                }
            }
        },
        {
            title: "Lookup Incorrectness Delay Timer",
            file_name: "lookup_incorrectness_delay_timer.csv",
            chart_type: "LineChart",
            options: {
                vAxis: {
                    title: "Mean Incorrect Lookup Delay (ms)"
                }
            }
        },
        {
            title: "Lookup Incorrectness hop Count Sampler",
            file_name: "lookup_incorrectness_hop_count_sampler.csv",
            chart_type: "LineChart",
            options: {
                vAxis: {
                    title: "Mean Incorrect Lookup Hop Count"
                }
            }
        },
        {
            title: "Lookup Incorrectness Rate",
            file_name: "lookup_incorrectness_rate.csv",
            chart_type: "LineChart",
            options: {
                vAxis: {
                    title: "Number of Incorrect Lookups Per Second"
                }
            }
        },
        {
            title: "Lookup Incorrectness Retry Count Sampler",
            file_name: "lookup_incorrectness_retry_count_sampler.csv",
            chart_type: "LineChart",
            options: {
                vAxis: {
                    title: "Mean Incorrect Lookup Retry Count"
                }
            }
        },
        {
            title: "JVM Heap + Non-heap Memory Usage",
            file_name: "memory_usage_gauge.csv",
            conversion: {
                column: {
                    2: "byteToGigabyte",
                    3: "byteToGigabyte",
                    4: "byteToGigabyte"
                }
            },
            options: {
                vAxis: {
                    title: "Heap and Non-heap Memory usage (GB)"
                }
            }
        },
        {
            title: "Peer Arrival Rate",
            file_name: "peer_arrival_rate.csv",
            chart_type: "LineChart",
            options: {
                vAxis: {
                    title: "Number of Peers Arrived Per Second"
                }
            }
        },
        {
            title: "Peer Departure Rate",
            file_name: "peer_departure_rate.csv",
            chart_type: "LineChart",
            options: {
                vAxis: {
                    title: "Number of Peers Departed Per Second"
                }
            }
        },
        {
            title: "Generated Events Queue Size",
            file_name: "queue_size_gauge.csv",
            chart_type: "LineChart",
            options: {
                vAxis: {
                    title: "Number of Generated Events"
                }
            }
        },
        {
            title: "State Size: Reachable per Alive Peer",
            file_name: "reachable_state_size_per_alive_peer_gauge.csv",
            chart_type: "LineChart",
            options: {
                vAxis: {
                    title: "Number of Reachable State Per Alive Peer"
                }
            }
        },
        {
            title: "Sent Bytes Rate",
            file_name: "sent_bytes_rate.csv",
            options: {
                vAxis: {
                    title: "Bytes Sent Per Second"
                }
            }
        },
        {
            title: "Sent Bytes Per Alive Peer Per Second",
            file_name: "sent_bytes_per_alive_peer_per_second_gauge.csv",
            options: {
                vAxis: {
                    title: "Bytes Sent Per Alive node Per Second"
                }
            }
        },
        {
            title: "JVM System Load Average Over One Minute",
            file_name: "system_load_average_gauge.csv",
            options: {
                vAxis: {
                    title: "Load Average Over One Minute"
                }
            }
        },
        {
            title: "JVM Alive Thread Count",
            file_name: "thread_count_gauge.csv",
            chart_type: "LineChart",
            options: {
                vAxis: {
                    title: "Number of Alive Threads"
                }
            }
        },
        {
            title: "JVM Thread CPU Usage",
            file_name: "thread_cpu_usage_gauge.csv",
            conversion: {
                column: {
                    2: util.convert.toPercent,
                    3: "toPercent",
                    4: "toPercent"
                }
            },
            options: {
                vAxis: {
                    title: "% of CPU Time Consumed By Threads",
                    useFormatFromData: true,
                    min: 0,
                    max: 100,
                    viewWindow: {
                        min: 0,
                        max: 100
                    },
                    gridlines: {
                        count: 6
                    }
                }
            }
        },
        {
            title: "State Size: Unreachable per Alive Peer",
            file_name: "unreachable_state_size_per_alive_peer_gauge.csv",
            options: {
                vAxis: {
                    title: "Number of Unreachable State Per Alive Peer"
                }
            }
        },
        {
            title: "Lookup Diagnostics Proportion",
            file_name: "unreachable_state_size_per_alive_peer_gauge.csv",
            chart_type: "PieChart"
        },
        {
            title: "Performance Vs. Cost: Lookup Delay",
            file_name: [ "sent_bytes_per_alive_peer_per_second_gauge.csv", "lookup_correctness_delay_timer.csv"],
            chart_type: "ScatterChart",
            options: {
                vAxis: {
                    title: "Mean Correct Lookup Delay (ms)"
                },
                hAxis: {
                    title: "Bytes Sent Per Node Per Second",
                    viewWindow: {
                        max: null
                    }
                }
            }
        },
        {
            title: "Performance Vs. Cost: Lookup Failure",
            file_name: [ "sent_bytes_per_alive_peer_per_second_gauge.csv", "lookup_incorrectness_rate.csv"],
            chart_type: "ScatterChart",
            options: {
                vAxis: {
                    title: "Mean Incorrect Lookup Rate per Second"
                },
                hAxis: {
                    title: "Bytes Sent Per Node Per Second",
                    viewWindow: {
                        max: null
                    }
                }
            }
        }
    ].sortBy("title");


    return {
        observations: observations,
        makeMenu: function () {

            $("#chart_list").html(mark.up(util.read("templates/sidebar.html"), {observations: observations}));


//            observations.forEach(function (observation, index) {
//                var link = $('<a href="javascript:void(0)" class="list-group-item" id="metric_' +
//                    index +
//                    '"></a>');
//                link.append(observation.title);
//                link.click(function () {
//                    query.metric = index;
//                    query.update();
//                    $("#chart_list .active").removeClass("active");
//                    $("#main_title").text(metric.innerText);
//                    $(this).addClass("active");
//                });
//                $('#chart_list').append(link);
//            });
//
//            $("#chart_list a:first").addClass("active");
//            selected_chart = observations[0].file_name;
//            $("#main_title").text(observations[0].title);
        }
    };
})
