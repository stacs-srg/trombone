define(['jquery', 'util', 'highcharts'], function ($, util) {

    var container = $('#chart');

    return{
        data: {},
        observation: {},
        matches: [],
        chart: {},
        renderer: function () {


            if (!this.observation.chart.onePerMatch) {

                if (this.observation.series_provider !== undefined && this.observation.series.length == 0) {
                    $('#extras').html('');
                    this.observation.populateSeries(this.matches)
                }
            } else {
                alert("not implemented yet");
            }
            this.refresh();
        },
        refresh: function () {

            container.highcharts(this.observation)
            this.chart = container.highcharts();
        }
    }
})