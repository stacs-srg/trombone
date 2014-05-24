require.config(
    {
        baseUrl: 'scripts',
        paths: {
            jquery: 'lib/jquery-1.11.0',
            mustache: 'lib/mustache',
            filesaver: 'lib/filesaver',
            jstat: 'lib/jstat',
            csv: 'lib/jquerycsv-0.71',
            mark: 'lib/markup-1.5.18',
            json: 'lib/json',
            bootstrap: 'lib/bootstrap-3.1.1',
            highcharts: 'lib/highcharts-4.0.1/highcharts',
            highcharts_more: 'lib/highcharts-4.0.1/highcharts-more',
            highcharts_exporting: 'lib/highcharts-4.0.1/modules/exporting',
            highcharts_3d: 'lib/highcharts-4.0.1/highcharts-3d',
            highcharts_csv: 'lib/highcharts-4.0.1/modules/export-csv',
            highcharts_draggable_legend: 'lib/highcharts-4.0.1/modules/draggable-legend-box',
            jstat: 'lib/jstat',
            jszip: 'lib/jszip.min',
            jszip_utils: 'lib/jszip-utils.min',
            observations: 'config/observations'
        },
        shim: {
            bootstrap: {
                deps: ['jquery']
            },
            csv: {
                deps: ['jquery']
            },
            highcharts: {
                deps: ['jquery']
            },
            highcharts_more: {
                deps: ['jquery', 'highcharts']
            },
            highcharts_exporting: {
                deps: ['jquery', 'highcharts']
            },
            highcharts_3d: {
                deps: ['jquery', 'highcharts']
            },
            highcharts_csv: {
                deps: ['jquery', 'highcharts']
            },
            highcharts_draggable_legend: {
                deps: ['jquery', 'highcharts']
            }
        }
    }
)
require(
    [
        'jquery',
        'observations',
        'util',
        'query',
        'action',
        'series',
        'config/theme',
        'chart',
        'data',
        'scope',
        'scenario',
        'mark',
        'bootstrap',
        'highcharts' ,
        'highcharts_more',
        'highcharts_exporting',
        'highcharts_3d',
        'highcharts_csv',
        'highcharts_draggable_legend',
        'jszip',
        'jszip_utils'

    ],
    function ($, observations, util, query, action, series, theme, chart, data, scope, scenario) {

        observations.makeMenu();
        query.init();

//        scenario('scenario_1').data();

        $(window).bind('resize', function (e) {
            window.resizeEvt;
            $(window).resize(function () {
                clearTimeout(window.resizeEvt);
                window.resizeEvt = setTimeout(function () {
                    util.resizeElementHeight(document.getElementById("chart_list"));
                }, 250);
            });
        });
    }
);