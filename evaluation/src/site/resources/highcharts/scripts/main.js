require.config(
    {
        baseUrl: 'scripts',
        paths: {
            jquery: 'lib/jquery-1.11.0',
            jquery_ui: 'lib/jquery-ui-1.10.4.min',
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
            jstat: 'lib/jstat',

            observations: 'config/observations'

        },
        shim: {
            jquery_ui: {
                deps: ['jquery']
            },
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
        'mark',
        'bootstrap',
        'highcharts' ,
        'highcharts_more',
        'highcharts_exporting',
        'highcharts_3d'
    ],
    function ($, observations, util, query, action, series, theme, chart, data) {

        observations.makeMenu();
        query.init();

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