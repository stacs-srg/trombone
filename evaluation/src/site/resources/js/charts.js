google.load('visualization', '1');

function getDataTable2(scenario_name, json_name) {
    var json_data = $.ajax({

        url: "/Users/masih/Documents/PhD/Code/t3/evaluation/results/" + scenario_name + "/analysis/" + json_name,
        dataType: "csv",
        async: false
    }).responseText;

    return new google.visualization.arrayToDataTable($.csv.toArrays(json_data, { onParseValue: $.csv.hooks.castToScalar }));
}

function getArray(scenario_name, json_name) {
    var json_data = $.ajax({

        url: "/Users/masih/Documents/PhD/Code/t3/evaluation/results/" + scenario_name + "/analysis/" + json_name,
        dataType: "csv",
        async: false
    }).responseText;

    return $.csv.toArrays(json_data, { onParseValue: $.csv.hooks.castToScalar });
}

function getDataTable(scenario_name, json_name) {
    var json_data = $.ajax({

        url: "/Users/masih/Documents/PhD/Code/t3/evaluation/results/" + scenario_name + "/analysis/" + json_name,
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