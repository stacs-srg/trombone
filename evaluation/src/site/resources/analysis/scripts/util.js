define(
    ['jquery', 'json', 'csv', 'jszip', 'jszip_utils'],
    function () {

        var alphanum = function (a, b) {
            function chunkify(t) {
                t = '' + t;
                var tz = new Array();
                var x = 0, y = -1, n = 0, i, j;

                while (i = (j = t.charAt(x++)).charCodeAt(0)) {
                    var m = (i == 46 || (i >= 48 && i <= 57));
                    if (m !== n) {
                        tz[++y] = "";
                        n = m;
                    }
                    tz[y] += j;
                }
                return tz;
            }

            var aa = chunkify(a);
            var bb = chunkify(b);

            for (x = 0; aa[x] && bb[x]; x++) {
                if (aa[x] !== bb[x]) {
                    var c = Number(aa[x]), d = Number(bb[x]);
                    if (c == aa[x] && d == bb[x]) {
                        return c - d;
                    } else return (aa[x] > bb[x]) ? 1 : -1;
                }
            }
            return aa.length - bb.length;
        }

        Array.prototype.groupBy = function (property_name, alias_function) {
            var groups = {};
            this.forEach(function (element) {
                var key = alias_function == undefined ? element[property_name] : alias_function(element[property_name]);

                if (groups[key] === undefined) {
                    groups[key] = new Array();
                }
                groups[key].push(element);
            });
            return groups;
        };
        Array.prototype.sortBy = function (property_name) {
            this.sort(function (one, other) {
                return alphanum(one[property_name], other[property_name]);
            })
            return this;
        };
        Array.prototype.addIfAbsent = function (element) {
            if (this.indexOf(element) == -1) {
                this.push(element);
                return true;
            }
            return false;
        }
        Array.prototype.removeIfExists = function (element) {
            var index = this.indexOf(element);
            if (index > -1) {
                this.splice(index, 1);
                return true;
            }
            return false;
        }
        String.prototype.hashCode = function () {
            var hash = 0;
            if (this.length == 0) return hash;
            for (i = 0; i < this.length; i++) {
                char = this.charCodeAt(i);
                hash = ((hash << 5) - hash) + char;
                hash = hash & hash;
            }
            return hash;
        }
        String.prototype.startsWith = function (str) {
            return this.indexOf(str) == 0;
        };
        String.prototype.contains = function (str) {
            var number = this.indexOf(str);
            console.log(str + ' ' + number)

            return number != -1;
        };

        return{

            color: {
                increaseBrightness: function (hex, percent) {
                    hex = hex.replace(/^\s*#|\s*$/g, '');
                    if (hex.length == 3) {
                        hex = hex.replace(/(.)/g, '$1$1');
                    }

                    var r = parseInt(hex.substr(0, 2), 16),
                        g = parseInt(hex.substr(2, 2), 16),
                        b = parseInt(hex.substr(4, 2), 16);

                    return '#' +
                        ((0 | (1 << 8) + r + (256 - r) * percent / 100).toString(16)).substr(1) +
                        ((0 | (1 << 8) + g + (256 - g) * percent / 100).toString(16)).substr(1) +
                        ((0 | (1 << 8) + b + (256 - b) * percent / 100).toString(16)).substr(1);
                }
            },

            getChurnDetail: function (churn) {

                var churn_detail = {
                    shortName: churn,
                    order: 10000
                }

                if (churn == 'session length: Infinite, down time: None') {
                    churn_detail.shortName = 'None';
                    churn_detail.order = 0;
                }
                else if (churn == 'session length: Exponential(mean: 30 mins), down time: Exponential(mean: 30 mins)') {
                    churn_detail.shortName = 'B';
                    churn_detail.order = 2;
                }
                if (churn == 'session length: Exponential(mean: 10 mins), down time: Exponential(mean: 30 mins)') {
                    churn_detail.shortName = 'C';
                    churn_detail.order = 3;
                }
                if (churn == 'session length: Exponential(mean: 30 mins), down time: Exponential(mean: 10 mins)') {
                    churn_detail.shortName = 'A';
                    churn_detail.order = 1;
                }
                if (churn == 'session length: Oscillating Exponential(min: 1 mins, max: 10 mins, cycle: 2 hrs), down time: Oscillating Exponential(min: 1 mins, max: 10 mins, cycle: 2 hrs)') {
                    churn_detail.shortName = 'Oscillating';
                    churn_detail.order = 4;
                }
                if (churn == 'session length: Oscillating Exponential(min: 1 mins, max: 10 mins, cycle: 2 hrs), down time: Oscillating Exponential(min: 10 mins, max: 1 mins, cycle: 2 hrs)') {
                    churn_detail.shortName = 'Oscillating Inverse';
                    churn_detail.order = 5;
                }

                return churn_detail;
            },
            getWorkloadDetail: function (workload) {
                var detail = {
                    shortName: workload,
                    order: 10000
                }

                if (workload == 'None') {
                    detail.shortName = 'None';
                    detail.order = 1;
                }
                else if (workload == 'Exponential(mean: 1 s)') {
                    detail.shortName = 'Heavy';
                    detail.order = 3;
                }
                if (workload == 'Exponential(mean: 10 s)') {
                    detail.shortName = 'Light';
                    detail.order = 2;
                }
                if (workload == 'Oscillating Exponential(min: 1 s, max: 10 s, cycle: 30 mins)') {
                    detail.shortName = 'Oscillating';
                    detail.order = 4;
                }

                return detail;
            },
            getPopulationSizeDetail: function (maintenance) {

                var detail = {
                    shortName: maintenance + '?',
                    order: 10000
                }

                if (maintenance.contains('population size: 50')) {
                    detail.shortName = '50';
                    detail.order = 5;
                }
                else if (maintenance.contains('population size: 40')) {
                    detail.shortName = '40';
                    detail.order = 4;
                }
                else if (maintenance.contains('population size: 30')) {
                    detail.shortName = '30';
                    detail.order = 3;
                }
                else if (maintenance.contains('population size: 20')) {
                    detail.shortName = '20';
                    detail.order = 2;
                }
                else if (maintenance.contains('population size: 10')) {
                    detail.shortName = '10';
                    detail.order = 1;
                }

                return detail;
            },
            getClusteringAlgorithmDetail: function (maintenance) {

                var detail = {
                    shortName: maintenance + '?',
                    order: 10000
                }

                if (maintenance.contains('PFClustClusterer')) {
                    detail.shortName = 'PFClust';
                    detail.order = 3;
                }
                else if (maintenance.contains('KMeansPlusPlusClusterer')) {
                    detail.shortName = 'K-Means++';
                    detail.order = 2;
                }
                else if (maintenance.contains('PerPointClusterer')) {
                    detail.shortName = 'None';
                    detail.order = 1;
                }

                return detail;
            },
            getMaintenanceSimpleName: function (maintenance) {
                maintenance = maintenance.trim();

                var names = {
                    'None': {shortName: 'None', order: 1},
                    'SuccessorListMaintenance': {shortName: 'Successor List', order: 3},
                    'SuccessorMaintenance': {shortName: 'Successor', order: 2},
                    'RandomSelectorMaintenance': {shortName: 'Random Selector', order: 4},
                    'MostRecentlySeenMaintenance': {shortName: 'Most Recently Seen', order: 5},
                    'Evolutionary(population size: 10, elite count: 2, mutation prob.: 0.1, trial length: 2 mins, clusterer: PFClustClusterer)': {shortName: 'Adaptive GA', order: 6},
                    'Evolutionary(population size: 10, elite count: 2, mutation prob.: 0.1, trial length: 2 mins, clusterer: PerPointClusterer)': {shortName: 'GA without clustering', order: 7},
                    'Evolutionary(population size: 10, elite count: 2, mutation prob.: 0.1, trial length: 2 mins, clusterer: KMeansPlusPlusClusterer)': {shortName: 'GA K-means', order: 8},
                    'RandomSearch(population size: 10, trial length: 2 mins, clusterer: PFClustClusterer)': {shortName: 'Adaptive Random', order: 9},
                    'Evolutionary(population size: 20, elite count: 2, mutation prob.: 0.1, trial length: 2 mins, clusterer: PFClustClusterer)': {shortName: 'GA 20', order: 10},
                    'Evolutionary(population size: 30, elite count: 2, mutation prob.: 0.1, trial length: 2 mins, clusterer: PFClustClusterer)': {shortName: 'GA 30', order: 11},
                    'Evolutionary(population size: 40, elite count: 2, mutation prob.: 0.1, trial length: 2 mins, clusterer: PFClustClusterer)': {shortName: 'GA 40', order: 12},
                    'Evolutionary(population size: 50, elite count: 2, mutation prob.: 0.1, trial length: 2 mins, clusterer: PFClustClusterer)': {shortName: 'GA 50', order: 13}
                }
                var detail = {
                    shortName: maintenance + '?',
                    order: 10000
                }

                return names[maintenance] != undefined ? names[maintenance] : detail;
            },
            getTrainingTime: function (experiment_duration) {
                experiment_duration = experiment_duration.trim();

                var names = {
                    '4 hrs': {shortName: '0', order: 1},
                    '6 hrs': {shortName: '2', order: 2},
                    '8 hrs': {shortName: '4', order: 3},
                    '10 hrs': {shortName: '6', order: 4},
                    '12 hrs': {shortName: '8', order: 5},
                    '14 hrs': {shortName: '10', order: 6}
                }
                var detail = {
                    shortName: experiment_duration + '?',
                    order: 10000
                }

                return names[experiment_duration] != undefined ? names[experiment_duration] : detail;
            },

            spinner: function (id) {
                return {
                    start: function () {
                        $('#' + id).css('display', 'block');
                    },
                    stop: function () {
                        $('#' + id).css('display', 'none');
                    }
                };
            },
            convert: {
                secondToHour: function (seconds) {
                    return seconds / 3600; //60^2
                },
                byteToGigabyte: function (bytes) {
                    return bytes / 1073741824; //1024^3
                },
                toPercent: function (number) {
                    return number * 100;
                },
                timeUnitToShortTimeUnit: function (time_unit) {
                    switch (time_unit) {
                        case "NANOSECONDS":
                            return "ns";
                        case "MICROSECONDS":
                            return "micros";
                        case "MILLISECONDS":
                            return "ms";
                        case "SECONDS":
                            return "s";
                        case "MINUTES":
                            return "mins";
                        case "HOURS":
                            return "hrs";
                        case "DAYS":
                            return "days";
                        default:
                            return time_unit;
                    }
                },
                durationToString: function (duration) {

                    var time_unit = duration.timeUnit.toUpperCase();
                    var length = duration.length;

                    if (length == 9223372036854775807) {
                        return "Infinite"
                    }
                    if (length == 0) {
                        return "None"
                    }

                    return length + " " + this.timeUnitToShortTimeUnit(time_unit);
                },
                intervalsToString: function (intervals) {

                    if (intervals.name == "OscillatingExponentialInterval") {
                        return "Oscillating Exponential(min: " + this.durationToString(intervals.minMean) + ", max: " + this.durationToString(intervals.maxMean) + ", cycle: " + this.durationToString(intervals.cycleLength) + ")"
                    }

                    if (intervals.name == "FixedExponentialInterval") {
                        return "Exponential(mean: " + this.durationToString(intervals.mean) + ")"
                    }
                    if (intervals.name == "ConstantIntervalGenerator") {
                        return this.durationToString(intervals.constantInterval);
                    }

                    return JSON.stringify(intervals);
                },
                workloadToString: function (workload) {
                    var intervals = this.intervalsToString(workload.intervals);
                    return  intervals == "Infinite" ? "None" : intervals;
                },
                churnToString: function (churn) {
                    return "session length: " + this.intervalsToString(churn.sessionLength) +
                        ", down time: " + this.intervalsToString(churn.downtime);
                },
                maintenanceToString: function (maintenance, scenario) {

                    if (maintenance.name == "Maintenance") {

                        var strategy = maintenance.strategy;
                        if (strategy == undefined) {
                            return "None";
                        }
                        return strategy.name
                    }

                    if (maintenance.name == "EvolutionaryMaintenance") {

                        return "Evolutionary(population size: " +
                            maintenance.populationSize +
                            ", elite count: " +
                            maintenance.eliteCount +
                            ", mutation prob.: " +
                            maintenance.mutationProbability +
                            ", trial length: " +
                            this.durationToString({timeUnit: maintenance.evolutionCycleLengthUnit, length: maintenance.evolutionCycleLength}) +
                            ", clusterer: " +
                            ( maintenance.clustererName === undefined ? "None[small-space]" : maintenance.clustererName == 'PFClustClusterer' && scenario.name.match(/scenario_batch(4|5|6|7|8|9)_.*/) != null ? maintenance.clustererName + 'Optimised' : maintenance.clustererName)
                            + ")";
                    }
                    if (maintenance.name == "RandomMaintenance") {

                        return "RandomSearch(population size: " +
                            maintenance.populationSize +
                            ", trial length: " +
                            this.durationToString({timeUnit: maintenance.evolutionCycleLengthUnit, length: maintenance.evolutionCycleLength}) +
                            ", clusterer: " +
                            ( maintenance.clustererName === undefined ? "OLD" : maintenance.clustererName)
                            + ")";
                    }

                    return JSON.stringify(maintenance);
                }
            },
            read: function (url) {
                var error;
                var csv = $.ajax({
                    url: url,
                    async: false,
                    dataType: 'text',
                    error: function (xhr, ajaxOptions, thrownError) {
                        error = thrownError;
                    }
                }).responseText;

                if (error != null) {
                    alert(error);
                    csv = "";
                }

                return csv;
            },
            readAsJSON: function (url) {
                var content = this.read(url);
                return $.parseJSON(content);
            },
            readCSV: function (url, columns, converters, skip_header) {
                var content = this.read(url);
                var csv_as_array = $.csv.toArrays(content, { onParseValue: function (value, state) {

                    var casted_value = $.csv.hooks.castToScalar(value.replace(/,/g, ""));
                    var converter = converters[state.colNum - 1];
                    if (state.rowNum > 1 && converter !== undefined) {
                        casted_value = converter(casted_value);
                    }

                    return  casted_value;
                } });

                if (skip_header) {
                    csv_as_array.splice(0, 1);
                }

                return csv_as_array.map(
                    function (value, index) {
                        var row = new Array();
                        columns.forEach(function (column) {
                            row.push(value[column]);
                        })
                        return row;
                    });
            },
            resultsPath: '../../../../../results/',
            scenarioJSONPath: function (scenario_name) {
                return this.resultsPath + scenario_name + '/scenario.json';
            },
            repetitionsJSONPath: function (scenario_name) {
                return this.resultsPath + scenario_name + '/repetitions/repetitions.json';
            },
            repetitionZipFiles: function (scenario_name) {

                var zip_files = [];
                var zip_names = this.readAsJSON(this.repetitionsJSONPath(scenario_name));
                var promisses = []

                zip_names.forEach(function (name) {

                    var deffered = $.Deferred();
                    var zip_path = this.resultsPath + scenario_name + '/repetitions/' + name;

                    JSZipUtils.getBinaryContent(zip_path, function (error, data) {
                        if (error) {
                            deffered.rejectWith(zip_path, [error]);
                        }
                        zip_files.push(new JSZip(data))
                        deffered.resolve();
                    });
                    promisses.push(deffered);
                }, this);

                var deffered = $.Deferred();
                $.when.apply($, promisses).done(function () {
                    deffered.resolveWith(scenario_name, [zip_files]);
                }).fail(function (error) {
                    deffered.rejectWith(scenario_name, [error]);
                });

                return deffered.promise();
            },
            analysisPath: function (scenario_name, file_name) {
                return this.resultsPath + scenario_name + "/analysis/" + file_name;
            },
            resizeElementHeight: function (element) {
                var height = 0;
                var body = window.document.body;
                if (window.innerHeight) {
                    height = window.innerHeight;
                } else if (body.parentElement.clientHeight) {
                    height = body.parentElement.clientHeight;
                } else if (body && body.clientHeight) {
                    height = body.clientHeight;
                }
                element.style.height = ((height - element.offsetTop - 50) + "px");
            },
            euclideanDistance: function (p1, p2) {

                if (p1.length != p2.length) {
                    throw  "unequal pint lengths"
                }

                var sum = 0;
                for (var i = 0; i < p1.length; i++) {
                    var dp = p1[i] - p2[i];
                    sum += dp * dp;
                }
                return Math.sqrt(sum);
            }
        }
    });