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
            highcharts: 'lib/highcharts-3.0.10',
            highcharts_more: 'lib/highcharts-more-3.0.10',
            highcharts_exporting: 'lib/highcharts-exporting-3.0.10',
            observations: 'config/observations',
            query: 'query'
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
            }
        }
    }
)
require(
    [
        'jquery',
        'config/scenarios',
        'config/chart_options',
        'observations',
        'util', 'query',
        'action',
        'mark',
        'bootstrap',
        'highcharts' ,
        'highcharts_more',
        'highcharts_exporting',

    ],
    function ($, scenarios, chart_options, observations, util, query, action) {
        observations.makeMenu();
        query.init();
        $(function () {
            $('#chart1').highcharts({
                title: {
                    text: 'sss'
                },
                chart: {
                    zoomType: 'xy'

                },
                credits: {
                    enabled: false
                },

                yAxis: {
                    min: 0,
                    max: 1,
                    title: "aaa"
                },
                series: [
                    {
                        name: 'CPU usage ',
                        data: util.readCSV(
                            util.analysisPath('scenario_8', 'thread_cpu_usage_gauge.csv')
                        ).map(
                            function (value, index) {
                                if (index == 0) return [0, 0];
                                return [util.convert.secondToHour(value[0]), value[1]]
                            }),
                        marker: {
                            enabled: false
                        },
                        zIndex: 1,
                        lineWidth: 1
                    },
                    {
                        name: 'Range',
                        type: 'arearange',
                        data: util.readCSV(
                            util.analysisPath('scenario_8', 'thread_cpu_usage_gauge.csv')
                        ).map(
                            function (value, index) {
                                if (index == 0) return [0, 0, 0];
                                return [util.convert.secondToHour(value[0]), value[2], value[3]]
                            }),
                        fillOpacity: 0.3,
                        zIndex: 0, lineWidth: 0
                    }
                ]
            });
        });
    }
);