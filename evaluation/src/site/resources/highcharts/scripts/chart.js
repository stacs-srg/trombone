define(['jquery', 'config/theme', 'highcharts' , 'highcharts_more', 'highcharts_exporting'], function ($, theme) {

    var container = $('#chart');
    container.highcharts(theme);
    return container.highcharts();
})