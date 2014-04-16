define(
    ['jquery', 'json', 'csv'],
    function () {

        Array.prototype.groupBy = function (property_name) {
            var groups = {};
            this.forEach(function (element) {
                var key = element[property_name];
                if (groups[key] === undefined) {
                    groups[key] = new Array();
                }
                groups[key].push(element);
            });
            return groups;
        };
        Array.prototype.sortBy = function (property_name) {
            this.sort(function (one, other) {
                return one[property_name].localeCompare(other[property_name]);
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

        return{

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
                            ( maintenance.clustererName === undefined ? "None[small-space]" : maintenance.clustererName == 'PFClustClusterer' && scenario.name.startsWith('scenario_batch4_')? maintenance.clustererName + 'Optimised' : maintenance.clustererName);
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