google.load('visualization', '1.1', {packages: ["corechart"]});
google.load('visualization', '1.1', {packages: ['charteditor']});

var legend_colours = ["#3366cc", "#dc3912", "#ff9900", "#109618", "#990099", "#0099c6", "#dd4477", "#66aa00", "#b82e2e", "#316395", "#994499", "#22aa99", "#aaaa11", "#6633cc", "#e67300", "#8b0707", "#651067", "#329262", "#5574a6", "#3b3eac", "#b77322", "#16d620", "#b91383", "#f4359e", "#9c5935", "#a9c413", "#2a778d", "#668d1c", "#bea413", "#0c5922", "#743411"];
var one_hour_in_seconds = 60 * 60;
var one_gigabyte_in_bytes = 1073741824;


var Spinner = function (id) {
    this.id = id;
    this.start = function () {
        $('#' + id).css('display', 'block');
    }

    this.stop = function () {
        $('#' + id).css('display', 'none');
    }
    return this;
};
var SPINNER = new Spinner("spinner");

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

function populateFilterCheckboxes(labels, parent_id, query_property_name) {

    labels.forEach(function (key, index) {

        var label = $("<label></label>");
        var checkbox = $('<input type="checkbox" value="' + key + '" id="' +
            query_property_name + '_' + index + '" />');

        checkbox.click(function () {
            var selection = query[query_property_name];
            var element_index = selection.indexOf(index);
            if (!this.checked) {
                if (element_index > -1) {
                    selection.splice(element_index, 1);
                }
            } else {
                if (element_index == -1) {
                    selection.push(index);
                }
            }
        });

        label.append(checkbox)
        label.append(key)

        var parent = $('#' + parent_id);
        parent.append(label);
        parent.append("<br/>");
    });
}


function read(url) {
    return $.ajax({
        url: url,
        async: false,
        dataType: 'json'
    }).responseText;
}

function readAsJSON(url) {
    var content = read(url);
    return jQuery.parseJSON(content);
}

function readCSVAsDataTable(url, conversion) {

    var data_csv = read(url);
    var data_as_array = $.csv.toArrays(data_csv, { onParseValue: function (value, state) {

        var casted_value = $.csv.hooks.castToScalar(value.replace(/,/g, ""));

        var converter_name = conversion.column[state.colNum];
        if (state.rowNum > 1 && converter_name !== undefined) {

            var converter = window[converter_name];
            if (typeof converter === 'function') {
                casted_value = converter(casted_value);
            }
        }

        return  casted_value;
    } });
    return new google.visualization.arrayToDataTable(data_as_array);
}

function secondToHour(seconds) {
    return seconds / one_hour_in_seconds;
}

function byteToGigabyte(bytes) {
    return bytes / one_gigabyte_in_bytes;
}

function toPercent(number) {
    return number * 100;
}

function saveChartAsSVG(chart_element_id, file_name) {
    var svg_element = document.getElementById(chart_element_id).getElementsByTagName('svg')[0];
    svg_element.setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
    svg_element.setAttribute("xmlns", "http://www.w3.org/2000/svg");

    var svg_text = svg_element.parentNode.innerHTML;
    var blob = new Blob([svg_text], {type: "image/svg+xml;charset=utf-8"});
    saveAs(blob, file_name);
}

function saveChartOptions(chart_wrapper, file_name) {
    var options_json = JSON.stringify(chart_wrapper.getOptions());
    var blob = new Blob([options_json], {type: "application/json;charset=utf-8"});
    saveAs(blob, file_name);
}

function setColumnProperty(indices, datatable, property_name, property_value) {

    indices.forEach(function (index) {
        datatable.setColumnProperty(index, property_name, property_value)
    })
}


function getColumn(data_table, index) {
    return {
        type: data_table.getColumnType(index),
        label: data_table.getColumnLabel(index),
        id: data_table.getColumnId(index),
        role: data_table.getColumnRole(index),
        pattern: data_table.getColumnPattern(index)
    };
}

function readScenarios() {
    var scenario_names = readAsJSON('config/scenarios.json');
    var scenarios = new Array();

    scenario_names.scenarios.forEach(function (scenario_name) {

        try {
            scenarios.push(readAsJSON('../../../../results/' + scenario_name + '/scenario.json'));
        } catch (e) {
            console.log("failed to load " + scenario_name + " : " + e);
        }
    });
    return scenarios;
}

function tidyScenario(scenario) {
    var host_scenario = scenario.hostScenarios[0];
    return{
        name: scenario.name,
        network_size: scenario.maximumNetworkSize,
        experiment_duration: durationToString(scenario.experimentDuration),
        workload: workloadToString(host_scenario.workload),
        churn: churnToString(host_scenario.churn),
        maintenance: maintenanceToString(host_scenario.configuration.maintenance)
    };
}

function mergeAsDataTable(csv_files, interval_col_indices, labels, label_index, conversion) {

    var data = new google.visualization.DataTable();
    var rows = new Array();

    var total_columns = 0;
    csv_files.forEach(function (csv, index) {
        var data_table = readCSVAsDataTable(csv, conversion);
        setColumnProperty(interval_col_indices, data_table, "role", "interval");
        data_table.setColumnLabel(label_index, labels[index]);

        for (var row = 0; row < data_table.getNumberOfRows(); row++) {

            if (rows[row] == undefined) {
                rows[row] = new Array();
            }
            var skipped = 0;
            for (var column = 0; column < data_table.getNumberOfColumns(); column++) {

                if (column == 0 && data.getNumberOfColumns() != 0) {
                    skipped++;
                    rows[row][column] = data_table.getValue(row, column);
                    continue;
                } else {
                    rows[row][total_columns + column - skipped] = data_table.getValue(row, column);
                }
            }
        }


        for (var column = 0; column < data_table.getNumberOfColumns(); column++) {

            if (column == 0) {
                if (data.getNumberOfColumns() == column) {
                    data.addColumn(getColumn(data_table, column));
                } else {
                    continue;
                }
            } else {
                data.addColumn(getColumn(data_table, column));
            }
        }

        total_columns = data.getNumberOfColumns();
    });

    for (var i = 0; i < rows.length; i++) {

        for (var j = rows[i].length; j < total_columns; j++) {
            rows[i][j] = 0;
        }
    }

    data.addRows(rows);
    return data;
}


function getObservationPath(scenario, observation_name) {

    return "../../../../results/" + scenario.name + "/analysis/" + observation_name;
}

function durationToString(duration) {

    var time_unit = duration.timeUnit.toUpperCase();
    var length = duration.length;

    if (length == 9223372036854775807) {
        return "Infinite"
    }
    if (length == 0) {
        return "None"
    }

    var short_unit;

    if (time_unit == "NANOSECONDS") {
        short_unit = "ns";
    }
    if (time_unit == "MICROSECONDS") {
        short_unit = "micros";
    }
    if (time_unit == "MILLISECONDS") {
        short_unit = "ms";
    }
    if (time_unit == "SECONDS") {
        short_unit = "s";
    }
    if (time_unit == "MINUTES") {
        short_unit = "mins";
    }
    if (time_unit == "HOURS") {
        short_unit = "hrs";
    }
    if (time_unit == "DAYS") {
        short_unit = "days";
    }

    return length + " " + short_unit;
}

function intervalsToString(intervals) {

    if (intervals.name == "OscillatingExponentialInterval") {
        return "Oscillating Exponential(min: " + durationToString(intervals.minMean) + ", max: " + durationToString(intervals.maxMean) + ", cycle: " + durationToString(intervals.cycleLength) + ")"
    }

    if (intervals.name == "FixedExponentialInterval") {
        return "Exponential(mean: " + durationToString(intervals.mean) + ")"
    }
    if (intervals.name == "ConstantIntervalGenerator") {
        return durationToString(intervals.constantInterval);
    }

    return JSON.stringify(intervals);
}

function workloadToString(workload) {
    var intervals = intervalsToString(workload.intervals);
    return  intervals == "Infinite" ? "None" : intervals;
}

function churnToString(churn) {
    return "session length: " + intervalsToString(churn.sessionLength) +
        ", down time: " + intervalsToString(churn.downtime);
}

function maintenanceToString(maintenance) {

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
            durationToString({timeUnit: maintenance.evolutionCycleLengthUnit, length: maintenance.evolutionCycleLength})
            + " " +
            (maintenance.clusterer === undefined ? "OLD" : JSON.stringify(maintenance.clusterer).replace(/(\{|\}|\")/g, "-")) +
            ")";
    }

    return JSON.stringify(maintenance);
}
function encodeQuery() {
    var encoded = query.metric + "_";
    encoded += encodeArray(query.churn) + "_";
    encoded += encodeArray(query.workload) + "_";
    encoded += encodeArray(query.maintenance);
    return encoded;
}

function encodeArray(array) {
    var encoded = "";
    var last_index = array.length - 1;
    array.forEach(function (value, index) {
        encoded += value;
        encoded += index != last_index ? "-" : "";
    });
    return encoded;
}

function decodeArray(encoded_array) {
    var decoded_array = new Array();
    encoded_array.split("-").forEach(function (element) {
        decoded_array.push(parseInt(element))
    });
    return decoded_array;
}
function getColumnValues(data_table, column) {
    var column_values = new Array();
    for (var i = 0; i < data_table.getNumberOfRows(); i++) {
        column_values[i] = data_table.getValue(i, column);
    }
    return column_values;
}

function getAverageOfColumn(data_table, column) {

    var column_values = getColumnValues(data_table, column);

    var sum = 0;
    for (var i = 0; i < column_values.length; i++) {
        sum += column_values[i];
    }

    return sum / column_values.length;
}
function decodeQuery(encoded) {

    var decoded = {};

    if (encoded === undefined || encoded.trim() == '') {
        decoded = {metric: 0, churn: [], workload: [], maintenance: []};
    } else {
        encoded.split("_").forEach(function (value, index) {

            if (index == 0) {
                decoded.metric = parseInt(value);
            }
            if (index == 1) {
                decoded.churn = decodeArray(value);
            }
            if (index == 2) {
                decoded.workload = decodeArray(value);
            }
            if (index == 3) {
                decoded.maintenance = decodeArray(value);
            }
        });
    }

    return decoded;
}

function drawChart(wrapper, scenarios, interval_col_indices, observation) {

    SPINNER.start();

    var data;
    if (observation.file_name instanceof Array) {

        data = new google.visualization.DataTable();
        data.addColumn('number', 'X');
        scenarios.forEach(function (scenario) {
            data.addColumn('number', scenario.name);
        });


        var rows = new Array();

        scenarios.forEach(function (scenario, scenario_index) {

            rows[scenario_index] = new Array();
            for (var index = 0; index < scenarios.length + 1; index++) {
                rows[scenario_index][index] = null;
            }

            observation.file_name.forEach(function (file_name, index) {
                var datatable = readCSVAsDataTable(getObservationPath(scenario, file_name), observation.conversion);

                if (index == 0 || scenario_index == 0) {
                    rows[scenario_index][index] = getAverageOfColumn(datatable, 1);
                } else {
                    rows[scenario_index ][scenario_index + index] = getAverageOfColumn(datatable, 1);
                }
            })
        });
        data.addRows(rows);

    } else {

        var csv_files = new Array();
        var labels = new Array();
        scenarios.forEach(function (scenario) {
            csv_files.push(getObservationPath(scenario, observation.file_name));
            labels.push(scenario.name);
        });
        data = mergeAsDataTable(csv_files, interval_col_indices, labels, 1, observation.conversion);
    }


    wrapper.setDataTable(data);
    wrapper.setChartType(observation.chart_type);
    wrapper.setOptions(observation.options);
    wrapper.draw();

    SPINNER.stop();
    return wrapper
}

