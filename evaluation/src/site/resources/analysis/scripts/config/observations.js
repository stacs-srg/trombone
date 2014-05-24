define(['jquery', 'util', 'mark', 'config/theme', 'data', 'series/double', 'series/verses', 'series/single', 'jstat'], function ($, util, mark, theme, data, double, verses, single, jstat) {

    function unsuccessfulLookupPercentage(filter) {

        return {
            columnAverage: function (scenario_name) {
                var inc = data("lookup_incorrectness_rate.csv", [1], undefined, filter).columnSum(scenario_name)[0];
                var fail = data("lookup_failure_rate.csv", [1], undefined, filter).columnSum(scenario_name)[0];
                var total = data("lookup_execution_rate.csv", [1], undefined, filter).columnSum(scenario_name)[0];
                return [util.convert.toPercent((inc + fail) / total)];
            },
            repetitions: function (scenario_name, average) {
                var inc = data("lookup_incorrectness_rate.csv", [1], undefined, filter).repetitionMeans(scenario_name);
                var fail = data("lookup_failure_rate.csv", [1], undefined, filter).repetitionMeans(scenario_name);
                var total = data("lookup_execution_rate.csv", [1], undefined, filter).repetitionMeans(scenario_name);

                var promise = $.Deferred();

                $.when(inc, fail, total).done(function (a, b, c) {

                    var percentages = []

                    for (var i = 0; i < a.length; i++) {
                        percentages.push(util.convert.toPercent((a[i] + b[i]) / c[i]))
                    }

                    var confidence_interval = jStat.tci(jStat(percentages).mean(), 0.05, percentages);
                    if (isNaN(confidence_interval[0])) {
                        confidence_interval[0] = null
                    }

                    if (isNaN(confidence_interval[1])) {
                        confidence_interval[1] = null
                    }
                    promise.resolve(confidence_interval);
                })


                return promise.promise()
            }
        }
    }


    function filterDuringTraining(data) {

        if (data.length > 1455) {
            data.splice(data.length - 1440, data.length);
        }

        return data;
    }

    function filterAfterTraining(data) {
        if (data.length > 1455) {
            data.splice(0, data.length - 1440);
        }

        return data;
    }

    function crossRepetitionWithIntervals(context, matches, data_obj, detail_property, detail_func) {
        var series = [];
        var series_promisses = [];
        var grouped_by_maintenance = matches.groupBy('maintenance');

        Object.keys(grouped_by_maintenance).forEach(function (group_name, index) {

            var simple_group_name = util.getMaintenanceSimpleName(group_name);
            var series_data = [];
            var series_intervals = [];
            var series_intervals_promises = [];

            grouped_by_maintenance[group_name].forEach(function (match, index) {
                var y = data_obj.columnAverage(match.name)[0];
                var churn_detail = detail_func(match[detail_property]);

                series_data.push({y: y, name: churn_detail.shortName, order: churn_detail.order, match: match})

                var deffered = $.Deferred();
                data_obj.repetitions(match.name, y, series_intervals).done(function (interval) {
                    series_intervals.push({low: interval[0], high: interval[1], order: churn_detail.order})
                    deffered.resolve();
                });

                series_intervals_promises.push(deffered);
            });

            var deffered = $.Deferred();
            $.when.apply($, series_intervals_promises).done(function () {

                series_data.sortBy('order');
                series_intervals.sortBy('order');
                series.push(
                    {
                        id: group_name,
                        name: simple_group_name.shortName,
                        data: series_data,
                        type: 'scatter',
                        color: theme.colours[index]
                    },
                    {
                        name: group_name + " Confidence Interval",
                        type: 'errorbar',
                        data: series_intervals,
                        zIndex: 0,
                        linkedTo: group_name,
                        color: theme.colours[index]
                    }
                );
                deffered.resolve();
            });
            series_promisses.push(deffered);

        })

        var deffered = $.Deferred();
        $.when.apply($, series_promisses).done(function () {
            deffered.resolve(series);
        });

        return deffered;
    }

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
                title: {text: "Bytes Sent Per Alive Peer Per Second"}
            }
        },
        {
            title: "RPC Error Rate Per Second",
            series_provider: double("rpc_error_rate.csv"),
            yAxis: {
                title: {text: "RPC Error Rate Per Second"}
            }
        },
        {
            title: "Reconfiguration Rate Per Second",
            series_provider: double("reconfiguration_rate.csv"),
            yAxis: {
                title: {text: "Reconfiguration Rate Per Second"}
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
            title: "JVM GC CPU Usage",
            series_provider: double("gc_cpu_usage_gauge.csv", {
                0: util.convert.secondToHour,
                1: util.convert.toPercent,
                2: util.convert.toPercent,
                3: util.convert.toPercent
            }),

            yAxis: {
                title: {text: "% of CPU Time Consumed By GC"},
                min: 0
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
            title: "Evolutionary Maintenance: Strategy Action Size",
            series_provider: double("strategy_action_size_sampler.csv"),
            yAxis: {
                title: {text: "Mean Strategy Action Size"}
            }
        },
        {
            title: "Evolutionary Maintenance: Strategy Uniformity",
            series_provider: double("strategy_uniformity_sampler.csv"),
            yAxis: {
                title: {text: "Mean Number of identical strategies across peers"}
            }
        },
        {
            title: "Evolutionary Maintenance: Generated Strategy Uniformity",
            series_provider: double("generated_strategy_uniformity_sampler.csv"),
            yAxis: {
                title: {text: "Mean Number of identical generated strategies across peers"}
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
            title: "PVC: Mean Successful Lookup Delay Vs. Bandwidth Usage",
            chart: {type: "scatter"},
            yAxis: {
                title: {text: "Mean Correct Lookup Delay (ms)"}
            },
            xAxis: {
                title: {text: "Bytes Sent Per Peer Per Second"},
                max: null
            },
            tooltip: {
                headerFormat: '<b>{point.key}</b><br>',
                pointFormat: '{point.x:.2f} Bytes/Peer/Second, {point.y:.2f} ms'
            },
            series_provider: verses(data("sent_bytes_per_alive_peer_per_second_gauge.csv", [1]), data("lookup_correctness_delay_timer.csv", [1]))
        },
        {
            title: "PVC: Percentage of Unsuccessful Lookups Vs. Bandwidth Usage ",
            series_provider: verses(data("sent_bytes_per_alive_peer_per_second_gauge.csv", [1]), unsuccessfulLookupPercentage()),
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
                headerFormat: '<b>{point.key}</b><br>',
                pointFormat: '{point.x:.2f} Bytes/Peer/Second, {point.y:.2f} %'
            }
        },
        {
            title: "PVC: Normalized Distance Vs. Bandwidth Usage",
            chart: {type: "scatter"},
            yAxis: {
                title: {text: "Euclidean Distance From Origin"}
            },
            xAxis: {
                title: {text: "Bytes Sent Per Peer Per Second"},
                max: null
            },
            tooltip: {
                headerFormat: '<b>{point.key}</b><br>',
                pointFormat: '{point.x:.2f} Bytes/Peer/Second, Distance: {point.y:.2f}'
            },
            series_provider: verses(data("sent_bytes_per_alive_peer_per_second_gauge.csv", [1]), {

                importance: 1,
                columnAverage: function (scenario_name) {


                    var correct_delay = data("lookup_correctness_delay_timer.csv", [1]).columnNormalizedAverage(scenario_name, 0) * this.importance;
                    var inc = data("lookup_incorrectness_rate.csv", [1]).columnSum(scenario_name)[0];
                    var fail = data("lookup_failure_rate.csv", [1]).columnSum(scenario_name)[0];
                    var total = data("lookup_execution_rate.csv", [1]).columnSum(scenario_name)[0];
                    var ratio_failed = (inc + fail) / total * (2 - this.importance);
                    var point = [correct_delay, ratio_failed];
                    var euclideanDistance = util.euclideanDistance(point, [0, 0]);

                    return  [euclideanDistance];
                }
            }, undefined, "churn")
        },
        {
            title: "By Churn: Bandwidth Usage",
            yAxis: {
                title: {text: "Bytes Sent Per Peer Per Second"}
            },
            xAxis: {
                title: {text: "Churn Model"},
                type: 'category'
            },
            tooltip: {
                formatter: function () {
                    return this.point.match.name;
                }
            },
            exporting: {
                filename: 'by_churn_bw'
            },
            series_provider: {
                get: function (matches) {
                    var bytes_sent = data("sent_bytes_per_alive_peer_per_second_gauge.csv", [1, 2, 3]);
                    return crossRepetitionWithIntervals(this, matches, bytes_sent, 'churn', util.getChurnDetail);
                }
            }
        },
        {
            title: "By Churn: Unsuccessful Lookup",
            chart: {
                type: "scatter"
            },
            yAxis: {
                title: {text: "% of unsuccessful Lookups"},
                max: 100
            },
            xAxis: {
                title: {text: "Churn Model"},
                type: 'category'
            },
            tooltip: {
                formatter: function () {
                    return this.point.match.name;
                }
            },
            exporting: {
                filename: 'by_churn_lookup_failure'
            },
            series_provider: {
                get: function (matches) {

                    var bytes_sent = unsuccessfulLookupPercentage();
                    return crossRepetitionWithIntervals(this, matches, bytes_sent, 'churn', util.getChurnDetail);
                }
            }
        },
        {
            title: "By Churn: Successful Lookup Delay",
            chart: {
                type: "scatter"
            },
            yAxis: {
                title: {text: "Successful Lookup Delay (ms)"}
            },
            xAxis: {
                title: {text: "Churn Model"},
                type: 'category'
            },
            tooltip: {
                formatter: function () {
                    return this.point.match.name;
                }
            },
            exporting: {
                filename: 'by_churn_successful_lookup'
            },
            series_provider: {
                get: function (matches) {

                    var data_obj = data("lookup_correctness_delay_timer.csv", [1]);
                    return crossRepetitionWithIntervals(this, matches, data_obj, 'churn', util.getChurnDetail);
                }
            }
        },
        {
            title: "By Workload: Bandwidth Usage",
            chart: {type: "scatter"},
            yAxis: {
                title: {text: "Bytes Sent Per Peer Per Second"}
            },
            xAxis: {
                title: {text: "Workload Model"},
                type: 'category'
            },
            tooltip: {
                formatter: function () {
                    return this.point.match.name;
                }
            },
            exporting: {
                filename: 'by_workload_bw'
            },
            series_provider: {
                get: function (matches) {

                    var data_obj = data("sent_bytes_per_alive_peer_per_second_gauge.csv", [1]);
                    return crossRepetitionWithIntervals(this, matches, data_obj, 'workload', util.getWorkloadDetail);
//                    var series = [];
//                    var bytes_sent = data("sent_bytes_per_alive_peer_per_second_gauge.csv", [1]);
//                    var grouped_by_maintenance = matches.groupBy('maintenance');
//
//                    Object.keys(grouped_by_maintenance).forEach(function (group_name, index) {
//                        var simple_group_name = util.getMaintenanceSimpleName(group_name);
//                        var series_data = [];
//                        grouped_by_maintenance[group_name].forEach(function (match, index) {
//                            var y = bytes_sent.columnAverage(match.name)[0];
//                            var workload_detail = util.getWorkloadDetail(match.workload);
//                            series_data.push({y: y, name: workload_detail.shortName, order: workload_detail.order, match: match})
//
//                        }, this);
//                        series_data.sortBy('order');
//                        series.push(
//                            {
//                                id: group_name,
//                                name: simple_group_name.shortName,
//                                data: series_data,
//                                color: theme.colours[index]
//                            }
//                        );
//                    }, this)
//                    return series;
                }
            }
        },
        {
            title: "By Workload: Unsuccessful Lookup",
            chart: {
                type: "scatter"
            },
            yAxis: {
                title: {text: "% of unsuccessful Lookups"},
                max: 100
            },
            xAxis: {
                title: {text: "Workload Model"},
                type: 'category'
            },
            tooltip: {
                formatter: function () {
                    return this.point.match.name;
                }
            },
            exporting: {
                filename: 'by_workload_lookup_failure'
            },
            series_provider: {
                get: function (matches) {
//                    var series = [];
//                    var grouped_by_maintenance = matches.groupBy('maintenance');
//
//
//                    Object.keys(grouped_by_maintenance).forEach(function (group_name, index) {
//                        var simple_group_name = util.getMaintenanceSimpleName(group_name);
//                        var series_data = [];
//                        grouped_by_maintenance[group_name].forEach(function (match, index) {
//                            var y = unsuccessfulLookupPercentage(match.name)[0];
//                            var workload_detail = util.getWorkloadDetail(match.workload);
//                            series_data.push({y: y, name: workload_detail.shortName, order: workload_detail.order, match: match})
//
//                        }, this);
//                        series_data.sortBy('order');
//                        series.push(
//                            {
//                                id: group_name,
//                                name: simple_group_name.shortName,
//                                data: series_data,
//                                color: theme.colours[index]
//                            }
//                        );
//                    }, this)
//                    return series;

                    var data_obj = unsuccessfulLookupPercentage();
                    return crossRepetitionWithIntervals(this, matches, data_obj, 'workload', util.getWorkloadDetail);
                }
            }
        },
        {
            title: "By Workload: Successful Lookup Delay",
            chart: {
                type: "scatter"
            },
            yAxis: {
                title: {text: "Successful Lookup Delay (ms)"}
            },
            xAxis: {
                title: {text: "Workload Model"},
                type: 'category'
            },
            tooltip: {
                formatter: function () {
                    return this.point.match.name;
                }
            },
            exporting: {
                filename: 'by_workload_successful_lookup'
            },
            series_provider: {
                get: function (matches) {
//                    var series = [];
//                    var delays = data("lookup_correctness_delay_timer.csv", [1]);
//                    var grouped_by_maintenance = matches.groupBy('maintenance');
//
//
//                    Object.keys(grouped_by_maintenance).forEach(function (group_name, index) {
//                        var simple_group_name = util.getMaintenanceSimpleName(group_name);
//                        var series_data = [];
//                        grouped_by_maintenance[group_name].forEach(function (match, index) {
//                            var y = delays.columnAverage(match.name)[0];
//                            var workload_detail = util.getWorkloadDetail(match.workload);
//                            series_data.push({y: y, name: workload_detail.shortName, order: workload_detail.order, match: match})
//
//                        }, this);
//                        series_data.sortBy('order');
//                        series.push(
//                            {
//                                id: group_name,
//                                name: simple_group_name.shortName,
//                                data: series_data,
//                                color: theme.colours[index]
//                            }
//                        );
//                    }, this)
//                    return series;
                    var data_obj = data("lookup_correctness_delay_timer.csv", [1]);
                    return crossRepetitionWithIntervals(this, matches, data_obj, 'workload', util.getWorkloadDetail);
                }
            }
        },
        {
            title: "By During Training: Bandwidth Consumption",
            chart: {
                type: "scatter"
            },
            yAxis: {
                title: {text: "Bytes Sent Per Peer Per Second"}
            },
            xAxis: {
                title: {text: "Training Duration (hrs)"},
                type: 'category'
            },
            tooltip: {
                formatter: function () {
                    return this.point.match.name;
                }
            },
            exporting: {
                filename: 'by_training_during_bw'
            },
            series_provider: {
                get: function (matches) {

                    var data_obj = data("sent_bytes_per_alive_peer_per_second_gauge.csv", [1], undefined, filterDuringTraining);
                    return crossRepetitionWithIntervals(this, matches, data_obj, 'experiment_duration', util.getTrainingTime);
                }
            }
        },
        {
            title: "By During Training: Unsuccessful Lookup",
            chart: {
                type: "scatter"
            },
            yAxis: {
                title: {text: "% of unsuccessful Lookups"},
                max: 100
            },
            xAxis: {
                title: {text: "Training Duration (hrs)"},
                type: 'category'
            },
            tooltip: {
                formatter: function () {
                    return this.point.match.name;
                }
            },
            exporting: {
                filename: 'by_training_during_lookup_failure'
            },
            series_provider: {
                get: function (matches) {

                    var data_obj = unsuccessfulLookupPercentage(filterDuringTraining);
                    return crossRepetitionWithIntervals(this, matches, data_obj, 'experiment_duration', util.getTrainingTime);
                }
            }
        },
        {
            title: "By During Training: Successful Lookup Delay",
            chart: {
                type: "scatter"
            },
            yAxis: {
                title: {text: "Successful Lookup Delay (ms)"}
            },
            xAxis: {
                title: {text: "Training Duration (hrs)"},
                type: 'category'
            },
            tooltip: {
                formatter: function () {
                    return this.point.match.name;
                }
            },
            exporting: {
                filename: 'by_training_during_successful_lookup'
            },
            series_provider: {
                get: function (matches) {

                    var data_obj = data("lookup_correctness_delay_timer.csv", [1], undefined, filterDuringTraining);
                    return crossRepetitionWithIntervals(this, matches, data_obj, 'experiment_duration', util.getTrainingTime);
                }
            }
        },
        {
            title: "By After Training: Bandwidth Consumption",
            chart: {
                type: "scatter"
            },
            yAxis: {
                title: {text: "Bytes Sent Per Peer Per Second"}
            },
            xAxis: {
                title: {text: "Training Duration (hrs)"},
                type: 'category'
            },
            tooltip: {
                formatter: function () {
                    return this.point.match.name;
                }
            },
            exporting: {
                filename: 'by_training_after_bw'
            },
            series_provider: {
                get: function (matches) {

                    var data_obj = data("sent_bytes_per_alive_peer_per_second_gauge.csv", [1], undefined, filterAfterTraining);
                    return crossRepetitionWithIntervals(this, matches, data_obj, 'experiment_duration', util.getTrainingTime);
                }
            }
        },
        {
            title: "By After Training: Unsuccessful Lookup",
            chart: {
                type: "scatter"
            },
            yAxis: {
                title: {text: "% of unsuccessful Lookups"},
                max: 100
            },
            xAxis: {
                title: {text: "Training Duration (hrs)"},
                type: 'category'
            },
            tooltip: {
                formatter: function () {
                    return this.point.match.name;
                }
            },
            exporting: {
                filename: 'by_training_after_lookup_failure'
            },
            series_provider: {
                get: function (matches) {

                    var data_obj = unsuccessfulLookupPercentage(filterAfterTraining);
                    return crossRepetitionWithIntervals(this, matches, data_obj, 'experiment_duration', util.getTrainingTime);
                }
            }
        },
        {
            title: "By After Training: Successful Lookup Delay",
            chart: {
                type: "scatter"
            },
            yAxis: {
                title: {text: "Successful Lookup Delay (ms)"}
            },
            xAxis: {
                title: {text: "Training Duration (hrs)"},
                type: 'category'
            },
            tooltip: {
                formatter: function () {
                    return this.point.match.name;
                }
            },
            exporting: {
                filename: 'by_training_after_successful_lookup'
            },
            series_provider: {
                get: function (matches) {

                    var data_obj = data("lookup_correctness_delay_timer.csv", [1], undefined, filterAfterTraining);
                    return crossRepetitionWithIntervals(this, matches, data_obj, 'experiment_duration', util.getTrainingTime);
                }
            }
        },
        {
            title: "By Clustering Algorithm: Successful Lookup Delay",
            chart: {
                type: "scatter"
            },
            yAxis: {
                title: {text: "Successful Lookup Delay (ms)"}
            },
            xAxis: {
                title: {text: "Clustering Algorithm"},
                type: 'category'
            },
            tooltip: {
                formatter: function () {
                    return this.point.match.name;
                }
            },
            series_provider: {
                get: function (matches) {
                    var series = [];
                    var delays = data("lookup_correctness_delay_timer.csv", [1]);
                    var grouped_by_maintenance = matches.groupBy('churn');

                    //TODO: in extras give option to group by churn or maintenance

                    console.log(grouped_by_maintenance);

                    Object.keys(grouped_by_maintenance).forEach(function (group_name, index) {
                        var simple_group_name = util.getMaintenanceSimpleName(group_name);
                        var series_data = [];
                        grouped_by_maintenance[group_name].forEach(function (match, index) {
                            var y = delays.columnAverage(match.name)[0];
                            var maintenance_detail = util.getClusteringAlgorithmDetail(match.maintenance);
                            series_data.push({y: y, name: maintenance_detail.shortName, order: maintenance_detail.order, match: match})

                        }, this);
                        series_data.sortBy('order');
                        series.push(
                            {
                                id: group_name,
                                name: simple_group_name.shortName,
                                data: series_data,
                                color: theme.colours[index]
                            }
                        );
                    }, this)
                    return series;
                }
            }
        },
        {
            title: "By Strategy: Bandwidth Usage",
            chart: {
                type: "column"
            },
            yAxis: {
                title: {text: "Bytes sent Per Peer Per Second"}
            },
            xAxis: {
                title: {text: "Strategy"},
                type: 'category'
            },
            exporting: {
                filename: 'by_strategy_bw'
            },
            legend:{
                enabled: false
            },
            series_provider: {
                get: function (matches) {

                    var series = [];
                    var series_data = [];
                    var series_intervals = [];
                    var data_obj = data("sent_bytes_per_alive_peer_per_second_gauge.csv", [1]);

                    var grouped_by_maintenance = matches.groupBy('maintenance');
                    Object.keys(grouped_by_maintenance).forEach(function (group_name, index) {
                        var simple_group_name = util.getMaintenanceSimpleName(group_name);
                        var yz = [];

                        grouped_by_maintenance[group_name].forEach(function (match, index) {
                            yz.push(data_obj.columnAverage(match.name)[0]);
                        });

                        var mean = jStat(yz).mean();
                        var interval = jStat.tci(mean, 0.05, yz);


                        series_data.push({y: mean, name: simple_group_name.shortName, order: simple_group_name.order, color: theme.colours[index]})
                        series_intervals.push({low: interval[0], high: interval[1], order: simple_group_name.order, color: 'black'})

                    });


                    series.push(
                        {
                            id: "group_name",
                            name: "simple_group_name.shortName",
                            data: series_data,
                            type: 'column'

                        },
                        {
                            name: " Confidence Interval",
                            type: 'errorbar',
                            data: series_intervals,
                            zIndex: 0,
                            linkedTo: "group_name"
                        }
                    );

                    return series;
                }
            }
        },
        {
            title: "By Strategy: Percentage of Unsuccessful Lookups",
            chart: {
                type: "column"
            },
            yAxis: {
                title: {text: "% of Unsuccessful Lookups"},
                max: 100
            },
            xAxis: {
                title: {text: "Strategy"},
                type: 'category'
            },
            exporting: {
                filename: 'by_strategy_lookup_failure'
            },
            legend:{
              enabled: false  
            },
            series_provider: {
                get: function (matches) {

                    var series = [];
                    var series_data = [];
                    var series_intervals = [];
                    var data_obj = unsuccessfulLookupPercentage();

                    var grouped_by_maintenance = matches.groupBy('maintenance');
                    Object.keys(grouped_by_maintenance).forEach(function (group_name, index) {
                        var simple_group_name = util.getMaintenanceSimpleName(group_name);
                        var yz = [];

                        grouped_by_maintenance[group_name].forEach(function (match, index) {
                            yz.push(data_obj.columnAverage(match.name)[0]);
                        });

                        var mean = jStat(yz).mean();
                        var interval = jStat.tci(mean, 0.05, yz);

                        series_data.push({y: mean, name: simple_group_name.shortName, order: simple_group_name.order, color: theme.colours[index]})
                        series_intervals.push({low: interval[0], high: interval[1], order: simple_group_name.order, color: 'black'})

                    });


                    series.push(
                        {
                            id: "group_name",
                            name: "simple_group_name.shortName",
                            data: series_data,
                            type: 'column'

                        },
                        {
                            name: " Confidence Interval",
                            type: 'errorbar',
                            data: series_intervals,
                            zIndex: 0,
                            linkedTo: "group_name"
                        }
                    );

                    return series;
                }
            }
        },
        {
            title: "By Strategy: Successful Lookup Delay",
            chart: {
                type: "column"
            },
            yAxis: {
                title: {text: "Mean Successful Lookup Delay (ms)"}
            },
            xAxis: {
                title: {text: "Strategy"},
                type: 'category'
            },
            exporting: {
                filename: 'by_strategy_successful_lookup'
            },
            legend:{
                enabled: false
            },
            series_provider: {
                get: function (matches) {

                    var series = [];
                    var series_data = [];
                    var series_intervals = [];
                    var data_obj = data("lookup_correctness_delay_timer.csv", [1]);

                    var grouped_by_maintenance = matches.groupBy('maintenance');
                    Object.keys(grouped_by_maintenance).forEach(function (group_name, index) {
                        var simple_group_name = util.getMaintenanceSimpleName(group_name);
                        var yz = [];

                        grouped_by_maintenance[group_name].forEach(function (match, index) {
                            yz.push(data_obj.columnAverage(match.name)[0]);
                        });

                        var mean = jStat(yz).mean();
                        var interval = jStat.tci(mean, 0.05, yz);


                        series_data.push({y: mean, name: simple_group_name.shortName, order: simple_group_name.order, color: theme.colours[index]})
                        series_intervals.push({low: interval[0], high: interval[1], order: simple_group_name.order, color: 'black'})

                    });


                    series.push(
                        {
                            id: "group_name",
                            name: "simple_group_name.shortName",
                            data: series_data,
                            type: 'column'

                        },
                        {
                            name: " Confidence Interval",
                            type: 'errorbar',
                            data: series_intervals,
                            zIndex: 0,
                            linkedTo: "group_name"
                        }
                    );

                    return series;
                }
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
