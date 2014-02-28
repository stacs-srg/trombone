google.load('visualization', '1');

function getDataTable2(scenario_name, json_name) {
    var json_data = $.ajax({

        url: "../../../../results/" + scenario_name + "/analysis/" + json_name,
        dataType: "csv",
        async: false
    }).responseText;

    return new google.visualization.arrayToDataTable($.csv.toArrays(json_data, { onParseValue: $.csv.hooks.castToScalar }));
}

function getArray(scenario_name, json_name) {
    var json_data = $.ajax({

        url: "../../../../results/" + scenario_name + "/analysis/" + json_name,
        dataType: "csv",
        async: false
    }).responseText;

    return $.csv.toArrays(json_data, { onParseValue: $.csv.hooks.castToScalar });
}

function getDataTable(scenario_name, json_name) {
    var json_data = $.ajax({

        url: "../../../../results/" + scenario_name + "/analysis/" + json_name,
        dataType: "json",
        async: false
    }).responseText;

    return new google.visualization.DataTable(json_data);
}

function openEditor(element_id) {

    var editor = new google.visualization.ChartEditor();
    google.visualization.events.addListener(editor, 'ok',
        function () {
            wrapper = editor.getChartWrapper();
            wrapper.draw(document.getElementById(element_id));
        });
    editor.openDialog(wrapper);
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

function toTitleCase(value) {
    return value.replace(/(?:^|\s)\w/g, function (match) {
        return match.toUpperCase();
    });
}

function replaceAll(value, find, substitude) {
    return value.replace(new RegExp(find, 'g'), substitude);
}

function mergeByRow(array_1, array_2) {


    var rows = Math.max(array_1.length, array_2.length);
    var merged_by_row = new Array();


    for (var i = 0; i < rows; i++) {

        if (array_1[i] != undefined && array_2[i] != undefined) {

            var a1_i_l = array_1[i].length;
            var a2_i_l = array_2[i].length;


            var cells = a1_i_l + a2_i_l;
            var row = new Array(cells)

            for (var j = 0; j < a1_i_l; j++) {
                row[j] = array_1[i][j];
            }
            if (array_2[i] !== undefined) {

                for (var j = 0; j < a2_i_l; j++) {
                    row[a1_i_l + j] = array_2[i][j];
                }
            }
            merged_by_row[i] = row

            console.log(row)
        }
    }
    return merged_by_row;
}

function getScenario(scenario_name) {
    var json_data = $.ajax({

        url: "../../../../results/" + scenario_name + "/analysis/scenario.json",
        dataType: "json",
        async: false
    }).responseText;
    return jQuery.parseJSON(json_data);
}

function deepCompare () {
    var leftChain, rightChain;

    function compare2Objects (x, y) {
        var p;

        // remember that NaN === NaN returns false
        // and isNaN(undefined) returns true
        if (isNaN(x) && isNaN(y) && typeof x === 'number' && typeof y === 'number') {
            return true;
        }

        // Compare primitives and functions.     
        // Check if both arguments link to the same object.
        // Especially useful on step when comparing prototypes
        if (x === y) {
            return true;
        }

        // Works in case when functions are created in constructor.
        // Comparing dates is a common scenario. Another built-ins?
        // We can even handle functions passed across iframes
        if ((typeof x === 'function' && typeof y === 'function') ||
            (x instanceof Date && y instanceof Date) ||
            (x instanceof RegExp && y instanceof RegExp) ||
            (x instanceof String && y instanceof String) ||
            (x instanceof Number && y instanceof Number)) {
            return x.toString() === y.toString();
        }

        // At last checking prototypes as good a we can
        if (!(x instanceof Object && y instanceof Object)) {
            return false;
        }

        if (x.isPrototypeOf(y) || y.isPrototypeOf(x)) {
            return false;
        }

        if (x.constructor !== y.constructor) {
            return false;
        }

        if (x.prototype !== y.prototype) {
            return false;
        }

        // check for infinitive linking loops
        if (leftChain.indexOf(x) > -1 || rightChain.indexOf(y) > -1) {
            return false;
        }

        // Quick checking of one object beeing a subset of another.
        // todo: cache the structure of arguments[0] for performance
        for (p in y) {
            if (y.hasOwnProperty(p) !== x.hasOwnProperty(p)) {
                return false;
            }
            else if (typeof y[p] !== typeof x[p]) {
                return false;
            }
        }

        for (p in x) {
            if (y.hasOwnProperty(p) !== x.hasOwnProperty(p)) {
                return false;
            }
            else if (typeof y[p] !== typeof x[p]) {
                return false;
            }

            switch (typeof (x[p])) {
                case 'object':
                case 'function':

                    leftChain.push(x);
                    rightChain.push(y);

                    if (!compare2Objects (x[p], y[p])) {
                        return false;
                    }

                    leftChain.pop();
                    rightChain.pop();
                    break;

                default:
                    if (x[p] !== y[p]) {
                        return false;
                    }
                    break;
            }
        }

        return true;
    }

    if (arguments.length < 1) {
        return true; //Die silently? Don't know how to handle such case, please help...
        // throw "Need two or more arguments to compare";
    }

    for (var i = 1, l = arguments.length; i < l; i++) {

        leftChain = []; //todo: this can be cached
        rightChain = [];

        if (!compare2Objects(arguments[0], arguments[i])) {
            return false;
        }
    }

    return true;
}