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

function getAverageOfColumn(data_table, column) {
    return google.visualization.data.avg(data_table.getDistinctValues(column));
}